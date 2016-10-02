package redica.client.io

import java.io.OutputStream
import java.net.Socket

import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class RedisBlockingIoTest extends FunSpec with MockitoSugar {

  describe("RedisBlockingIoTest") {

    it("should send")  {
      val out = mock[OutputStream]
      val socket = mock[Socket]
      when(socket.getOutputStream).thenReturn(out)

      val sut = new RedisBlockingIo(socket)
      Await.result(sut.send(Array[Byte](1, 2, 3))(_ => Right(1)), Duration.Inf)
      verify(out, times(1)).write(any[Array[Byte]])

    }

  }
}
