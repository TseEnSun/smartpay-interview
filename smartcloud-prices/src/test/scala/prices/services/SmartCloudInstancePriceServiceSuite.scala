package prices.services

import cats.implicits._
import cats.effect._
import munit.CatsEffectSuite
import prices.apiClient.SmartCloudClient
import prices.apiClient.SmartCloudProtocol.SmartCloudPrice
import prices.apiClient.errors.{Exception => ClientException}
import prices.services.errors._
import prices.data.{InstanceKind, InstancePrice, PriceAmount}

class SmartCloudInstancePriceServiceSuite extends CatsEffectSuite {

  def mkSmartCloudClientWithPrice(
    result: Either[ClientException, SmartCloudPrice]
  ): SmartCloudClient[IO] = new SmartCloudClient[IO] {
    override def getInstanceKinds: IO[Either[ClientException, List[String]]] =
      IO.pure(Right(List.empty[String]))
    override def getPrice(
      instance: InstanceKind): IO[Either[ClientException, SmartCloudPrice]] =
      IO.pure(result)
  }

  test("Get right from client should return right") {
    val expectVal: InstancePrice = InstancePrice(InstanceKind("kind"), PriceAmount(1.0))

    SmartCloudInstancePriceService.make[IO](
      mkSmartCloudClientWithPrice(
        SmartCloudPrice(
          kind = expectVal.kind.getString,
          price = expectVal.amount.value,
          timestamp = "???"
        ).asRight[ClientException])
      ).getPrice(expectVal.kind).assertEquals(expectVal.asRight[Exception])
  }

  test ("Get left from client should return left") {
    val expectVal = Exception.APICallFailure("unknown")

    SmartCloudInstancePriceService.make[IO](
      mkSmartCloudClientWithPrice(
        ClientException.APICallFailure(code = 500, expectVal.message).asLeft[SmartCloudPrice])
    ).getPrice(InstanceKind("anyKind")).assertEquals(expectVal.asLeft[InstancePrice])
  }

}
