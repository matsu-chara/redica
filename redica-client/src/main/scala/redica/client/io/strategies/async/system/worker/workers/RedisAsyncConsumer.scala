package redica.client.io.strategies.async.system.worker.workers

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

import redica.client.io.protocols.ArrayByteWrapper
import redica.client.io.protocols.reply.{ReplyFailed, ReplyInProgress, ReplyParser, ReplySuccess}
import redica.client.io.strategies.async.system.RootScheduler
import redica.client.io.strategies.async.system.worker.Worker
import redica.client.io.strategies.async.system.worker.Worker.WorkerRequest
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncConsumer.ReadRequest

import scala.concurrent.Promise
import scala.util.control.NonFatal

object RedisAsyncConsumer {
  case class ReadRequest(requestPromise: Promise[Array[Byte]]) extends WorkerRequest
}

class RedisAsyncConsumer(channel: AsynchronousSocketChannel, protected val rootScheduler: RootScheduler) extends Worker {

  private val READ_CAPACITY = 16

  private val bufferAndInputStream = ArrayByteWrapper.empty

  private val consumerPromiseQueue = new LinkedBlockingQueue[ReadRequest]

  private val reading = new AtomicBoolean(false)

  override protected val partialReceive: Receive = {
    case read: ReadRequest =>
      consumerPromiseQueue.put(read)

      // if reading == true, then cast will return immediately (reduce useless scheduling)
      if (!reading.get()) {
          rootScheduler.schedule(this)
      }
  }

  override def cast(): Unit = {
    if (consumerPromiseQueue.isEmpty) {
      return
    }

    // avoid java.nio.channels.ReadPendingException
    if (!reading.compareAndSet(false, true)) {
      return
    }

    val read = consumerPromiseQueue.take()
    try {
      resolveResponse(read.requestPromise)
    } catch {
      case NonFatal(e) =>
        read.requestPromise.failure(e)
        readFinished()
    }
  }

  /**
    * first, try parse from buffer. if not enough, then fetch & parse from channel
    */
  private def resolveResponse(promise: Promise[Array[Byte]]): Unit = {
    ReplyParser.parse(bufferAndInputStream) match {
      case ReplySuccess(v) =>
        promise.success(v)
        readFinished()
      case ReplyFailed(e) =>
        promise.failure(e)
        readFinished()
      case ReplyInProgress(p) =>
        bufferAndInputStream.prepend(p)
        readRecursive(promise)
    }
  }

  private def readRecursive(promise: Promise[Array[Byte]]): Unit = {
    val readBuffer = ByteBuffer.allocate(READ_CAPACITY)

    channel.read(readBuffer, (), new CompletionHandler[Integer, Unit] {
      override def completed(result: Integer, attachment: Unit): Unit = {
        val read = readBuffer.array().take(result)
        bufferAndInputStream.append(read)
        ReplyParser.parse(bufferAndInputStream) match {
          case ReplySuccess(v) =>
            promise.success(v)
            readFinished()
          case ReplyFailed(e) =>
            promise.failure(e)
            readFinished()
          case ReplyInProgress(p) =>
            // retry. decoding from start during response is not enough, so it can be improve performance (via skipping already decoded part?)
            bufferAndInputStream.prepend(p)
            readRecursive(promise)
        }
      }

      override def failed(exc: Throwable, attachment: Unit): Unit = {
        promise.failure(exc)
        readFinished()
      }
    })
  }

  private def readFinished(): Unit = {
    reading.set(false)
    rootScheduler.schedule(this)
  }
}
