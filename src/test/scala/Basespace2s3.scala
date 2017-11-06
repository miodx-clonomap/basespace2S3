package era7bio.basespace2s3.test

import org.scalatest.FunSuite

import era7bio.basespace2s3._
import era7bio.basespace2s3.code.checkFile

class Basespace2s3Test extends FunSuite {

  test("Checksum a file") {
    val testFile = new File(
      getClass.getResource("/random.txt").getPath
    )

    val (file, md5) = checkFile(testFile)

    assert( md5 === "7b6da1fc59041fb6eaac76e7d9a6aa38" )
  }
}
