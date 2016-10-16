package redica.util

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object FutureUtil {
  private[redica] val immediateExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit = try {
      runnable.run()
    } catch {
      case NonFatal(e) => reportFailure(e)
    }

    override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
  }
}
