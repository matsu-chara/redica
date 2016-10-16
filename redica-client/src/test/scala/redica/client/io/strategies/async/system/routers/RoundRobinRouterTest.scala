package redica.client.io.strategies.async.system.routers

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.{FunSpec, FunSuite}
import org.scalatest.mockito.MockitoSugar
import redica.client.io.strategies.async.system.worker.Worker
import redica.client.io.strategies.async.system.worker.workers.RedisAsyncProducer.WriteRequest
import redica.testutils.WriteRequestUtil

class RoundRobinRouterTest extends FunSpec with MockitoSugar {

  describe("RoundRobinRouter") {
    it("receive") {
      val workers = Seq.fill(5)(mock[Worker])
      val sut = new RoundRobinRouter(workers)
      (1 to 6).foreach { _ =>
        sut.receive(WriteRequestUtil.empty)
      }

      verify(workers.head, times(2)).receive(any[WriteRequest])
      workers.tail.foreach { w => verify(w, times(1)).receive(any[WriteRequest]) }
    }
  }

}
