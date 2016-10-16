package redica.client.io.protocols.request

import redica.util.ByteUtil

import scala.collection.mutable

/**
  * encode request as RESP(REdis Serialization Protocol)
  *
  * @see [[http://redis.io/topics/protocol]]
  */
trait RequestParser {
  private val CRLF = ByteUtil.getBytes("\r\n")

  /**
    * encode multi bulk request ("GET key", "SET key value" ...)
    */
  def multiBulk(command: String, args: Seq[Array[Byte]]): Array[Byte] = {
    val msg = mutable.ArrayBuilder.make[Byte]()
    msg ++= ByteUtil.getBytes("*%d".format(1 + args.length))
    msg ++= CRLF

    // command
    msg ++= ByteUtil.getBytes("$%d".format(ByteUtil.getBytes(command).length))
    msg ++= CRLF
    msg ++= ByteUtil.getBytes(command)
    msg ++= CRLF

    // args
    args.foreach { arg =>
      msg ++= ByteUtil.getBytes("$%d".format(arg.length))
      msg ++= CRLF
      msg ++= arg
      msg ++= CRLF
    }
    msg.result()
  }
}

object RequestParser extends RequestParser
