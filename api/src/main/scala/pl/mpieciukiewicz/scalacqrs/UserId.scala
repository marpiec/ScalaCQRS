package pl.mpieciukiewicz.scalacqrs

object UserId {

  val ZERO: UserId = new UserId(0L)

  def parseOrZero(str: String): UserId = {
    try {
      new UserId(str.toLong)
    } catch {
      case e: NumberFormatException => ZERO
    }
  }

}

case class UserId(uid: Long) {
  override def toString = uid.toString
}