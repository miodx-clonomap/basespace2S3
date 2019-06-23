package ohnosequences.basespace2s3


case class Basespace2S3Config(
  s3bucket: String,
  keyPairName: String,
  iamRoleName: String,
  iamInstanceProfileName: String,
  artifactUrl: String
) { self =>
  object baseSpace2S3Metadata extends ohnosequences.statika.AnyArtifactMetadata {
    val organization: String = "com.miodx.clonomap"
    val artifact: String     = "clonomapbridge"
    val version: String      = "0.3.1-SNAPSHOT"
    val artifactUrl: String  = self.artifactUrl
  }

}


object Basespace2S3Config extends Basespace2S3Config(
  s3bucket = "miodx-clonomap-backups-dev",    // "miodx"
  keyPairName = "primetalk_deployer_key",     // "miodx-dev"
  iamRoleName = "primetalk_webmiodx_iam_role", // "era7-projects"
  iamInstanceProfileName = "primetalk_webmiodx_profile",
  // "s3://snapshots.miodx.com/com.miodx.clonomap/basespace2s3_2.11/0.2.0-SNAPSHOT/basespace2s3_2.11-0.2.0-SNAPSHOT-fat.jar"
  artifactUrl = "s3://miodx-clonomap-backups-dev/com.miodx.clonomap/basespace2s3_2.11/0.2.0-SNAPSHOT/basespace2s3-assembly-0.2.0-SNAPSHOT.jar"
) {

}

