package ohnosequences

package basespace2s3 {
  case class BasespaceError ( val error: String )
  case class S3Error        ( val error: String )
}

package object basespace2s3 {

  type BasespaceURL =
    String

  type URL =
    String

  type Reply =
    String

  type Token =
    String

  type Checksum =
    String

  type File =
    java.io.File

  type FileStream =
    (File, java.io.OutputStream)

  def readFile: File => String =
    file =>
      new String( java.nio.file.Files readAllBytes file.toPath )

  type CheckedFile =
    (File, Checksum)

  type S3Object =
    ohnosequences.awstools.s3.S3Object

  type CheckedS3Object =
    (S3Object, Checksum)

  type +[X,Y] =
     Either[X,Y]
}
