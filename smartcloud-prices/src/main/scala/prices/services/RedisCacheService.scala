package prices.services

import scala.concurrent.duration.FiniteDuration

import cats.implicits._
import cats.effect.Sync
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.effect.Log.Stdout._

import prices.data._
import dev.profunktor.redis4cats.effect.Log

object RedisCacheService {
  
  def make[F[_]: Sync](redis: RedisCommands[F, InstanceKind, PriceAmount]): CacheService[F] = new CacheService[F] {
    
    override def get(key: InstanceKind): F[Option[PriceAmount]] = redis.get(key)

    override def set(key: InstanceKind, value: PriceAmount, expiresIn: FiniteDuration): F[Unit] = 
      for {
        _ <- redis.set(key, value)
        _ <- Log[F].debug(s"Setting $key")
        _ <- redis.expire(key, expiresIn)
        _ <- Log[F].debug(s"Setting $key expiration")
      } yield ()

    override def setMany(kvs: Map[InstanceKind, PriceAmount], expiresIn: FiniteDuration): F[Unit] = {
      val expireOps = kvs.keys.toList.map(redis.expire(_, expiresIn).void)
      for {
        _ <- redis.mSet(kvs)
        _ <- Log[F].debug(s"Setting ${kvs.size} keys")
        _ <- redis.pipeline_(expireOps)
        _ <- Log[F].debug(s"Setting ${kvs.size} expirations")
      } yield ()

    }
  }
}
