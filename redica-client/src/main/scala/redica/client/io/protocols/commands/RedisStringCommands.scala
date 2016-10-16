package redica.client.io.protocols.commands

import redica.client.io.protocols.request.RequestParser

/**
  * Redis Basic Commands
  */
object RedisStringCommands {
  sealed trait Commands[V] {
    protected val command: String

    protected def encode(args: Seq[Array[Byte]]): Array[Byte] = RequestParser.multiBulk(command, args)
  }

  object Get extends Commands[Array[Byte]] {
    override protected val command: String = "GET"

    def encodeRequest(key: Array[Byte]) = encode(Seq(key))
  }

  object Set extends Commands[Boolean] {
    override protected val command: String = "SET"

    def encodeRequest(key: Array[Byte], value: Array[Byte]) = encode(Seq(key, value))
  }
}
