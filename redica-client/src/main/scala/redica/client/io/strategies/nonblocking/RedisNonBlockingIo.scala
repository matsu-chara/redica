package redica.client.io.strategies.nonblocking

import java.nio.channels.{SelectableChannel, Selector}

import redica.client.io.strategies.RedisIo
import redica.util.ThreadUtil

import scala.concurrent.Future

class RedisNonBlockingIo(selector: Selector, channels: Seq[SelectableChannel]) extends RedisIo {
  private val systemExecutor = ThreadUtil.singleDaemonThreadExecutor
  private val redisSelector = new RedisSelector(selector, channels)

  def start() = {
    systemExecutor.submit(redisSelector)
  }

  override def send(data: Array[Byte]): Future[Array[Byte]] = {
    redisSelector.request(data)
  }

  override def close(): Unit = {
    systemExecutor.shutdownNow()
    channels.foreach(_.close())
  }
}
