package redica.client.io.protocols.reply

import java.io.InputStream

import redica.client.io.exceptions.RedicaProtocolException
import redica.exceptions.RedicaException
import redica.util.{ByteUtil, StringParseUtil}

import scala.util.control.NonFatal

/**
  * parse response as RESP(REdis Serialization Protocol)
  * @see [[http://redis.io/topics/protocol]]
  */
trait ReplyParser extends UseRespReader {

  /**
    * parse status reply( OK or error msg)
    */
  def status(in: InputStream): Either[RedicaException, Boolean] = {
    try {
      val line = ByteUtil.fromBytes(respReader.readLine(in))
      line.headOption match {
        case Some('+') if line.tail == s"OK" => Right(true)
        case Some('-') => Right(false)
        case _ => Left(new RedicaProtocolException(s"unknown response ${line}"))
      }
    } catch {
      case NonFatal(e) => Left(new RedicaException("failed to read reply", e))
    }
  }

  /**
    * parse bulk reply
    * see test for sample data
    */
  def bulk(in: InputStream): Either[RedicaException, Array[Byte]] = {
    try {
      val firstLine = ByteUtil.fromBytes(respReader.readLine(in))

      firstLine.startsWith("$") match {
        case true =>
          // for more specified exception
          val replyBodyBytes = StringParseUtil.safeParseInt(firstLine.tail).right
            .getOrElse(throw new RedicaProtocolException(s"redis response doesn't contain total bytes in first line: $firstLine"))

          val content = respReader.readBytes(in, replyBodyBytes)
          respReader.readLine(in) // ignore last CR_LF
          Right(content)
        case false =>
          Left(new RedicaProtocolException("redis response not start with $"))
      }
    } catch {
      case NonFatal(e) => Left(new RedicaException("failed to read reply", e))
    }
  }
}

object ReplyParser extends ReplyParser with MixInRespReader {}
