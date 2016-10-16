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

  /**
    * string for debugging
    */
  def printable(bs: Array[Byte]): String = {
    if (bs.length == 1 && (bs.head == 1 || bs.head == 0)) {
      if (bs.head == 1) s"true: ${bs.toSeq}" else s"false: ${bs.toSeq}"
    } else {
      ByteUtil.fromBytes(bs).replace("\r", "\\r").replace("\n", "\\n") + " : " + bs.toSeq
    }
  }
}
