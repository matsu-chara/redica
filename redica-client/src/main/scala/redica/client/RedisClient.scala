package redica.client

import redica.client.exceptions.RedicaDeserializeException
import redica.client.formatters.{ByteDeserializer, ByteSerializer}
import redica.client.io.protocols.commands.RedisStringCommands
import redica.client.io.strategies.RedisIo
import redica.util.ByteUtil

import scala.concurrent.{ExecutionContext, Future}

class RedisClient private[client](redisIo: RedisIo) extends GetOps {

  def get[K, V](key: K)(implicit serializer: ByteSerializer[K], deserializer: ByteDeserializer[V], ec: ExecutionContext): Future[V] = {
    val request = RedisStringCommands.Get.encodeRequest(serializer.toBytes(key))
    val reply = redisIo.send(request)
    reply.flatMap { bytes =>
      deserializer.fromBytesOpt(bytes) match {
        case Some(data) => Future.successful(data)
        case None => Future.failed[V](new RedicaDeserializeException(s"get parse failed. content = ${ByteUtil.printable(bytes)}"))
      }
    }
  }

  def set[K, V](key: K, value: V)(implicit serializerK: ByteSerializer[K], serializerV: ByteSerializer[V], ec: ExecutionContext): Future[Boolean] = {
    val keyBytes = serializerK.toBytes(key)
    val valueBytes = serializerV.toBytes(value)
    val request = RedisStringCommands.Set.encodeRequest(keyBytes, valueBytes)
    val response: Future[Array[Byte]] = redisIo.send(request)
    response.flatMap { bytes =>
      bytes.headOption.map(b => Future.successful(if (b == 1) true else false))
        .getOrElse(Future.failed(new RedicaDeserializeException(s"set parse failed. content = ${ByteUtil.printable(bytes)}")))
    }
  }

  def close() = redisIo.close()
}

trait GetOps {self: RedisClient =>
  def getAsString[K: ByteSerializer](key: K)(implicit ec: ExecutionContext) = get[K, String](key)

  def getAsInt[K: ByteSerializer](key: K)(implicit ec: ExecutionContext) = get[K, Int](key)
}
