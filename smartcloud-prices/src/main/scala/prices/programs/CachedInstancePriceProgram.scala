package prices.programs

import cats.implicits._
import cats.effect.Sync
import cats.effect.std.Semaphore
import org.typelevel.log4cats.Logger
import prices.config.Config.RedisConfig
import prices.services.InstancePriceService
import prices.services.CacheService
import prices.data.{InstanceKind, InstancePrice}
import prices.programs.InstancePriceProgram._


object  CachedInstancePriceProgram {

  def make[F[_]: Sync: Logger](
    instancePriceService: InstancePriceService[F],
    cache: CacheService[F],
    semaphore: Semaphore[F],
    config: RedisConfig
  ): InstancePriceProgram[F] = new InstancePriceProgram[F] {

    override def getPrice(instance: InstanceKind): F[Either[Exception, InstancePrice]] =
      for {
        _ <- semaphore.acquire
        cachePrice <- cache.get(instance)
        result <- cachePrice match {
          case Some(price) => InstancePrice(instance, price).asRight[Exception].pure[F]
          case None => instancePriceService.getPrice(instance).map(_.left.map(toProgramException))
        }
        _ <- result match {
          case Right(instancePrice) => cache.set(instance, instancePrice.amount, config.expiredTime)
          case Left(_) => Logger[F].warn("Cannot get data from service, cache nothing.")
        }
        _ <- semaphore.release
      } yield result

  }
}
