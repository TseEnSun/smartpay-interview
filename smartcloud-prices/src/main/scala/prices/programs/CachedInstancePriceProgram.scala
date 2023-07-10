package prices.programs

import cats.implicits._
import cats.effect.Sync
import cats.effect.std.Semaphore

import prices.services.InstancePriceService
import prices.services.CacheService
import prices.data.{InstanceKind, InstancePrice}
import prices.programs.InstancePriceProgram._


object  CachedInstancePriceProgram {

  def make[F[_]: Sync](
    instancePriceService: InstancePriceService[F],
    cache: CacheService[F],
    semaphore: Semaphore[F]
  ): InstancePriceProgram[F] = new InstancePriceProgram[F] {

    override def getPrice(instance: InstanceKind): F[Either[InstancePriceProgram.Exception, InstancePrice]] =
      for {
        _ <- semaphore.acquire
        cachePrice <- cache.get(instance)
        result <- cachePrice match {
          case Some(price) => InstancePrice(instance, price).asRight[InstancePriceProgram.Exception].pure[F]
          case None => instancePriceService.getPrice(instance).map(_.left.map(toProgramException))
        }
        _ <- semaphore.release
      } yield result

  }
}
