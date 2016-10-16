package redica.client.io.protocols.reply

import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.client.io.protocols.ArrayByteOrInputStreamWrapper
import redica.util.ByteUtil

class RespReaderTest extends FunSpec with MockitoSugar {

  describe("RespReaderTest") {

    it("should readBytes") {
      val data = "abcdefg"
      val sut = new RespReader {}
      val in = mock[ArrayByteOrInputStreamWrapper]
      when(in.read()).thenReturn(data.head.toByte, data.tail.map(_.toByte): _*)

      sut.readBytes(in, 4) match {
        case ReplySuccess(actual) => assert(actual === data.map(_.toByte).take(4))
        case ReplyFailed(e) => fail(e)
        case _ => fail("reply not completed")
      }
    }

    it("should readLine") {
      val line = "abc\rdef\r\np"
      val in = mock[ArrayByteOrInputStreamWrapper]
      when(in.read()).thenReturn(line.head.toByte, line.tail.map(_.toByte): _*)

      val sut = new RespReader {}
      sut.readLine(in) match {
        case ReplySuccess(actual) => assert(actual === ByteUtil.getBytes(line.dropRight(1)))
        case ReplyFailed(e) => fail(e)
        case _ => fail("reply not completed")
      }
    }

  }
}
