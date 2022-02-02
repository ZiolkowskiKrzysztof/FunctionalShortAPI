case class Directory(fullURL: String,
                     shortURL: Option[String],
                     hits: Long = 0) {
  def next: Directory = this.copy(hits = hits + 1)

  def shorten: Directory = {
    val short = java.util.Base64.getEncoder.encode(fullURL.getBytes()).toString
    Directory(fullURL, Option(short), 1)
  }

}
