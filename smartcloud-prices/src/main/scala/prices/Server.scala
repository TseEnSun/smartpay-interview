package prices

import cats.syntax.all._
import cats.effect._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import prices.config.Config
import prices.resources.AppResources
import prices.client.SmartCloudClient
import prices.routes.InstanceKindRoutes
import prices.routes.InstancePriceRoutes
import prices.services.SmartcloudInstanceKindService
import prices.services.SmartcloudInstancePriceService


object Server {

  def serve(config: Config, resources: AppResources[IO]): Stream[IO, ExitCode] = {

    implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
    // val instanceKindService = SmartcloudInstanceKindService.dummy[IO]

    val smartcloudClient = SmartCloudClient.make[IO](config.smartcloud, resources.httpClient)
    val instanceKindService = SmartcloudInstanceKindService.make[IO](smartcloudClient)
    val instancePriceService = SmartcloudInstancePriceService.make[IO](smartcloudClient)

    val httpApp = (
      InstanceKindRoutes[IO](instanceKindService).routes <+> InstancePriceRoutes[IO](instancePriceService).routes
    ).orNotFound

    Stream
      .eval(
        EmberServerBuilder
          .default[IO]
          .withHost(Host.fromString(config.app.host).get)
          .withPort(Port.fromInt(config.app.port).get)
          .withHttpApp(Logger.httpApp(true, true)(httpApp))
          .build
          .useForever
      )
  }

}
