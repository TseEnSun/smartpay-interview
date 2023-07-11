package prices

import cats.syntax.all._
import cats.effect._
import cats.effect.std.Semaphore
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{Logger => MiddlewareLogger}
import org.typelevel.log4cats.Logger
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

  def serve[F[_]: Async: Logger](
    config: Config,
    resources: AppResources[F],
    semaphore: Semaphore[F]
  ): Stream[F, ExitCode] = {

    // val instanceKindService = SmartCloudInstanceKindService.dummy[F]

    val smartCloudClient = SmartCloudClient.make[F](config.smartcloud, resources.httpClient)
    val instanceKindService = SmartCloudInstanceKindService.make[F](smartCloudClient)
    val instancePriceService = SmartCloudInstancePriceService.make[F](smartCloudClient)
    val cacheService = RedisCacheService.make[F](resources.redis)

    val instancePriceProgram =
      CachedInstancePriceProgram.make[F](instancePriceService, cacheService, semaphore, config.redis)

    val httpApp = (
      InstanceKindRoutes[F](instanceKindService).routes <+> InstancePriceRoutes[F](instancePriceProgram).routes
    ).orNotFound

    Stream
      .eval(
        EmberServerBuilder
          .default[F]
          .withHost(Host.fromString(config.app.host).get)
          .withPort(Port.fromInt(config.app.port).get)
          .withHttpApp(MiddlewareLogger.httpApp(logHeaders = true, logBody = true)(httpApp))
          .build
          .useForever
      )

  }
}
