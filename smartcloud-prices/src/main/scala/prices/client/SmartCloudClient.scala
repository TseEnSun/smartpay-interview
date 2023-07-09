package prices.client

import scala.util.control.NoStackTrace

import cats.syntax.all._
import cats.effect._
import org.http4s._
import org.http4s.Method._
import org.http4s.client._
import org.http4s.headers._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._

import prices.data._
import prices.config.Config._
import SmartCloudProtocal._


trait SmartCloudClient[F[_]] {
  
  def getInstanceKinds(): F[List[String]]
  def getPrice(instance: InstanceKind): F[SmartCloudPrice]
  
}

object SmartCloudClient {

  sealed trait Exception extends NoStackTrace
  case class APICallFailure(message: String) extends Exception
  

  def make[F[_]: Concurrent](
    config: SmartcloudConfig,
    client: Client[F]
  ): SmartCloudClient[F] = new SmartCloudClient[F] with Http4sClientDsl[F] {

    implicit val decoder: EntityDecoder[F, List[String]] = jsonOf[F, List[String]]

    def getInstanceKinds(): F[List[String]] = 
      Uri.fromString(s"${config.baseUri}/instances").liftTo[F].flatMap { uri =>
        val request = GET(uri, Authorization(Credentials.Token(AuthScheme.Bearer, config.token)))
        client.run(request).use { response =>
          response.status match {
            case Status.Ok => response.as[List[String]]
            case st => 
              APICallFailure(st.reason).raiseError[F, List[String]]
          }
        }
      }
      
    def getPrice(instance: InstanceKind): F[SmartCloudPrice] = 
      Uri.fromString(s"${config.baseUri}/instances/${instance.getString}").liftTo[F].flatMap { uri =>
        val request = GET(uri, Authorization(Credentials.Token(AuthScheme.Bearer, config.token)))
        client.run(request).use { response =>
          response.status match {
            case Status.Ok => response.asJsonDecode[SmartCloudPrice]
            case st => 
              APICallFailure(st.reason).raiseError[F, SmartCloudPrice]
          }
        }
      }
  }
  
}
