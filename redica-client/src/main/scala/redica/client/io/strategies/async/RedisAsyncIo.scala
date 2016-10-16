package redica.client.io.strategies.async

import java.nio.channels.AsynchronousSocketChannel

import redica.client.io.strategies.RedisIo
import redica.client.io.strategies.async.system.RootSystem
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncProducer.WriteRequest
import redica.util.ThreadUtil

import scala.concurrent._

/**
  * nio async (experimental impl. no tested)
  */
private[client] class RedisAsyncIo(channels: Seq[AsynchronousSocketChannel]) extends RedisIo {

  val rootSystem = new RootSystem(channels)

  val systemExecutor = ThreadUtil.singleDaemonThreadExecutor

  def start(): Unit = {
    systemExecutor.submit(rootSystem)
    ()
  }

  override def send(data: Array[Byte]): Future[Array[Byte]] = {
    // resolved when reply completely parsed
    val requestPromise = Promise[Array[Byte]]()

    val writeReq = WriteRequest(data, requestPromise)
    rootSystem.receive(writeReq)

    requestPromise.future
  }

  override def close(): Unit = {
    systemExecutor.shutdownNow()
    channels.foreach(_.close())
  }
}
