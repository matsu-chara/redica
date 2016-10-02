package redica.client

import java.net.{InetSocketAddress, Socket}

import redica.client.io.RedisBlockingIo
import redica.client.io.exceptions.RedicaConnectionException

import scala.util.control.NonFatal

object RedisClientFactory {
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
}
