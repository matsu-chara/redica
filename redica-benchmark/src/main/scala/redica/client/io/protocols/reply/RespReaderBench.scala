package redica.client.io.protocols.reply

import java.io.ByteArrayInputStream

import org.openjdk.jmh.annotations.{Benchmark, Scope, State}
import redica.util.ByteUtil

object RespReaderBench {
  @State(Scope.Benchmark)
  val reader = new RespReader

  val data = ByteUtil.getBytes("a" * 1024 * 1024)
}

class RespReaderBench {

  import RespReaderBench._

  @Benchmark
  def readBytes(): Array[Byte] = {
    val in = new ByteArrayInputStream(data)
    reader.readBytes(in, 1024 * 1024)
  }

}
