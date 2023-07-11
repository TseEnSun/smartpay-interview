package prices.routes.protocol

import io.circe._
import io.circe.syntax._

import prices.data._

final case class InstancePriceResponse(
  kind: InstanceKind,
  amount: PriceAmount
)

final case class InstanceKindNotFoundResponse(
  kind: InstanceKind
)

object InstancePriceResponse {
  
  def instancePriceToResponse(instancePrice: InstancePrice): InstancePriceResponse =
    InstancePriceResponse(
      kind = instancePrice.kind,
      amount = instancePrice.amount
    )
  
  implicit val instancePriceResponseEncoder: Encoder[InstancePriceResponse] =
    Encoder.instance[InstancePriceResponse] {
      case InstancePriceResponse(k, a) =>
        Json.obj(
          "kind" -> k.getString.asJson,
          "amount" -> a.value.asJson
        )

    }
}

object InstanceKindNotFoundResponse {

  implicit val instanceKindNotFoundResponseEncoder: Encoder[InstanceKindNotFoundResponse] =
    Encoder.instance[InstanceKindNotFoundResponse] {
      case InstanceKindNotFoundResponse(kind) =>
        Json.obj(
          "kind" -> kind.getString.asJson
        )
    }

}