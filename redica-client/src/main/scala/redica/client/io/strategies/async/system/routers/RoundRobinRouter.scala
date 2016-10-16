package redica.client.io.strategies.async.system.routers

import java.util.concurrent.atomic.AtomicLong

import redica.client.io.strategies.async.system.worker.Worker
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncProducer.WriteRequest

/**
  * decide next worker for send job
  */
class RoundRobinRouter(workers: Seq[Worker]) {
  private val nextProducerIndex = new AtomicLong(0)

  def receive(request: WriteRequest): Unit = {
    val index = (nextProducerIndex.getAndIncrement % workers.size).toInt // index might be negative when overflow
    val worker = workers(if (index < 0) workers.size + index - 1 else index)
    worker.receive(request)
  }
}
