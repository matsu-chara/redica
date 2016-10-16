package redica.client.io.strategies.nonblocking

import java.nio.channels.{SelectableChannel, Selector}

import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar

class RedisNonBlockingIoTest extends FunSpec with MockitoSugar {

  describe("RedisNonBlockingIoTest") {

    it("should send") {
      val chan = mock[SelectableChannel]
      val sel = mock[Selector]
      val io = new RedisNonBlockingIo(sel, Seq(chan))
      val future = io.send(Array(1,2,3).map(_.toByte))
      assert(future.isCompleted === false)
    }

  }
}
