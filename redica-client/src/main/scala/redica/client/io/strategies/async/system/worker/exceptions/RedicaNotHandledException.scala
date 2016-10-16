package redica.client.io.strategies.async.system.worker.exceptions

/**
  * throws when request not handled by worker
  */
class RedicaNotHandledException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  def this(message: String) = this(message, null)
}
