package redica.client.io.protocols.reply

import java.io.InputStream

import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.util.ByteUtil

class RespReaderTest extends FunSpec with MockitoSugar {

  describe("RespReaderTest") {

    it("should readBytes") {
      val data = "abcdefg"
      val sut = new RespReader {}
      val in = mock[InputStream]
      when(in.read()).thenReturn(data.head.toByte.toInt, data.tail.map(_.toByte.toInt): _*)

      val actual= sut.readBytes(in, 4)
      assert(actual === data.map(_.toByte).take(4))
    }

    it("should readLine") {
      val line = "abc\rdef\r\np"
      val in = mock[InputStream]
      when(in.read()).thenReturn(line.head.toByte.toInt, line.tail.map(_.toByte.toInt): _*)

      val sut = new RespReader {}
      val actual = sut.readLine(in)
      assert(actual === ByteUtil.getBytes(line.dropRight(3)))
    }

  }
}
