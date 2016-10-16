package redica.client.io.protocols.reply

import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.client.io.protocols.ArrayByteOrInputStreamWrapper
import redica.util.ByteUtil

class ReplyParserTest extends FunSpec with MockitoSugar {

  describe("ReplyParserTest") {

    it("should parse bulk") {
      // "$4\r\ntest\r\n"

      val mockReader = mock[RespReader]
      when(mockReader.readLine(any[ArrayByteOrInputStreamWrapper])).thenReturn(ReplySuccess(ByteUtil.getBytes("$4\r\n")))
      when(mockReader.readBytes(any[ArrayByteOrInputStreamWrapper], Matchers.eq(4 + 2))).thenReturn(ReplySuccess(ByteUtil.getBytes("test\r\n")))

      val sut = new ReplyParser {
        override val respReader = mockReader
      }

      sut.parse(null) match {
        case ReplySuccess(bs) => assert(ByteUtil.getBytes("test") === bs)
        case ReplyFailed(e) => fail(e)
        case _ => fail("parse not completed")
      }
    }

    it("should parse status") {
      // "+OK\r\n"

      val mockReader = mock[RespReader]
      when(mockReader.readLine(any[ArrayByteOrInputStreamWrapper])).thenReturn(ReplySuccess(ByteUtil.getBytes("+OK\r\n")))

      val sut = new ReplyParser {
        override val respReader = mockReader
      }

      sut.parse(null) match {
        case ReplySuccess(status) => assert(status === Array(1.toByte))
        case ReplyFailed(e) => fail(e)
        case _ => fail("parse not completed")
      }

      // no need to ignore last line
      verify(mockReader, times(1)).readLine(any[ArrayByteOrInputStreamWrapper])
    }

  }
}
