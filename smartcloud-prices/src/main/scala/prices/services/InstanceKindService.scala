package prices.services

import prices.data._
import prices.services.errors._

trait InstanceKindService[F[_]] {
  def getAll: F[Either[Exception, List[InstanceKind]]]
}

