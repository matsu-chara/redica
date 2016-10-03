package redica.client

import redica.client.exceptions.RedicaDeserializeException
import redica.client.formatters.{ByteDeserializer, ByteSerializer}
import redica.client.io.RedisIo
import redica.client.io.protocols.commands.RedisStringCommands

import scala.concurrent.{ExecutionContext, Future}

class RedisClient private[client](redisIo: RedisIo) extends GetOps {

  def get[K, V](key: K)(implicit serializer: ByteSerializer[K], deserializer: ByteDeserializer[V], ec: ExecutionContext): Future[V] = {
    val request = RedisStringCommands.Get.encodeRequest(serializer.toBytes(key))
    val reply = redisIo.send(request)(RedisStringCommands.Get.decodeReply)
    reply.flatMap { r =>
      deserializer.fromBytesOpt(r) match {
        case Some(data) => Future.successful(data)
        case None => Future.failed[V](new RedicaDeserializeException(s"parse failed. content = $reply"))
      }
    }
  }

  def set[K, V](key: K, value: V)(implicit serializerK: ByteSerializer[K], serializerV: ByteSerializer[V]): Future[Boolean] = {
    val keyBytes = serializerK.toBytes(key)
    val valueBytes = serializerV.toBytes(value)
    val request = RedisStringCommands.Set.encodeRequest(keyBytes, valueBytes)
    redisIo.send(request)(RedisStringCommands.Set.decodeReply)
  }

  def close() = redisIo.close()
}

trait GetOps { self: RedisClient =>
  def getAsString[K: ByteSerializer](key: K)(implicit ec: ExecutionContext) = get[K, String](key)
  def getAsInt[K: ByteSerializer](key: K)(implicit ec: ExecutionContext) = get[K, Int](key)
}
