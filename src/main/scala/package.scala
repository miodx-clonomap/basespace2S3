package era7bio

package basespace2s3 {
  case class BasespaceError ( val error: String )
  case class S3Error        ( val error: String )
  case class NotifyError    ( val error: String )
}

package object basespace2s3 {

  // TODO ...
  type BasespaceURL =
    String

  type URL =
    String

  type Reply =
    String

  type Token =
    String

  // TODO maybe (String,String) so that ("md5", "dfda43ad6m3na...")
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

  // (bucket, key)
  type S3Object =
    (String, String)

  type CheckedS3Object =
    (S3Object, Checksum)

  type +[X,Y] =
     Either[X,Y]
}
