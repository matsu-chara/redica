package redica.util

object ByteUtil {
  val charset = "UTF-8"

  /**
    * getBytes with charset arg
    */
  def getBytes(s: String): Array[Byte] = s.getBytes(charset)

  /**
    * new String with charset arg
    */
  def fromBytes(bs: Array[Byte]) = new String(bs, charset)
}
