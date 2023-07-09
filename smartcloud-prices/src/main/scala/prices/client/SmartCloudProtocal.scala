package prices.client

import io.circe._
import io.circe.generic.semiauto._


object SmartCloudProtocal {

  final case class SmartCloudPrice(
    kind: String,
    price: Double,
    timestamp: String
  )

  implicit val smartCloudPriceDecoder: Decoder[SmartCloudPrice] = deriveDecoder
  
}
