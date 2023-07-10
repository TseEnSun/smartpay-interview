package prices.services

import scala.concurrent.duration.FiniteDuration

import prices.data._


trait CacheService[F[_]] {
  def get(key: InstanceKind): F[Option[PriceAmount]]
  def set(key: InstanceKind, value: PriceAmount, expiresIn: FiniteDuration): F[Unit]
  def setMany(kvs: Map[InstanceKind, PriceAmount], expiresIn: FiniteDuration): F[Unit]
}

