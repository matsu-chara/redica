package redica.client.io.protocols.reply

import redica.client.io.exceptions.RedicaProtocolException
import redica.client.io.protocols.ArrayByteOrInputStreamWrapper
import redica.exceptions.RedicaException
import redica.util.{ByteUtil, StringParseUtil}

import scala.util.control.NonFatal

/**
  * parse response as RESP(REdis Serialization Protocol)
  *
  * @see [[http://redis.io/topics/protocol]]
  */
trait ReplyParser extends UseRespReader {

  def trimCRLF(a: Array[Byte]): Array[Byte] = {
    if (a.endsWith(ByteUtil.getBytes("\r\n"))) {
      a.dropRight(2)
    } else {
      a
    }
  }

  def parse(in: ArrayByteOrInputStreamWrapper): ReplyResult[Array[Byte]] = {
    try {
      val firstLineResult = respReader.readLine(in)
      firstLineResult.flatMap { firstLine =>
        ByteUtil.fromBytes(trimCRLF(firstLine)).headOption match {
          case Some('$') => bulk(firstLine, in)
          case Some('+') | Some('-') => status(firstLine, in)
          case _ => ReplyFailed(new RedicaException(s"unsupported reply ${ByteUtil.fromBytes(firstLine)}"))
        }
      }
    } catch {
      case NonFatal(e) => ReplyFailed(new RedicaException("failed to read reply", e))
    }
  }

  /**
    * parse status reply( OK or error msg)
    */
  private def status(fisrtLineBytes: Array[Byte], in: ArrayByteOrInputStreamWrapper): ReplyResult[Array[Byte]] = {
    val firstLine = ByteUtil.fromBytes(trimCRLF(fisrtLineBytes))
    firstLine.headOption match {
      case Some('+') if firstLine.tail == s"OK" => ReplySuccess(Array(1.toByte))
      case Some('+') => ReplyInProgress(fisrtLineBytes)
      case Some('-') => ReplySuccess(Array(0.toByte))
      case _ => ReplyFailed(new RedicaException("wrong implementation."))
    }
  }


  /**
    * parse bulk reply
    * see test for sample data
    */
  private def bulk(firstLineBytes: Array[Byte], in: ArrayByteOrInputStreamWrapper): ReplyResult[Array[Byte]] = {
    // for more specified exception
    val replyBodyBytes = StringParseUtil.safeParseInt(ByteUtil.fromBytes(trimCRLF(firstLineBytes)).tail).right
      .getOrElse(throw new RedicaProtocolException(s"redis response doesn't contain total bytes in first line: ${
        firstLineBytes.tail
      }"))

    respReader
      .readBytes(in, replyBodyBytes + 2) // +2 means CR_LF bytes for complete reading
      .inProgressMap(bytes => firstLineBytes ++ bytes) // return to state before reading
      .map(trimCRLF) // remove CR_LF if succeeded
  }
}

object ReplyParser extends ReplyParser with MixInRespReader {}
