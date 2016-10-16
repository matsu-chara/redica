package redica.client.io.strategies.async.system.worker

import redica.client.io.strategies.async.system.RootScheduler
import redica.client.io.strategies.async.system.worker.Worker.{WorkerId, WorkerRequest}
import redica.client.io.strategies.async.system.worker.exceptions.RedicaNotHandledException

trait Worker {
  /** for scheduling */
  val id = new WorkerId

  protected val rootScheduler: RootScheduler

  /**
    * non-blocking job processing
    * ! if blocking in the cast, it will block rootSystem.
    *
    * worker should call rootScheduler#schedule) at the end of handling message.
    * you can call it in the async completionHandler callback.
    * ! if schedule not called, worker will be stopped(until new message receiving)
    */
  def cast(): Unit

  /* non-blocking receive job */
  def receive(request: WorkerRequest): Unit = {
    partialReceive.lift.apply(request).getOrElse(throw new RedicaNotHandledException(s"operation $request not handled"))
  }

  // just for aliasing
  type Receive = PartialFunction[WorkerRequest, Unit]

  protected val partialReceive: Receive
}

object Worker {
  trait WorkerRequest

  class WorkerId
}
