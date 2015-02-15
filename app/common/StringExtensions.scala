package common

object StringExtensions {

  implicit class StringOps(val s: String) extends AnyVal {

    def toIntOpt: Option[Int] = {
      try {
        Some(s.toInt)
      }
      catch {
        case e: Exception => None
      }
    }

    /**
     * Trim leading and trailing whitespace including NBSP from the string
     * @return The trimmed string
     */
    def trimWhitespace: String = {
      s.trim.replaceFirst("^[\\x00-\\x200\\xA0]+", "").replaceFirst("[\\x00-\\x20\\xA0]+$", "")
    }
  }
}
