package example

import redica.client.RedisClientFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {

  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val client = RedisClientFactory.connect("127.0.0.1", 6379)
    val result = for {
      _ <- client.set("a", "str")
      a <- client.getAsString("a")
      _ <- client.set("b", 3333)
      b <- client.getAsInt("b")
    } yield (a, b)

    println(Await.result(result, Duration.Inf)) // (str, 3333)
    client.close()
  }
}

