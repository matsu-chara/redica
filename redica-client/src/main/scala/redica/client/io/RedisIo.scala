package redica.client.io

import java.io.InputStream

import redica.exceptions.RedicaException

import scala.concurrent.Future

private[client] trait RedisIo {
  def send[V](data: Array[Byte])(replyDecoder: InputStream => Either[RedicaException, V]): Future[V]
  def close(): Unit
}
