package redica.client.io.strategies.async.system

import java.nio.channels.AsynchronousSocketChannel

import redica.client.io.strategies.async.system.routers.RoundRobinRouter
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncProducer.WriteRequest
import redica.client.io.strategies.async.system.worker.workers.{RedisAsyncConsumer, RedisAsyncProducer}

class RootSystem(channels: Seq[AsynchronousSocketChannel]) extends Runnable {
  val rootScheduler = new RootScheduler

  val producerRouter = {
    val producers = channels.map { chan =>
      val consumer = new RedisAsyncConsumer(chan, rootScheduler)
      new RedisAsyncProducer(chan, consumer, rootScheduler)
    }
    new RoundRobinRouter(producers)
  }

  override def run(): Unit = {
    while (true) {
      rootScheduler.call()
    }
  }

  def receive(write: WriteRequest): Unit = {
    producerRouter.receive(write)
  }
}
