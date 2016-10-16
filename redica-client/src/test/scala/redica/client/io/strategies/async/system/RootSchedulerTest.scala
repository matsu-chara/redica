package redica.client.io.strategies.async.system

import java.util.concurrent.TimeUnit

import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import redica.client.io.strategies.async.system.worker.Worker
import redica.client.io.strategies.async.system.worker.Worker.WorkerId

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RootSchedulerTest extends FunSpec with MockitoSugar {

  describe("RootScheduler") {
    it("should schedule and call") {
      val sut = new RootScheduler
      val worker = mock[Worker]
      when(worker.id).thenReturn(new WorkerId)
      
      sut.schedule(worker)
      val called = Future(sut.call())(scala.concurrent.ExecutionContext.global)

      Await.result(called, Duration(1, TimeUnit.SECONDS))
      verify(worker, times(1)).cast()
    }
  }
}
