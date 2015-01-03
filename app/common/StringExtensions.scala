package common

object StringExtensions {

  implicit class StringOps(val s: String) extends AnyVal {
    def toIntOpt: Option[Int] = {
      try {
        Some(s.toInt)
      }
      catch {
        case e:Exception => None
      }
    }
  }
}
