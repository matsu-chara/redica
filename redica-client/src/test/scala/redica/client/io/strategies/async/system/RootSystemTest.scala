package redica.client.io.strategies.async.system

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.client.io.strategies.async.system.routers.RoundRobinRouter
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncProducer.WriteRequest
import redica.testutils.WriteRequestUtil

class RootSystemTest extends FunSpec with MockitoSugar {

  describe("RootSystemTest") {
    it("should receive") {
      val router = mock[RoundRobinRouter]

      val sut = new RootSystem(Seq()) {
        override val producerRouter = router
      }
      sut.receive(WriteRequestUtil.empty)

      verify(router, times(1)).receive(any[WriteRequest]())
    }
  }
}
