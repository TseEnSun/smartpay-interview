package prices.apiClient

import io.circe._
import io.circe.generic.semiauto._


object SmartCloudProtocol {

  final case class SmartCloudPrice(
    kind: String,
    price: Double,
    timestamp: String
  )

  implicit val smartCloudPriceDecoder: Decoder[SmartCloudPrice] = deriveDecoder
  
}
