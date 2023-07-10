package prices.services

import prices.data._

trait InstanceKindService[F[_]] {
  def getAll: F[Either[Exception, List[InstanceKind]]]
}

