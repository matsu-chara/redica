package redica.client.io.exceptions

import redica.exceptions.RedicaException

/**
  * throw when connection failed or disconnected to redis
  */
class RedicaConnectionException(message: String, cause: Throwable) extends RedicaException(message, cause) {
  def this(message: String) = this(message, null)
}
