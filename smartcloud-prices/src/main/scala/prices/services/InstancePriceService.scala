package prices.services

import prices.services.errors._
import prices.data._

trait InstancePriceService[F[_]] {
  def getPrice(instance: InstanceKind): F[Either[Exception, InstancePrice]]
}
