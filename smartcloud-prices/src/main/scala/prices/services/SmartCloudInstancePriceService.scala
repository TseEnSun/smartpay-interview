package prices.services

import cats.implicits._
import cats.effect._

import prices.data._
import prices.apiClient.SmartCloudClient


object SmartCloudInstancePriceService {
  
  def make[F[_]: Sync](client: SmartCloudClient[F]): InstancePriceService[F] = new InstancePriceService[F] {

    override def getPrice(instanceKind: InstanceKind): F[Either[Exception, InstancePrice]]=
      client.getPrice(instanceKind).flatMap {
        case Right(smartCloudPrice) =>
          InstancePrice(instanceKind, PriceAmount(smartCloudPrice.price)).asRight[Exception].pure[F]
        case Left(exp) =>
          toServiceException(exp).asLeft[InstancePrice].pure[F]
      }
  }
}
