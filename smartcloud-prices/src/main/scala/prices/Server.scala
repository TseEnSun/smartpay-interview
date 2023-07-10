package prices

import cats.syntax.all._
import cats.effect._
import cats.effect.std.Semaphore
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

import prices.config.Config
import prices.resources.AppResources
import prices.apiClient.SmartCloudClient
import prices.routes.InstanceKindRoutes
import prices.routes.InstancePriceRoutes
import prices.services.SmartCloudInstanceKindService
import prices.services.SmartCloudInstancePriceService
import prices.services.RedisCacheService
import prices.programs.CachedInstancePriceProgram


object Server {

  def serve(config: Config, resources: AppResources[IO], semaphore: Semaphore[IO]): Stream[IO, ExitCode] = {

    // val instanceKindService = SmartCloudInstanceKindService.dummy[IO]

    val smartCloudClient = SmartCloudClient.make[IO](config.smartcloud, resources.httpClient)
    val instanceKindService = SmartCloudInstanceKindService.make[IO](smartCloudClient)
    val instancePriceService = SmartCloudInstancePriceService.make[IO](smartCloudClient)
    val cacheService = RedisCacheService.make[IO](resources.redis)

    val instancePriceProgram = CachedInstancePriceProgram.make[IO](instancePriceService, cacheService, semaphore)

    val httpApp = (
      InstanceKindRoutes[IO](instanceKindService).routes <+> InstancePriceRoutes[IO](instancePriceProgram).routes
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
