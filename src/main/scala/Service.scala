import cats.effect._
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl

object Service extends Http4sDsl[IO] {

  implicit val simpleDirectoryDecoder: Decoder[SimpleDirectory] =
    deriveDecoder[SimpleDirectory]
  implicit val directoryEncoder: Encoder[Directory] = deriveEncoder[Directory]

  def routes(ref: Ref[IO, Seq[Directory]]): HttpRoutes[IO] =
    HttpRoutes
      .of[IO] {
        case req @ POST -> Root / "api" / "shorten" =>
          for {
            data <- req.as[SimpleDirectory]
            _ <- ref.getAndUpdate(_ :+ Directory(data.uri, data.generateShort))
            response <- Ok(Directory(data.uri, data.generateShort))
          } yield response
      }

}
