package ohnosequences.basespace2s3

import data._
import ohnosequences.loquat._
import ohnosequences.datasets._
import ohnosequences.statika._, aws._
import ohnosequences.awstools._, ec2._ , s3._, autoscaling._, regions._
import com.amazonaws.auth.profile._
import com.amazonaws.auth._
import scala.concurrent.duration._
import scala.util.Try
import java.util.concurrent.ScheduledFuture

/**
 * Namespace wrapping all Loquat-related functions, as well as the Loquat
 * configuration.
 *
 * The most important method here is run, that triggers the whole process
 */
object loquat {

  /**
   * Run the Loquat, given a dataMapping label and a list of pairs of files
   * URLs along with their expected S3 destiny
   *
   * It returns a scheduled future that resolves to the result of the Loquat
   * process, all wrapped in a Try that is successfull if the resources can be
   * successfully prepared and the manager local instructions can be
   * successfully run.
   */
  val run: String => List[(BasespaceURL, S3Object)] => Try[ScheduledFuture[_]] =
    prefix => seq => {

      val dm =
        dataMapping(prefix)(seq)

      launcher.run(
        config              = loquat.config     ,
        user                = me                ,
        dataProcessing      = transfer.run      ,
        dataMappings        = dm                ,
        manager             = managerBundle(dm) ,
        monitoringInterval  = 30.second
      )
    }

  /** The default AMI used throught the project */
  val defaultAMI =
      AmazonLinuxAMI(Ireland, HVM, InstanceStore)

  /**
   * Metadata auto-generated by Statika. For this to work, sbt must have
   * configured generateStatikaMetadataIn(Compile)
   */
  val metadata =
    com.miodx.clonomap.generated.metadata.basespace2S3

  /**
   * Define the map between data resource and its associated content. In this
   * case, for every data resource (baesspace file URL, destination bucket,
   * destination key), a MessageResource is assigned, containing a string with
   * the URL for the specific data resource.
   */
  val dataMapping:
    String =>
    List[(BasespaceURL, S3Object)] =>
    List[DataMapping[transfer.run.type]] =
      prefix => seq =>
        seq map {
          case (url, s3obj) =>
            DataMapping(prefix, transfer.run)(
              remoteInput = Map[AnyData, AnyRemoteResource](
                basespaceFileURL      -> MessageResource(url)       ,
                basespaceFileS3Bucket -> MessageResource(s3obj.bucket)  ,
                basespaceFileS3Key    -> MessageResource(s3obj.key)
              ),
              remoteOutput = Map()
            )
        }


  /**
   * Loquat configuration. This should stay as it is. The only configuration
   * that may be changed with more frequency are the IAM roles, the AMIs and
   * instances types, as well as the price for the instances.
   */
  case object config extends AnyLoquatConfig {

    /** A label used by Loquat for several purposes */
    val loquatName =
      "b2s3"

    /** The same auto-generated metadata as above */
    val metadata =
      loquat.metadata

    /**
     * The IAM role that the machines will be launched with. This is used, for
     * example, for having direct access to S3.
     */
    val iamRoleName =
      "era7-projects"

    /** Base S3 directory where the Loquat logs will be uploaded */
    val logsS3Prefix =
      S3Folder("resources.ohnosequences.com", "b2s3")

    /** The manager configuration (defined below) */
    val managerConfig =
      loquat.managerConfig

    /**
     * Define the environment of the machine. Basically used for setting Java
     * options
     */
    override
    lazy val amiEnv =
      amznAMIEnv(
        ami,
        javaHeap    = 50, // GB
        javaOptions = Seq("-XX:+UseG1GC")
      )

    /**
     * The configuratrion of the workers; i.e., the machines that will perform
     * the transfer
     */
    override
    lazy val workersConfig: AnyWorkersConfig =
      WorkersConfig(
        defaultAMI,
        r3.`2xlarge`,
        PurchaseModel.spot(0.2),
        AutoScalingGroupSize(0, 1, 1)
      )
  }

  // worker
  ///////////////////////////////////////////////////////////////////
  case object worker extends WorkerBundle(transfer.run, config)

  case object workerCompat extends CompatibleWithPrefix(
    "ohnosequences.basespace2s3.loquat"
  )(
      environment = config.amiEnv,
      bundle      = worker,
      metadata    = config.metadata
    )
    {

      override lazy val fullName: String =
        "ohnosequences.basespace2s3.loquat.workerCompat"
    }

  // manager
  ////////////////////////////////////////////////////////////////////
  object managerConfig
      extends ManagerConfig(
        defaultAMI,
        m3.medium,
        PurchaseModel.spot(0.1)
      )

  val managerBundle: List[DataMapping[transfer.run.type]] => AnyManagerBundle =
    dm =>
      new ManagerBundle(worker)(dm) {

        val fullName: String =
          "ohnosequences.basespace2s3.loquat"
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
