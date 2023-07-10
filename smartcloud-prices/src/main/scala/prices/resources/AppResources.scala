package prices.resources

import cats.effect._
import cats.implicits._
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import dev.profunktor.redis4cats.codecs.Codecs
import dev.profunktor.redis4cats.codecs.splits.SplitEpi
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.effect.Log
import org.typelevel.log4cats.Logger
import io.circe.generic.auto._
import io.circe.parser.{ decode => jsonDecode }
import io.circe.syntax._


import prices.config.Config
import prices.config.Config.RedisConfig
import prices.data._


sealed abstract class AppResources[F[_]](
  val httpClient: Client[F],
  val redis: RedisCommands[F, InstanceKind, PriceAmount]
)
  
object AppResources {

  private val instanceKindSplitEpi: SplitEpi[String, InstanceKind] =
    SplitEpi[String, InstanceKind](
      str => jsonDecode[InstanceKind](str).getOrElse(InstanceKind("Unknown")),
      _.asJson.noSpaces
    )
  private val priceAmountSplitEpi: SplitEpi[String, PriceAmount] =
    SplitEpi[String, PriceAmount](
      str => jsonDecode[PriceAmount](str).getOrElse(PriceAmount(0)),
      _.asJson.noSpaces
    )

  private val instancePriceCodec: RedisCodec[InstanceKind, PriceAmount] =
    Codecs.derive(RedisCodec.Utf8, instanceKindSplitEpi, priceAmountSplitEpi)


  def make[F[_]: Async: Logger: Log](config: Config): Resource[F, AppResources[F]] = {

    def checkRedisConnection(redis: RedisCommands[F, InstanceKind, PriceAmount]): F[Unit] =
      redis.info.flatMap {
        _.get("redis_version").traverse_ { v =>
          Logger[F].info(s"Connected to Redis $v")
        }
      }

    def mkRedisResource(config: RedisConfig): Resource[F, RedisCommands[F, InstanceKind, PriceAmount]] =
      Redis[F].simple(s"${config.host}:${config.port}", instancePriceCodec).evalTap(checkRedisConnection)

    for {
      httpClient <- EmberClientBuilder.default[F].build
      redis <- mkRedisResource(config.redis)
    } yield new AppResources[F](httpClient, redis) {}
  }


}
