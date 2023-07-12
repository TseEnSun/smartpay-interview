package prices.programs

import scala.concurrent.duration._
import cats.implicits._
import cats.effect._
import cats.effect.std.Semaphore
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import munit.CatsEffectSuite
import prices.config.Config.RedisConfig
import prices.data._
import prices.services._
import prices.services.errors.{Exception => ServiceException}
import prices.programs.InstancePriceProgram.Exception


class CachedInstancePriceProgramSuite extends CatsEffectSuite {

  implicit val logger: SelfAwareStructuredLogger[IO] = NoOpLogger[IO]
  val dummyRedisConfig: RedisConfig = RedisConfig("0.0.0.0", 6379, 5.seconds)

  def successInstancePriceService(instancePrice: InstancePrice): InstancePriceService[IO] =
    new InstancePriceService[IO] {
      override def getPrice(
        instance: InstanceKind
      ): IO[Either[ServiceException, InstancePrice]] =
        instancePrice.asRight[ServiceException].pure[IO]
    }

  def notFoundInstancePriceService(msg: String): InstancePriceService[IO] =
    new InstancePriceService[IO] {
      override def getPrice(
        instance: InstanceKind
      ): IO[Either[ServiceException, InstancePrice]] =
        ServiceException.NoFoundFailure(msg).asLeft[InstancePrice].pure[IO]
    }

  def failedInstancePriceService(msg: String): InstancePriceService[IO] =
    new InstancePriceService[IO] {
      override def getPrice(
        instance: InstanceKind
      ): IO[Either[ServiceException, InstancePrice]] =
        ServiceException.APICallFailure(msg).asLeft[InstancePrice].pure[IO]
    }


  def successCacheService(price: Double): CacheService[IO] = new CacheService[IO] {
    override def get(
      key: InstanceKind): IO[Option[PriceAmount]] = PriceAmount(price).some.pure[IO]

    override def set(key: InstanceKind, value: PriceAmount, expiresIn: FiniteDuration): IO[Unit] = IO.unit

    override def setMany(kvs: Map[InstanceKind, PriceAmount], expiresIn: FiniteDuration): IO[Unit] = IO.unit
  }

  def failedCacheService(): CacheService[IO] = new CacheService[IO] {
    override def get(key: InstanceKind): IO[Option[PriceAmount]] = None.pure[IO]

    override def set(key: InstanceKind, value: PriceAmount, expiresIn: FiniteDuration): IO[Unit] = IO.unit

    override def setMany(kvs: Map[InstanceKind, PriceAmount], expiresIn: FiniteDuration): IO[Unit] = IO.unit
  }

  test("Cache: O and Service: X then O") {
    val expectVal = InstancePrice(InstanceKind("kind"), PriceAmount(1.0))

    val program = for {
      s <- Semaphore[IO](1)
      program <- CachedInstancePriceProgram.make[IO](
        failedInstancePriceService("something wrong"),
        successCacheService(expectVal.amount.value),
        s,
        dummyRedisConfig
      ).pure[IO]
    } yield program

    program.flatMap(p => p.getPrice(expectVal.kind)).assertEquals(expectVal.asRight[Exception])
  }

  test("Cache: X and Service: O then O") {
    val expectVal = InstancePrice(InstanceKind("kind"), PriceAmount(1.0))

    Semaphore[IO](1).map { s =>
      CachedInstancePriceProgram.make[IO](
        successInstancePriceService(expectVal),
        failedCacheService(),
        s,
        dummyRedisConfig
      )
    }.flatMap { program =>
      program.getPrice(expectVal.kind)
    }.assertEquals(expectVal.asRight[Exception])
  }

  test("Cache: X and Service: X then X") {
    val expectVal = Exception.ProgramFailure("something wrong")

    Semaphore[IO](1).map { s =>
      CachedInstancePriceProgram.make[IO](
        failedInstancePriceService(expectVal.message),
        failedCacheService(),
        s,
        dummyRedisConfig
      )
    }.flatMap { program =>
      program.getPrice(InstanceKind("kind"))
    }.assertEquals(expectVal.asLeft[InstancePrice])

  }
}
