# basespace2s3

[![](http://img.shields.io/github/release/ohnosequences/basespace2S3/all.svg)](https://github.com/ohnosequences/basespace2S3/releases/latest)
[![](https://img.shields.io/badge/license-AGPLv3-blue.svg)](https://tldrlegal.com/license/gnu-affero-general-public-license-v3-%28agpl-3.0%29)
[![](https://img.shields.io/badge/contact-gitter_chat-dd1054.svg)](https://gitter.im/era7bio/basespace2S3)

A Loquat-based process focused on one simple task: downloading the content from a URL —which happens to host a Basespace file— and uploading it to S3.

The Loquat receives as input a list of tuples that each contains three strings:
  - the URL from where the file will be downloaded
  - the S3 bucket where that will contain the uploaded file
  - the S3 key that will identify the uploaded S3 object

After the Loquat is successfully started, an autoscaling group with an EC2 instance per list item is launched. After the process finishes, all the files will be mirrored to S3.

# Usage

The most simple usage is to call `loquat.run` with a label and the list of inputs:

```scala
import ohnosequences.basespace2s3.loquat
import ohnosequences.awstools.s3._

loquat.run("b2s3")(
  List(
    "https://url/to/a/file.fastq" -> s3"bucket" / "dest" / "key1.fastq",
    "https://url/to/another/file.fastq" -> s3"bucket" / "dest" / "key2.fastq"
  )
)
