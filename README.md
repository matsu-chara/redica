# Redica

## Usage

start redis-server.

```sh
docker pull redis
docker run --name some-redis -p 6379:6379
```

add build.sbt dependency.

```scala
lazy val sample = project.in(file("."))
  .settings(
    scalaVersion := "2.11.8"
  )
  .dependsOn(redicaProject)

lazy val redicaProject = ProjectRef(uri("git://github.com/matsu-chara/redica.git"), "core")
```

write code.

```scala
import redica.client.RedisClientFactory
import scala.concurrent.Await
import scala.concurrent.duration.Duration
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
```

## Todo

- non-blocking get/set
- connection pool
- (partial) redis cluster support
- implement ponylang version
