package redica.client.io.protocols.reply

import java.io.InputStream
import java.nio.ByteBuffer

import redica.client.io.exceptions.RedicaConnectionException

import scala.collection.mutable

/**
  * low level resp parser
  */
private[reply] class RespReader {

  /**
    * read while \r\n found
    * @return a line data (it does not contain \r\n)
    */
  def readLine(in: InputStream): Array[Byte] = {
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
        throw new RedicaConnectionException("connection close?")
      }

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
          reply += current.toByte

        // "not delimiter bytes" found.
        case 1 =>
          delimiterPos = 0
          reply += 13.toByte
          reply += current.toByte

        case _ =>
          throw new IllegalStateException("implementation error")
      }
    }
    reply.result()
  }

  /**
    * read n bytes
    */
  def readBytes(in: InputStream, n: Int): Array[Byte] = {
    var count = 0
    val buf = ByteBuffer.allocate(n)
    while(count < n) {
      val next = in.read()
      if(next == -1) {
        throw new RedicaConnectionException("connection close?")
      }
      buf.put(next.toByte)
      count = count + 1
    }
    buf.array()
  }
}


trait UseRespReader {
  val respReader: RespReader
}

trait MixInRespReader {
  val respReader = new RespReader
}
