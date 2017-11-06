package era7bio.basespace2s3.test

import org.scalatest._
import Matchers._

import era7bio.basespace2s3._
import era7bio.basespace2s3.code._
import ohnosequences.awstools.s3
import play.api.libs.ws._
import play.api.libs.ws.ahc._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import java.io.File;

class Basespace2s3Test extends FunSuite {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val testCheckedFile: CheckedFile = (
    new File(
      getClass.getResource("/random.txt").getPath
    ),
    "7b6da1fc59041fb6eaac76e7d9a6aa38"
  )

  // Random file in the internet with pre-computed MD5
  object checkedRemoteFile{
    val url = "https://raw.githubusercontent.com/era7bio/webmiodx/master/docs/visualizations/TCRbeta_4_time_points_with_clonotype_data_V2.csv?token=ADvjT_iXya3DVPhLUWfr24LHVg3ttovUks5aCZNIwA%3D%3D"
    val md5 = "1c408770485f3c9c4af65d4e41aab4b0"
  }

  test("Checksum a file") {
    val (knownFile, knownMD5) = testCheckedFile

    val (_, computedMd5) = checkFile(knownFile)

    assert( computedMd5 === knownMD5 )
  }

  test("Download a file") {
    val url = checkedRemoteFile.url
    val knownMD5 = checkedRemoteFile.md5

    val tmpFile = File.createTempFile("file", ".txt");
    tmpFile.deleteOnExit()
    val fileStream = openStream(tmpFile)

    val ws = AhcWSClient()
    val downloadResult = downloadTo(ws)(url)(fileStream)
    ws.close()

    downloadResult match {
      case Left(error)             => fail
      case Right((file, checksum)) => assert(knownMD5 === checksum)
    }
  }

  test("Upload a file") {
    val s3Client = s3.defaultClient
    val s3Object = s3.S3Object(
      "test.era7",
      s"random-${System.currentTimeMillis.toString}.txt"
    )

    print(s3Object.toString)

    val uploadResult = uploadTo(s3Client)(testCheckedFile)(s3Object)

    uploadResult match {
      case Left(error)            => fail
      case Right((obj, checksum)) =>
        assert( s3.ScalaS3Client(s3Client).objectExists(obj) )
    }
  }
}
