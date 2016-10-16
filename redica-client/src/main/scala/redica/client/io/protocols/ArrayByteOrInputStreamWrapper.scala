package redica.client.io.protocols

import java.io.InputStream

import scala.collection.mutable

/**
  * mutable
  */
trait ArrayByteOrInputStreamWrapper {
  def read(): Byte
}

class InputStreamWrapper(private val in: InputStream) extends ArrayByteOrInputStreamWrapper {
  override def read(): Byte = in.read().toByte
}

class ArrayByteWrapper(private var value: Array[Byte]) extends ArrayByteOrInputStreamWrapper {
  override def read(): Byte = {
    value.headOption match {
      case Some(a) =>
        value = value.tail
        a
      case None =>
        (-1).toByte
    }
  }

  def prepend(a: Array[Byte]): Unit = {
    val builder = mutable.ArrayBuilder.make[Byte]
    builder.sizeHint(a.length + value.length)
    builder ++= a
    builder ++= value
    value = builder.result()
  }

  def append(a: Array[Byte]): Unit = {
    val builder = mutable.ArrayBuilder.make[Byte]
    builder.sizeHint(value.length + a.length)
    builder ++= value
    builder ++= a
    value = builder.result()
  }
}

object ArrayByteWrapper {
  def empty = new ArrayByteWrapper(Array.emptyByteArray)
}
