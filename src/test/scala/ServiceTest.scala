import org.scalatest.flatspec.AnyFlatSpec
import cats.effect._
import cats.effect.unsafe.IORuntime
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.circe.jsonDecoder
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.Method.POST
import org.http4s.client.Client

class ServiceTest extends AnyFlatSpec {
  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  implicit val SimpleDirectoryDecoder: Decoder[SimpleDirectory] =
    deriveDecoder[SimpleDirectory]
  implicit val SimpleDirectoryEncoder: Encoder[SimpleDirectory] =
    deriveEncoder[SimpleDirectory]

  implicit val DirectoryDecoder: Decoder[Directory] = deriveDecoder[Directory]
  implicit val DirectoryEncoder: Encoder[Directory] = deriveEncoder[Directory]

  val testSimpleDirectory: SimpleDirectory = SimpleDirectory("test")
  val testBodyJson: Json = testSimpleDirectory.asJson

  val testDirectory = Some(Directory(testSimpleDirectory.uri,
                                     testSimpleDirectory.generateShort).asJson)

  def check[A](actual: IO[Response[IO]],
               expectedStatus: Status,
               expectedBody: Option[A])(
      implicit ev: EntityDecoder[IO, A]
  ): Boolean = {
    val actualResp = actual.unsafeRunSync()
    val statusCheck = actualResp.status == expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](
      // Verify Response's body is empty.
      actualResp.body.compile.toVector.unsafeRunSync().isEmpty)(
      expected => actualResp.as[A].unsafeRunSync() == expected
    )
    statusCheck && bodyCheck
  }

  "POST /api/shorten" should "create a new short" in {
    val request: Request[IO] =
      Request(method = Method.POST, uri = uri"/api/shorten")
        .withEntity(testBodyJson)

    val init: Ref[IO, Seq[Directory]] =
      Ref.unsafe(Seq.empty)

    val response =
      Service
        .routes(init)
        .orNotFound
        .run(request)

    assert(check[Json](response, Status.Ok, testDirectory))
  }

  "GET /api/stats" should "return the full list of shorten urls" in {
    ???
  }

  "GET /api/stats/<short>" should "response with number of hits" in {
    ???
  }

  "GET /api/<short>" should "redirects to original URI" in {
    ???
  }

}
