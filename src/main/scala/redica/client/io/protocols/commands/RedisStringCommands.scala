package redica.client.io.protocols.commands

import java.io.InputStream

import redica.client.io.protocols.reply.ReplyParser
import redica.client.io.protocols.request.RequestParser
import redica.exceptions.RedicaException

/**
  * Redis Basic Commands
  */
object RedisStringCommands {
  sealed trait Commands[V] {
    protected val command: String
    def decodeReply(in: InputStream): Either[RedicaException, V]

    protected def encode(args: Seq[Array[Byte]]): Array[Byte] = RequestParser.multiBulk(command, args)
  }

  object Get extends Commands[Array[Byte]] {
    override protected val command: String = "GET"
    def encodeRequest(key: Array[Byte]) = encode(Seq(key))
    override def decodeReply(in: InputStream): Either[RedicaException, Array[Byte]] = ReplyParser.bulk(in)
  }

  object Set extends Commands[Boolean] {
    override protected val command: String = "SET"
    def encodeRequest(key: Array[Byte], value: Array[Byte]) = encode(Seq(key, value))
    override def decodeReply(in: InputStream): Either[RedicaException, Boolean] = ReplyParser.status(in)
  }
}
