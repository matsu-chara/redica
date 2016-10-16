package redica.client.io.strategies.nonblocking

import java.nio.ByteBuffer
import java.nio.channels.{SelectableChannel, SelectionKey, Selector, SocketChannel}
import java.util.concurrent.LinkedBlockingQueue

import redica.client.io.protocols.ArrayByteWrapper
import redica.client.io.protocols.reply.{ReplyFailed, ReplyInProgress, ReplyParser, ReplySuccess}

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

class RedisSelector(selector: Selector, channels: Seq[SelectableChannel]) extends Runnable {

  private val READ_CAPACITY = 10

  private val writeRequestQueue = new LinkedBlockingQueue[RequestState]
  private val channelQueue = new LinkedBlockingQueue[SelectableChannel]
  channels.foreach(c => channelQueue.put(c))

  def select(): Unit = {
    selector.select()
    val selectedKeys = selector.selectedKeys()
    selectedKeys.asScala.foreach { key =>
      if (key.isWritable) {
        val state = RedisSelector.toState(key.attachment())
        try {
          val chan = RedisSelector.toChan(key.channel())
          chan.write(ByteBuffer.wrap(state.sendData))
          chan.register(selector, SelectionKey.OP_READ, state)
        } catch {
          case NonFatal(e) => state.requestPromise.failure(e)
        }
      }

      if (key.isReadable) {
        val state = RedisSelector.toState(key.attachment())
        try {
          val chan = RedisSelector.toChan(key.channel())

          val bufferAndInputStream = ArrayByteWrapper.empty
          state.replyResult.inProgressForeach { bytes =>
            bufferAndInputStream.prepend(bytes)
          }

          val readBuffer = ByteBuffer.allocate(READ_CAPACITY)
          val readBytes = chan.read(readBuffer)
          bufferAndInputStream.append(readBuffer.array().take(readBytes))
          ReplyParser.parse(bufferAndInputStream) match {
            case ReplySuccess(value: Array[Byte]) =>
              state.requestPromise.success(value)
              key.interestOps(0)
              channelQueue.put(chan)
            case ReplyInProgress(data: Array[Byte]) =>
              state.update(data)
              chan.register(selector, SelectionKey.OP_READ, state)
            case ReplyFailed(e) =>
              state.requestPromise.failure(e)
              key.interestOps(0)
              channelQueue.put(chan)
          }
        } catch {
          case NonFatal(e) => state.requestPromise.failure(e)
        }
      }
    }
    selectedKeys.clear()
  }

  override def run(): Unit = {
    while (true) {
      if (channelQueue.isEmpty) {
        // all channels are already used
      } else if (writeRequestQueue.isEmpty && channelQueue.size() < channels.size) {
        // nothing to do. And we have processing task
      } else {
        // we have task and channel
        val chan = channelQueue.take()
        val state = writeRequestQueue.take()
        chan.register(selector, SelectionKey.OP_WRITE).attach(state)
      }
      select()
    }
  }


  def request(data: Array[Byte]) = {
    val state = new RequestState(data)
    writeRequestQueue.put(state)
    state.requestPromise.future
  }
}

object RedisSelector {
  def toChan(channel: SelectableChannel): SocketChannel = channel.asInstanceOf[SocketChannel]

  def toState(attach: AnyRef): RequestState = attach.asInstanceOf[RequestState]
}

