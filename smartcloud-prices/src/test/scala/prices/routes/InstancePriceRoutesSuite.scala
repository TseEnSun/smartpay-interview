package prices.routes

import cats.implicits._
import cats.effect._
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import prices.data._
import prices.programs.InstancePriceProgram
import prices.programs.InstancePriceProgram._

class InstancePriceRoutesSuite extends CatsEffectSuite{

  def successProgram(instancePrice: InstancePrice): InstancePriceProgram[IO] =
    new InstancePriceProgram[IO] {
      override def getPrice(
        instance: InstanceKind
      ): IO[Either[InstancePriceProgram.Exception, InstancePrice]] =
        instancePrice.asRight[Exception].pure[IO]
    }

  def notFoundProgram(errMsg: String): InstancePriceProgram[IO] =
    new InstancePriceProgram[IO] {
      override def getPrice(
        instance: InstanceKind
      ): IO[Either[Exception, InstancePrice]] =
        Exception.InstanceNotFound(errMsg).asLeft[InstancePrice].pure[IO]
    }

  def failedProgram(errMsg: String): InstancePriceProgram[IO] =
    new InstancePriceProgram[IO] {
      override def getPrice(
        instance: InstanceKind
      ): IO[Either[Exception, InstancePrice]] =
        Exception.ProgramFailure(errMsg).asLeft[InstancePrice].pure[IO]
    }

  test("Get right from program should return Ok") {
    val request = Request[IO](
      GET,
      uri"/prices".withQueryParam("kind", "someKind")
    )
    val routes = InstancePriceRoutes[IO](
      successProgram(InstancePrice(InstanceKind("someKind"), PriceAmount(1.0)))
    ).routes
    routes.run(request).value.map{
      case Some(resp) => assert(resp.status.code == 200)
      case None => fail("route not found")
    }
  }

  test("If program return InstanceNotFound should return NotFound") {
    val request = Request[IO](
      GET,
      uri"/prices".withQueryParam("kind", "someKind")
    )
    val routes = InstancePriceRoutes[IO](
      notFoundProgram("something wrong")
    ).routes
    routes.run(request).value.map {
      case Some(resp) => assert(resp.status.code == 404)
      case None => fail("route not found")
    }
  }

  test("Uri without query parameter should not return") {
    val request = Request[IO](
      GET,
      uri"/prices"
    )
    val routes = InstancePriceRoutes[IO](
      successProgram(InstancePrice(InstanceKind("someKind"), PriceAmount(1.0)))
    ).routes
    routes.run(request).value.map(x => assert(x.isEmpty))
  }
}
