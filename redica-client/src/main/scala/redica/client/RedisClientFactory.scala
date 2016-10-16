package redica.client

import java.net._
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}
import java.util.concurrent.TimeUnit

import redica.client.io.exceptions.RedicaConnectionException
import redica.client.io.strategies.async.RedisAsyncIo
import redica.client.io.strategies.blocking.RedisBlockingIo

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

object RedisClientFactory {
  val defaultAsyncChannels = 5
  val socketConnectTimeout = 3000
  val socketReadTimeout = 2000

  def connect(host: String, port: Int): RedisClient = {
    try {
      val socket = new Socket()
      socket.connect(new InetSocketAddress(host, port), socketConnectTimeout)
      socket.setSoTimeout(socketReadTimeout)
      socket.setKeepAlive(true)
      socket.setTcpNoDelay(true)

      new RedisClient(new RedisBlockingIo(socket))
    } catch {
      case NonFatal(e) => throw new RedicaConnectionException("connection failed.", e)
    }
  }

  def connectAsync(host: String, port: Int, connectionCount: Int = defaultAsyncChannels): RedisClient = {
    try {
      val futureChans = (1 to defaultAsyncChannels).map(_ => connectedChannel(host, port))
      val channels = futureChans.map { fc => Await.result(fc, Duration(socketConnectTimeout.toLong, TimeUnit.MILLISECONDS)) }
      val asyncIo = new RedisAsyncIo(channels)
      asyncIo.start()
      new RedisClient(asyncIo)
    } catch {
      case NonFatal(e) => throw new RedicaConnectionException("connection failed.", e)
    }
  }

  private def connectedChannel(host: String, port: Int): Future[AsynchronousSocketChannel] = {
    val chan = AsynchronousSocketChannel.open()
    chan.setOption[java.lang.Boolean](StandardSocketOptions.SO_KEEPALIVE, true)
    chan.setOption[java.lang.Boolean](StandardSocketOptions.TCP_NODELAY, true)

    val connectPromise = Promise[AsynchronousSocketChannel]()
    chan.connect(new InetSocketAddress(host, port), (), new CompletionHandler[Void, Unit] {
      override def completed(result: Void, attachment: Unit): Unit = {
        connectPromise.success(chan)
        ()
      }

      override def failed(exc: Throwable, attachment: Unit): Unit = {
        connectPromise.failure(exc)
        ()
      }
    })
    connectPromise.future
  }
}
