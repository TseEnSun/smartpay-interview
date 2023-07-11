package prices

import cats.effect.std.Semaphore
import cats.effect.{IO, IOApp, Resource}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import dev.profunktor.redis4cats.effect.Log.NoOp._

import prices.config.Config
import prices.resources.AppResources

object Main extends IOApp.Simple {

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def run: IO[Unit] = (
    for {
    config <- Resource.eval(Config.load[IO])
    semaphore <- Resource.eval(Semaphore[IO](1))
    resources <- AppResources.make[IO](config)
  } yield (config, semaphore, resources)
  ).use { case (config, semaphore, resources) =>
    Server.serve[IO](config, resources, semaphore).compile.drain
  }

}
