package prices.data

final case class PriceAmount(value: Double) extends AnyVal

final case class InstancePrice(kind: InstanceKind, amount: PriceAmount)
