package prices.resources

import cats.effect.{ Async, Resource }
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder


sealed abstract class AppResources[F[_]](
  val httpClient: Client[F]
)
  
object AppResources {
  
  def make[F[_]: Async]: Resource[F, AppResources[F]] = {
    EmberClientBuilder.default[F].build.map { httpClient =>
      new AppResources[F](httpClient) {}
    }
  }
}
