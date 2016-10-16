package redica.client.io.strategies.async.system

import java.util.concurrent.{ConcurrentHashMap, LinkedBlockingQueue}

import redica.client.io.strategies.async.system.worker.Worker
import redica.client.io.strategies.async.system.worker.Worker.WorkerId

class RootScheduler {
  private val workerQueue = new LinkedBlockingQueue[Worker]()

  // WorkerId Set, which is already in queue
  private val queuedWorkers = new ConcurrentHashMap[WorkerId, Unit]()

  def schedule(worker: Worker): Unit = {
    if(Option(queuedWorkers.putIfAbsent(worker.id, ())).isEmpty) {
      workerQueue.put(worker)
    }
  }

  def call(): Unit = {
    val worker = workerQueue.take() // block during nothing to do
    queuedWorkers.remove(worker.id)
    worker.cast()
  }
}
