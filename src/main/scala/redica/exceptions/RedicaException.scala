package redica.exceptions

/**
  * RootException
  */
class RedicaException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  def this(message: String) = this(message, null)
}
