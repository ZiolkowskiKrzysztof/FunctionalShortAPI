case class SimpleDirectory(uri: String) {
  def generateShort: Option[String] =
    Some(java.util.Base64.getEncoder.encode(uri.getBytes()).toString.drop(3))
}
