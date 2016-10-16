package redica.client

import java.net._
import java.nio.channels._
import java.nio.channels.spi.SelectorProvider
import java.util.concurrent.TimeUnit

import redica.client.io.exceptions.RedicaConnectionException
import redica.client.io.strategies.async.RedisAsyncIo
import redica.client.io.strategies.blocking.RedisBlockingIo
import redica.client.io.strategies.nonblocking.RedisNonBlockingIo

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

object RedisClientFactory {
  val defaultChannels = 5 // not used on blocking io
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

  def connectAsync(host: String, port: Int, connectionCount: Int = defaultChannels): RedisClient = {
    try {
      val futureChans = (1 to defaultChannels).map(_ => connectedAsyncChannel(host, port))
      val channels = futureChans.map { fc => Await.result(fc, Duration(socketConnectTimeout.toLong, TimeUnit.MILLISECONDS)) }
      val asyncIo = new RedisAsyncIo(channels)
      asyncIo.start()
      new RedisClient(asyncIo)
    } catch {
      case NonFatal(e) => throw new RedicaConnectionException("connection failed.", e)
    }
  }

  private def connectedAsyncChannel(host: String, port: Int): Future[AsynchronousSocketChannel] = {
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

  def connectNonBlocking(host: String, port: Int, connectionCount: Int = defaultChannels): RedisClient = {
    try {
      val channels = (1 to defaultChannels).map { _ =>
        val channel = SocketChannel.open(new InetSocketAddress(host, port))
        channel.configureBlocking(false)
      }
      val nonBlockingIo = new RedisNonBlockingIo(SelectorProvider.provider().openSelector(), channels)
      nonBlockingIo.start()
      new RedisClient(nonBlockingIo)
    } catch {
      case NonFatal(e) => throw new RedicaConnectionException("connection failed.", e)
    }
  }
}
