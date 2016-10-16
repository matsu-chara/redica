package redica.client.io.strategies.async.system.worker.workers

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

import redica.client.io.strategies.async.system.RootScheduler
import redica.client.io.strategies.async.system.worker.Worker
import redica.client.io.strategies.async.system.worker.Worker.WorkerRequest
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncConsumer.ReadRequest
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncProducer.WriteRequest

import scala.concurrent.Promise
import scala.util.control.NonFatal

object RedisAsyncProducer {
  case class WriteRequest(data: Array[Byte], requestPromise: Promise[Array[Byte]]) extends WorkerRequest
}

class RedisAsyncProducer(channel: AsynchronousSocketChannel, consumer: RedisAsyncConsumer, protected val rootScheduler: RootScheduler) extends Worker {
  private val producerQueue = new LinkedBlockingQueue[WriteRequest]

  private val writing = new AtomicBoolean(false)

  override protected val partialReceive: Receive = {
    case write: WriteRequest =>
      producerQueue.put(write)

      // if writing == true, then cast will return immediately (reduce useless scheduling)
      if (!writing.get()) {
        rootScheduler.schedule(this)
      }
  }

  override def cast(): Unit = {
    if (producerQueue.isEmpty) {
      return
    }

    // avoid java.nio.channels.WritePendingException
    if (!writing.compareAndSet(false, true)) {
      return
    }

    val write = producerQueue.take()
    try {
      doWrite(write.data, write.requestPromise)
    } catch {
      case NonFatal(e) =>
        write.requestPromise.failure(e)
        writeFinished()
    }
  }

  private def doWrite(data: Array[Byte], requestPromise: Promise[Array[Byte]]): Unit = {
    channel.write(ByteBuffer.wrap(data), (), new CompletionHandler[Integer, Unit] {
      override def completed(result: Integer, attachment: Unit): Unit = {
        /*
         * redis response order is starting write order (not completionHandler invoked order).
         * but, this class run on single thread and blocking for writeComplete, so we can put promise in completionHandler.
         *
         * we can avoid causing IllegalStateException("promise already completed") when write operation failed by put promise here.
         */
        consumer.receive(ReadRequest(requestPromise))
        writeFinished()
      }

      override def failed(exc: Throwable, attachment: Unit): Unit = {
        requestPromise.failure(exc)
        writeFinished()
      }
    })
  }

  private def writeFinished(): Unit = {
    writing.set(false)
    rootScheduler.schedule(this)
  }
}
