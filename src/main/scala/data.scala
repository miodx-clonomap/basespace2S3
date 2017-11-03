package era7bio.basespace2s3

import ohnosequences.datasets._
import ohnosequences.loquat._
import ohnosequences.statika._
import ohnosequences.datasets._
import ohnosequences.cosas._, types._, records._

case object data {

  case object basespaceFileURL  extends Data("file-url")
  case object basespaceFileS3   extends Data("file-s3")
  case object notifyTo          extends Data("notify-url")
  // TODO maybe outputs
  case object checksum          extends Data("checksum")
  case object sizeKB            extends Data("sizeKB")

  case object input   extends DataSet(
    basespaceFileURL  :×:
    basespaceFileS3   :×:
    notifyTo          :×:
    |[AnyData]
  )

  case object output extends DataSet(
    // checksum    :×:
    // sizeKB      :×:
    |[AnyData]
  )
}
