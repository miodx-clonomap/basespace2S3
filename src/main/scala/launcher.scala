package ohnosequences.basespace2s3

import ohnosequences.loquat._, utils._
import com.typesafe.scalalogging.LazyLogging
import scala.util.{ Try, Failure }
import scala.concurrent.duration._
import java.util.concurrent.ScheduledFuture
import java.nio.file.Files
import scala.language.existentials

/**
 * Launcher-specific functions, including the most important one:
 * [[launcher.run]]
 */
case object launcher extends LazyLogging {

  /**
   * Start Loquat, which will trigger the launching of the EC2 machines to
   * perform the file transfer
   *
   * @param config is the configuration of the Loquat
   * @param user is the user running the Loquat, mainly used for privileges
   * and notifications
   * @param dataProcessing contains the code that will be run in the remote
   * client machines
   * @param dataMappings defines the inputs and outputs of the data processing
   * and map them to input strings or output S3 resources
   * @param manager contains the code that will be run in the remote manager
   * @param monitoringInterval is the duration between consecutive monitorings
   *
   * @return a scheduled future that resolves to the result of the Loquat
   * process, all wrapped in a Try that is successfull if the resources can be
   * successfully prepared and the manager local instructions can be
   * successfully run.
   */
  def run(
    config: AnyLoquatConfig,
    user: LoquatUser,
    dataProcessing: AnyDataProcessingBundle,
    dataMappings: List[AnyDataMapping],
    manager: AnyManagerBundle,
    monitoringInterval: FiniteDuration
  ): Try[ScheduledFuture[_]] = {

    // Manager bundle needs some local directory to run in
    val localTmpDir = Files.createTempDirectory(config.loquatName).toFile

    LoquatOps.check(config, user, dataProcessing, dataMappings) match {
      case Left(msg) => Failure(new java.util.prefs.InvalidPreferencesFormatException(msg))
      case Right(aws) => {
        logger.info("Obtained AWS clients")
        // executing a chain of steps that prepare AWS resources
        val resourcesPrepared: Try[_] =
          LoquatOps.prepareResourcesSteps(config, user, aws)
            .foldLeft[Try[_]](
              util.Success(true)
            ) { (result: Try[_], next: Step[_]) =>
              result.flatMap(_ => next.execute)
            }

        logger.info("resourcesPrepared: " + resourcesPrepared.toString)

        resourcesPrepared.flatMap { _ =>
          // if the resource are ready, launching manager locally
          val result = manager.localInstructions(user).run(localTmpDir)
          logger.info("result of launching manager locally: " + result.toString)

          resultToTry(result)
        }.map { _ =>
          logger.info("everything went fine so far: ")

          // and finally if everything went fine so far, returning a ScheduledFuture with the termination monitor
          TerminationDaemonBundle(
            config,
            Scheduler(1),
            dataMappings.length
          ).checkAndTerminate(
            after = 10.seconds,
            every = monitoringInterval
          )
        }
      }
    }
  }
}
