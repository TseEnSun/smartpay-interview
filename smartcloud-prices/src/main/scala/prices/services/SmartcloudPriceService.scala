package prices.services

import cats.implicits._
import cats.effect._

import prices.data._
import prices.client.SmartCloudClient


object SmartcloudInstancePriceService {
  
  def make[F[_]: Sync](client: SmartCloudClient[F]): InstancePriceService[F] = 
    
    new InstancePriceService[F] {

      override def getPrice(instanceKind: InstanceKind): F[InstancePrice] =
        for {
          smartcloudPrice <- client.getPrice(instanceKind)
        } yield InstancePrice(
          instanceKind,
          PriceAmount(smartcloudPrice.price),
        ) 
    }
}
