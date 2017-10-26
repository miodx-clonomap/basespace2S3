package era7bio.basespace2s3

import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._

// awful name
case object code {

  // TODO implement, add API client whatever
  // should return the same file in CheckedFile if OK
  val downloadTo:
    BasespaceAPI  =>
    File          =>
    BasespaceURL  =>
    BasespaceError + CheckedFile =
    ???

  // TODO implement, add S3 API client as arg etc
  val uploadTo:
    CheckedFile =>
    S3Object    =>
    S3Error + CheckedS3Object =
    ???

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

      val token =
        readFile( context inputFile data.token )

      val fileURL =
        readFile( context inputFile data.basespaceFileURL )

      val fileS3 =
        readFile( context inputFile data.basespaceFileS3 )

      val notifyURL =
        readFile( context inputFile data.notifyTo )

      val outFile =
        context / "basespaceFile"

      new SimpleInstructions[*[AnyDenotation { type Value <: FileResource }]](
        { f: File =>

          // TODO should be something real
          val basespaceAPI =
            token

          downloadTo(basespaceAPI)(outFile)(fileURL) match {
            case Left(basespaceErr) => Failure(basespaceErr.toString)
            case Right(checkedFile) =>
              uploadTo(checkedFile)(fileS3) match {
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
