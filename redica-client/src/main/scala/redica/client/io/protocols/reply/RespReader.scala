package redica.client.io.protocols.reply

import java.nio.ByteBuffer

import redica.client.io.protocols.ArrayByteOrInputStreamWrapper

import scala.collection.mutable

/**
  * low level resp parser
  */
private[reply] class RespReader {

  /**
    * read while \r\n found
    *
    * @return a line data (it contains \r\n)
    */
  def readLine(in: ArrayByteOrInputStreamWrapper): ReplyResult[Array[Byte]] = {
    /*
     * 0: not in delimtier
     * 1: in delimiterPos (read 13)
     * 2: detect delimiterPos (read 13 => 10)
     */
    var delimiterPos = 0

    val reply = mutable.ArrayBuilder.make[Byte]()
    while (delimiterPos < 2) {
      val current = in.read()

      // end of stream
      if (current == -1) {
        return ReplyInProgress(reply.result())
      }

      reply += current

      delimiterPos match {
        // found "\r", forward.
        case 0 if current == 13 =>
          delimiterPos = 1

        // found "\r\n", finish.
        case 1 if current == 10 =>
          delimiterPos = 2

        // "not delimiter bytes" found.
        case 0 =>
          delimiterPos = 0

        // "not delimiter bytes" found.
        case 1 =>
          delimiterPos = 0

        case _ =>
          throw new IllegalStateException("implementation error")
      }
    }
    ReplySuccess(reply.result())
  }

  /**
    * read n bytes
    */
  def readBytes(in: ArrayByteOrInputStreamWrapper, n: Int): ReplyResult[Array[Byte]] = {
    var count = 0
    val buf = ByteBuffer.allocate(n)
    while (count < n) {
      val next = in.read()
      if (next == -1) {
        return ReplyInProgress(buf.array().take(count))
      }
      buf.put(next)
      count = count + 1
    }
    ReplySuccess(buf.array())
  }
}


trait UseRespReader {
  val respReader: RespReader
}

trait MixInRespReader {
  val respReader = new RespReader
}
