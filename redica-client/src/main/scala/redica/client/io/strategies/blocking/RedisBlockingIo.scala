package redica.client.io.strategies.blocking

import java.io.{BufferedInputStream, InputStream, OutputStream}
import java.net.Socket

import redica.client.io.protocols.InputStreamWrapper
import redica.client.io.protocols.reply._
import redica.client.io.strategies.RedisIo
import redica.exceptions.RedicaException

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * RedisIo with Blocking java.io API
  *
  * @param socket
  */
private[client] class RedisBlockingIo(protected val socket: Socket) extends RedisIo {
  private val out: OutputStream = socket.getOutputStream
  private val in: InputStream = new BufferedInputStream(socket.getInputStream)

  /**
    * returns Future. but all request will be blocked, and synchronized
    */
  override def send(data: Array[Byte]): Future[Array[Byte]] = {
    synchronized {
      val res = Try {
        out.write(data)
        out.flush()
      }

      val decoded = res.flatMap { _ =>
        ReplyParser.parse(new InputStreamWrapper(in)) match {
          case ReplySuccess(v) => Success(v)
          case ReplyFailed(e) => Failure(e)
          case ReplyInProgress(p) => Failure(new RedicaException("invalid partial response is returned."))
        }
      }
      Future.fromTry(decoded)
    }
  }

  override def close(): Unit = socket.close()
}
