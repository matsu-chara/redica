package redica.util

import java.util.concurrent.{Executors, ThreadFactory}
;

object ThreadUtil {
  def singleDaemonThreadExecutor = Executors.newSingleThreadExecutor(new ThreadFactory {
    override def newThread(r: Runnable): Thread = {
      val t = new Thread(r)
      t.setDaemon(true)
      t
    }
  })
}
