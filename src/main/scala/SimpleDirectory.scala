case class SimpleDirectory(uri: String) {
  def generateShort: Option[String] = Some(uri + "SHORT")
}
