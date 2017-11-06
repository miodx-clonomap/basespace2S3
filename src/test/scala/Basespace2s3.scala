package era7bio.basespace2s3.test

import org.scalatest._
import Matchers._

import era7bio.basespace2s3._
import era7bio.basespace2s3.code._
import play.api.libs.ws._
import play.api.libs.ws.ahc._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

class Basespace2s3Test extends FunSuite {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  test("Checksum a file") {
    val testFile = new File(
      getClass.getResource("/random.txt").getPath
    )

    val (file, md5) = checkFile(testFile)

    assert( md5 === "7b6da1fc59041fb6eaac76e7d9a6aa38" )
  }

  test("Download a file") {
    val ws = AhcWSClient()

    // Random file in the internet with pre-computed MD5
    val url = "https://raw.githubusercontent.com/era7bio/webmiodx/master/docs/visualizations/TCRbeta_4_time_points_with_clonotype_data_V2.csv?token=ADvjT_iXya3DVPhLUWfr24LHVg3ttovUks5aCZNIwA%3D%3D"
    val md5 = "1c408770485f3c9c4af65d4e41aab4b0"
    val tmpFile = new File("/tmp/file.txt")
    val fileStream = openStream(tmpFile)

    val downloadResult = downloadTo(ws)(url)(fileStream)
    ws.close()

    downloadResult match {
      case Left(error)             => fail
      case Right((file, checksum)) => assert(md5 === checksum)
    }

  }
}
