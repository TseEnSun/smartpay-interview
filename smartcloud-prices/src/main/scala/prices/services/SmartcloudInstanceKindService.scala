package prices.services

import cats.implicits._
import cats.effect._

import prices.data._
import prices.client.SmartCloudClient

object SmartcloudInstanceKindService {
  def dummy[F[_]: Concurrent] = new InstanceKindService[F] {

    override def getAll(): F[List[InstanceKind]] =
      List("sc2-micro", "sc2-small", "sc2-medium")
        .map(InstanceKind(_))
        .pure[F]

  }

  def make[F[_]: Concurrent](client: SmartCloudClient[F]) = new InstanceKindService[F] {

    override def getAll(): F[List[InstanceKind]] =
      client.getInstanceKinds().map(_.map(InstanceKind(_)))
        
  }

}
