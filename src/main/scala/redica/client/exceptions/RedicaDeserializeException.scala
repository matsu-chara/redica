package redica.client.exceptions

import redica.exceptions.RedicaException

/**
  * throw when Deserialize Failed
  */
class RedicaDeserializeException(message: String, cause: Throwable) extends RedicaException(message, cause) {
  def this(message: String) = this(message, null)
}

