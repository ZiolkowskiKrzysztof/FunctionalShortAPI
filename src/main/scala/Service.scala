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

import scala.util.Try

object Service extends Http4sDsl[IO] {

  implicit val simpleDirectoryDecoder: Decoder[SimpleDirectory] =
    deriveDecoder[SimpleDirectory]
  implicit val directoryEncoder: Encoder[Directory] = deriveEncoder[Directory]

  object URLVar {
    def unapply(str: String): Option[String] = {
      if (str.nonEmpty)
        Some(str)
      else
        None
    }
  }

  def routes(ref: Ref[IO, Seq[Directory]]): HttpRoutes[IO] =
    HttpRoutes
      .of[IO] {
        case req @ POST -> Root / "api" / "shorten" =>
          for {
            data <- req.as[SimpleDirectory]
            _ <- ref.getAndUpdate(_ :+ Directory(data.uri, data.generateShort))
            response <- Ok(Directory(data.uri, data.generateShort))
          } yield response

        case GET -> Root / "api" / URLVar(shortURL) =>
          ref.get.map(_.filter(_.shortURL.get == shortURL)).map {
            case Seq(dir) => Response(Status.Ok).withEntity(dir)
            case _        => Response(Status.BadRequest)
          }

        case GET -> Root / "api" / "stats" => Ok(ref.get)

        case GET -> Root / "api" / "stats" / URLVar(fullURL) =>
          ref.get.map(_.filter(_.fullURL == fullURL)).map {
            case Seq(dir) =>
              val counts = Counts(dir.hits)
              Response(Status.Ok).withEntity(counts)
            case _ => Response(Status.BadRequest)
          }
      }

}
