import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class DirectoryTest extends AnyFlatSpec with should.Matchers {

  val fullURL = "google.com"
  val shortURL = "g.com"

  "Directory" should "be able to create instance with shorten url" in {
    val d1 = Directory(fullURL, None, 0)
    d1.hits should be(0L)

    val d2 = d1.shorten
    d2.shortURL shouldNot be(None)
    d2.hits should be(1L)
  }

  "Directory" should "have possibility to 'store' and increase hits value" in {
    val d1 = Directory(fullURL, Some(shortURL), 0)
    d1.hits should be(0L)

    val d2 = d1.next
    d2.fullURL should be(fullURL)
    d2.shortURL shouldNot be(None)
    d2.hits should be(1L)
  }
}
