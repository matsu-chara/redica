package redica.client.io.strategies

import scala.concurrent.Future

private[client] trait RedisIo {
  def send(data: Array[Byte]): Future[Array[Byte]]

  def close(): Unit
}
