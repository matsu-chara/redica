package redica.client.formatters

import redica.util.ByteUtil

import scala.annotation.implicitNotFound

@implicitNotFound(msg = "ByteSerializer[${A}] instance not found. please implement.")
trait ByteSerializer[A] { self =>
  def toBytes(a: A): Array[Byte]

  def contramap[B](f: B => A) = new ByteSerializer[B] {
    override def toBytes(a: B): Array[Byte] = self.toBytes(f(a))
  }
}

object ByteSerializer {
  private def impl[A](f: A => Array[Byte]) = new ByteSerializer[A] {
    override def toBytes(a: A): Array[Byte] = f(a)
  }

  implicit val byteSerializer: ByteSerializer[Array[Byte]] = impl(identity)
  implicit val stringSerializer: ByteSerializer[String] = impl(ByteUtil.getBytes)
  implicit val intSerializer: ByteSerializer[Int] = impl(i => ByteUtil.getBytes(i.toString))
}
