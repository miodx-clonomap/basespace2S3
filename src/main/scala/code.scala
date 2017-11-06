package era7bio.basespace2s3

import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.awstools.s3
import play.api.libs.ws._
import play.api.libs.ws.ahc._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import java.nio.file.Files.newOutputStream
import com.amazonaws.services.s3.AmazonS3

// awful name
case object code {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  // TODO: Compute checksum (md5, whatever)
  val checkFile:
    File => CheckedFile =
      ???

  val openStream:
    File => FileStream =
      file => (file, newOutputStream(file.toPath))

  val closeResources:
    WSClient   =>
    FileStream =>
    Unit =
      ws => fileStream =>
        {
          ws.close()
          system.terminate()
          fileStream._2.close()
        }

  val downloadTo:
    WSClient     =>
    BasespaceURL =>
    FileStream   =>
    BasespaceError + CheckedFile =
      ws => url => fileStream =>
        Await.result(
          ws.url(url).withMethod("GET").stream() flatMap {
            response =>

              response.body.runWith(
                Sink.foreach[akka.util.ByteString] { bytes =>
                  fileStream._2.write(bytes.toArray)
                }
              ) map {
                _ => Right[BasespaceError, CheckedFile](
                  checkFile(fileStream._1)
                )
              } recoverWith {
                case e => Future {
                  Left[BasespaceError, CheckedFile](BasespaceError(e.toString))
                }
              }
          },
          Duration.Inf
        )

  // TODO Add checksum as metadata
  val uploadTo:
    AmazonS3    =>
    CheckedFile =>
    S3Object    =>
    S3Error + CheckedS3Object =
      s3Client => checkedFile => s3Object =>
        {
          val (file, checksum) = checkedFile
          val (bucket, key) = s3Object

          Try {
            s3Client.putObject(
              bucket,
              key,
              file
            )
          } match {
            case scala.util.Success(s) => Right((s3Object, checksum))
            case scala.util.Failure(e) => Left(S3Error(e.toString))
          }
        }

  // TODO add any params here (file, checksum, IDs, ...)
  val notifyTo:
    CheckedS3Object =>
    URL             =>
    NotifyError + Reply =
    ???

  // TODO add conf here (if needed)
  case object run extends DataProcessingBundle()(
    input  = data.input,
    output = data.output
  )
  {

    def instructions: AnyInstructions =
      say("BaseSpace to S3 file uploader")

    def process(context: ProcessingContext[Input])
      : AnyInstructions { type Out <: OutputFiles } = {

      val fileURL =
        readFile( context inputFile data.basespaceFileURL )

      val fileS3Bucket =
        readFile( context inputFile data.basespaceFileS3Bucket )

      val fileS3Key =
        readFile( context inputFile data.basespaceFileS3Key )

      val notifyURL =
        readFile( context inputFile data.notifyTo )

      val outFile =
        context / "basespaceFile"

      new SimpleInstructions[*[AnyDenotation { type Value <: FileResource }]](
        { f: File =>

          val ws         = AhcWSClient()
          val fileStream = openStream(outFile)
          val s3Client   = s3.defaultClient
          val s3Object   = (fileS3Bucket, fileS3Key)

          downloadTo(ws)(fileURL)(fileStream) match {
            case Left(BasespaceError(err)) =>
              closeResources(ws)(fileStream)
              Failure(err)
            case Right(checkedFile)        =>
              closeResources(ws)(fileStream)
              uploadTo(s3Client)(checkedFile)(s3Object) match {
                case Left(s3Err)        => Failure(s3Err.toString)
                case Right(checkS3Obj)  =>
                  notifyTo(checkS3Obj)(notifyURL) match {
                    case Left(notifyErr)  => Failure(notifyErr.toString)
                    case Right(reply)     =>
                      Success(
                        s"uploaded ${checkS3Obj._1} with checksum ${checkS3Obj._2}",
                        *[AnyDenotation { type Value <: FileResource }]
                      )
                  }
              }
          }
        }
      )
    }
  }
}
