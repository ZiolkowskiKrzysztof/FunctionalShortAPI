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

class ServiceTest extends AnyFlatSpec {
  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  implicit val SimpleDirectoryDecoder: Decoder[SimpleDirectory] =
    deriveDecoder[SimpleDirectory]
  implicit val SimpleDirectoryEncoder: Encoder[SimpleDirectory] =
    deriveEncoder[SimpleDirectory]

  implicit val DirectoryDecoder: Decoder[Directory] = deriveDecoder[Directory]
  implicit val DirectoryEncoder: Encoder[Directory] = deriveEncoder[Directory]

  implicit val CountsEncoder: Encoder[Counts] = deriveEncoder[Counts]

  val testSimpleDirectory: SimpleDirectory = SimpleDirectory("test")
  val testBodyJson: Json = testSimpleDirectory.asJson

  val testDirectory
    : Option[Json] = Some(Directory(testSimpleDirectory.uri,
                                    testSimpleDirectory.generateShort).asJson)

  val directory1: Directory = Directory("fullURL", Option("shortURL"), 1)
  val directory2: Directory = Directory("fullURL2", Option("shortURL2"), 2)

  val directoriesJson: Json = Seq(directory1, directory2).asJson

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
    val request: Request[IO] =
      Request(method = Method.GET, uri = uri"/api/stats")

    val init: Ref[IO, Seq[Directory]] =
      Ref.unsafe(Seq(directory1, directory2))

    val response =
      Service
        .routes(init)
        .orNotFound
        .run(request)

    assert(check[Json](response, Status.Ok, Option(directoriesJson)))
  }

  "GET /api/stats/<short>" should "response with number of hits" in {
    val request: Request[IO] =
      Request(method = Method.GET, uri = uri"/api/stats/fullURL")

    val init: Ref[IO, Seq[Directory]] =
      Ref.unsafe(Seq(directory1, directory2))

    val expectedResponse = Counts(1).asJson

    val response =
      Service
        .routes(init)
        .orNotFound
        .run(request)

    assert(check[Json](response, Status.Ok, Option(expectedResponse)))
  }

  "GET /api/<short>" should "redirects to original URI" in {
    val request: Request[IO] =
      Request(method = Method.GET, uri = uri"/api/shortURL")

    val init: Ref[IO, Seq[Directory]] =
      Ref.unsafe(Seq(directory1, directory2))

    val expectedResponse = directory1.asJson

    val response =
      Service
        .routes(init)
        .orNotFound
        .run(request)

    assert(check[Json](response, Status.Ok, Option(expectedResponse)))
  }

}
