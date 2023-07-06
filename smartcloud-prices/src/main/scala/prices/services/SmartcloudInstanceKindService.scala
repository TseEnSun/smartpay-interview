package prices.services

import cats.implicits._
import cats.effect._
import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.headers._

import prices.data._

object SmartcloudInstanceKindService {

  final case class Config(
      baseUri: String,
      token: String
  )

  def dummy[F[_]: Concurrent]: InstanceKindService[F] = new DummyInstanceKindService[F]
  def make[F[_]: Concurrent](config: Config, client: Client[F]): InstanceKindService[F] = 
    new SmartcloudInstanceKindService(config, client)

  private final class DummyInstanceKindService[F[_]: Concurrent] extends InstanceKindService[F] {

    override def getAll(): F[List[InstanceKind]] =
      List("sc2-micro", "sc2-small", "sc2-medium")
        .map(InstanceKind(_))
        .pure[F]

  }
  
  private final class SmartcloudInstanceKindService[F[_]: Concurrent](
      config: Config,
      client: Client[F]
  ) extends InstanceKindService[F] {

    implicit val instanceKindsEntityDecoder: EntityDecoder[F, List[String]] = jsonOf[F, List[String]]

    val getAllUri = s"${config.baseUri}/instances"
    val getAllRequest = Request[F](
      method = Method.GET,
      uri = Uri.unsafeFromString(getAllUri),
      headers = Headers(
        Authorization(Credentials.Token(AuthScheme.Bearer, config.token))
      )
    )

    override def getAll(): F[List[InstanceKind]] =
      client
        .expect[List[String]](getAllRequest)
        .map(_.map(InstanceKind(_)))
        
  }

}
