package redica.client.io.exceptions

import redica.exceptions.RedicaException

/**
  * throw when redis reply does not follows specification (or wrong parser implementation...)
  */
class RedicaProtocolException(message: String, cause: Throwable) extends RedicaException(message, cause) {
  def this(message: String) = this(message, null)
}
