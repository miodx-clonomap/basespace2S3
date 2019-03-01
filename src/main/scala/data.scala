package ohnosequences.basespace2s3

import ohnosequences.datasets._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.datasets._
import ohnosequences.cosas._, types._, records._

case object data {

  // Input URL from where the Basespace file will be downloaded
  case object basespaceFileURL      extends Data("file-url")

  // User-defined S3 bucket and key where the file will be uploaded
  case object basespaceFileS3Bucket extends Data("file-s3-bucket")
  case object basespaceFileS3Key    extends Data("file-s3-key")

  /**
   * The input is a Basespace file URL and the S3 path where the file will be
   * transferred, encoded in separate values that define the bucket and the key
   */
  case object input   extends DataSet(
    basespaceFileURL      :×:
    basespaceFileS3Bucket :×:
    basespaceFileS3Key    :×:
    |[AnyData]
  )

  /**
   * There is no output, the upload of the S3 files is done in the data
   * processing.
   */
  case object output extends DataSet(
    |[AnyData]
  )
}
