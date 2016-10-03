package redica.client.io

import java.io.{BufferedInputStream, InputStream, OutputStream}
import java.net.Socket

import redica.exceptions.RedicaException

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * RedisIo with Blocking java.io API
  * @param socket
  */
private[client] class RedisBlockingIo(protected val socket: Socket) extends RedisIo {
  private val in: InputStream = new BufferedInputStream(socket.getInputStream)
  private val out: OutputStream = socket.getOutputStream

  override def send[V](data: Array[Byte])(replyDecoder: (InputStream) => Either[RedicaException, V]): Future[V] = {
    val res = Try {
      out.write(data)
      out.flush()
    }

    val decoded = res.flatMap { _ =>
      replyDecoder(in) match {
        case Right(v) => Success(v)
        case Left(e) => Failure(e)
      }
    }
    Future.fromTry(decoded)
  }

  override def close(): Unit = socket.close()
}
