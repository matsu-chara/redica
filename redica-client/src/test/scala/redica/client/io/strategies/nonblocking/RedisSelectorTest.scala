package redica.client.io.strategies.nonblocking

import java.nio.channels.{SelectableChannel, Selector}

import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar

class RedisSelectorTest extends FunSpec with MockitoSugar {

  describe("RedisSelectorTest") {
    it("should request") {
      val chan = mock[SelectableChannel]
      val sel = mock[Selector]
      val selector = new RedisSelector(sel, Seq(chan))
      assert(selector.request(Array(1,2,3).map(_.toByte)).isCompleted === false)
    }
  }
}
