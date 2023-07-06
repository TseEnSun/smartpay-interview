package prices

import cats.effect.{ IO, IOApp }

import prices.config.Config
import prices.resources.AppResources

object Main extends IOApp.Simple {

  def run: IO[Unit] = {
    Config.load[IO].flatMap { config =>
      AppResources.make[IO].use { resources =>
        Server.serve(config, resources).compile.drain
      }
    }
  }

}
