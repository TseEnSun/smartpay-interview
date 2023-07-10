package prices.config

import cats.effect.kernel.Sync

import pureconfig.ConfigSource
import pureconfig.generic.auto._

case class Config(
    app: Config.AppConfig,
    smartcloud: Config.SmartCloudConfig,
    redis: Config.RedisConfig
)

object Config {

  case class AppConfig(
      host: String,
      port: Int
  )

  case class SmartCloudConfig(
      baseUri: String,
      token: String
  )

  case class RedisConfig(
      host: String,
      port: Int
  )

  def load[F[_]: Sync]: F[Config] =
    Sync[F].delay(ConfigSource.default.loadOrThrow[Config])

}
