case class Directory(fullURL: String,
                     shortURL: Option[String],
                     hits: Long = 0) {

  def next: Directory = this.copy(hits = hits + 1)
  def shorten: Directory = this.copy(shortURL = Option("g.com"), hits = 1)

  def incrementHits(s: Directory): (Directory, Long) = (s.next, s.hits)

  override def toString: String =
    s"fullURL: $fullURL,\n shortURL: $shortURL,\n hits: $hits"
}
