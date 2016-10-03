package redica.util

object StringParseUtil {
  /**
    * toInt with try-catch
    */
  def safeParseInt(s: String): Either[NumberFormatException, Int] = try {
    Right(s.toInt)
  } catch {
    case e: NumberFormatException => Left(e)
  }
}
