package prices.services

import cats.effect._
import cats.implicits._
import prices.data._
import prices.services.errors._
import prices.apiClient.SmartCloudClient

object SmartCloudInstanceKindService {
  def dummy[F[_]: Concurrent]: InstanceKindService[F] = new InstanceKindService[F] {

    override def getAll: F[Either[Exception, List[InstanceKind]]] =
      List("sc2-micro", "sc2-small", "sc2-medium")
        .map(InstanceKind)
        .asRight[Exception]
        .pure[F]

  }

  def make[F[_]: Concurrent](client: SmartCloudClient[F]): InstanceKindService[F] = new InstanceKindService[F] {

    override def getAll: F[Either[Exception, List[InstanceKind]]] =
      for {
        kinds <- client.getInstanceKinds
        result <- kinds match {
          case Right(strKinds) => strKinds.map(InstanceKind).asRight[Exception].pure[F]
          case Left(exp) => toServiceException(exp).asLeft[List[InstanceKind]].pure[F]
        }
      } yield result
        
  }

}
