package redica.testutils

import redica.client.io.strategies.async.system.worker.workers.RedisAsyncProducer.WriteRequest

import scala.concurrent.Promise

object WriteRequestUtil {
  def empty = WriteRequest(Array.emptyByteArray, Promise[Array[Byte]]())
}
