package era7bio.basespace2s3

import data._
import ohnosequences.loquat._
import ohnosequences.datasets._
import ohnosequences.statika._, aws._
import ohnosequences.awstools._, ec2._ , s3._, autoscaling._, regions._
import com.amazonaws.auth.profile._
import com.amazonaws.auth._
import scala.concurrent.duration._

object loquat {

  type Argh =
    scala.util.Try[java.util.concurrent.ScheduledFuture[_]]

  val run: String => List[(BasespaceURL, S3Object)] => Argh =
    prefix => seq => {
    
      val dm =
        dataMapping(prefix)(seq)

      awful.launcher.run(
        config              = loquat.config     ,
        user                = me                ,
        dataProcessing      = code.run          ,
        dataMappings        = dm                ,
        manager             = managerBundle(dm) ,
        monitoringInterval  = 30.second
      )
    }

  val defaultAMI =
      AmazonLinuxAMI(Ireland, HVM, InstanceStore)

  val metadata =
    era7bio.generated.metadata.basespace2S3

  val dataMapping:
    String => List[(BasespaceURL, S3Object)] => List[DataMapping[code.run.type]] =
      prefix => seq =>
        seq map {
          case (url, s3obj) =>
            DataMapping(prefix, code.run)(
              remoteInput = Map[AnyData, AnyRemoteResource](
                basespaceFileURL      -> MessageResource(url)       ,
                basespaceFileS3Bucket -> MessageResource(s3obj.bucket)  ,
                basespaceFileS3Key    -> MessageResource(s3obj.key)  
              ),
              remoteOutput = Map()
            )
        }
      
  // Loquat conf

  case object config extends AnyLoquatConfig {

    val loquatName =
      "this-is-sad"

    val metadata =
      loquat.metadata

    val iamRoleName =
      "era7-projects"

    val logsS3Prefix =
      S3Folder("resources.ohnosequences.com", "random-logs")

    val managerConfig =
      loquat.managerConfig

    override
    lazy val amiEnv =
      amznAMIEnv(
        ami,
        javaHeap    = 50, // GB
        javaOptions = Seq("-XX:+UseG1GC")
      )

    override
    lazy val workersConfig: AnyWorkersConfig =
      WorkersConfig(
        defaultAMI,
        r3.`2xlarge`, // TODO should be i3.2xlarge
        PurchaseModel.spot(0.2),
        AutoScalingGroupSize(0, 1, 1)
      )
  }

  // worker
  ///////////////////////////////////////////////////////////////////
  case object worker extends WorkerBundle(code.run, config)

  case object workerCompat extends CompatibleWithPrefix("era7bio.basespace2s3.loquat")(
      environment = config.amiEnv,
      bundle      = worker,
      metadata    = config.metadata
    )
    {

      override lazy val fullName: String =
        "era7bio.basespace2s3.loquat.workerCompat"
    }

  // manager
  ////////////////////////////////////////////////////////////////////
  object managerConfig
      extends ManagerConfig(
        defaultAMI,
        m3.medium,
        PurchaseModel.spot(0.1)
      )

  val managerBundle: List[DataMapping[code.run.type]] => AnyManagerBundle =
    dm =>
      new ManagerBundle(worker)(dm) {

        val fullName: String =
          "era7bio.basespace2s3.loquat"
      }

  // loquat user
  ////////////////////////////////////////////////////////////////////
   val me = LoquatUser(
      email             = "eparejatobes@ohnosequences.com",
      localCredentials  =
        new AWSCredentialsProviderChain(
          new InstanceProfileCredentialsProvider(false),
          new ProfileCredentialsProvider("default")
        ),
      keypairName       = "miodx-dev"
    )

}
