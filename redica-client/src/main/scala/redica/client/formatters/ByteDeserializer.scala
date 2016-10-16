package redica.client.formatters

import redica.util.ByteUtil

import scala.annotation.implicitNotFound
import scala.util.Try

@implicitNotFound(msg = "ByteDeserializer[${A}] instance not found. please implement.")
trait ByteDeserializer[A] {self =>
  def fromBytesOpt(a: Array[Byte]): Option[A]

  def map[B](f: A => B) = new ByteDeserializer[B] {
    override def fromBytesOpt(a: Array[Byte]): Option[B] = self.fromBytesOpt(a).map(f)
  }
}

object ByteDeserializer {
  protected def impl[A](f: Array[Byte] => Option[A]) = new ByteDeserializer[A] {
    override def fromBytesOpt(a: Array[Byte]): Option[A] = f(a)
  }

  implicit val byteDeserializer: ByteDeserializer[Array[Byte]] = impl(x => Some(x))
  implicit val stringDeserializer: ByteDeserializer[String] = impl(x => Some(ByteUtil.fromBytes(x)))
  implicit val intDeserializer: ByteDeserializer[Int] = impl(bs => Try(Integer.parseInt(ByteUtil.fromBytes(bs))).toOption)
}
