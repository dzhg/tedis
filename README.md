# Tedis (A Redis Implementation in Scala for Unit Testing)

[![Build Status](https://img.shields.io/travis/dzhg/tedis/master.svg)](https://travis-ci.org/dzhg/tedis) 
[![Coverage Status](https://img.shields.io/coveralls/dzhg/tedis/master.svg)](https://coveralls.io/github/dzhg/tedis?branch=master)

## Key features

* Pure scala implementation
* Run locally in memory
* Easy to integrate with existing Java or Scala unit tests
* Decouple unit tests from standalone Redis server

## Introduction

Tedis is basically a mock of Redis. It's implemented in Scala. It can be run locally in same JVM with unit test code.

It's useful for testing Redis related code without a real Redis server.

```Scala
class ServerRunner(server: TedisServer) extends Thread {
  override def run(): Unit = server.start()
}

// Initialize TedisServer
val server = new TedisServer(0)
new ServerRunner(server).start()

// Create a RedisClient
val client = new RedisClient("localhost", server.socket.getLocalPort)

// Client can talk to TedisServer as if it's a real Redis server
client.set("key", "value")
val v = client.get("key")
```

## Command supported

![Command Support Progress](https://img.shields.io/badge/progress-9%2F200-orange.svg)

Total number of commands available in Redis: [200](https://redis.io/commands)

Currently supported by Tedis: 9

 * [PING](https://redis.io/commands/ping)
 * [MULTI](https://redis.io/commands/multi), [EXEC](https://redis.io/commands/exec) and [DISCARD](https://redis.io/commands/discard)
 * [SET](https://redis.io/commands/set) and [GET](https://redis.io/commands/get)
 * [MSET](https://redis.io/commands/mset) and [MGET](https://redis.io/commands/mget)
 * [TTL](https://redis.io/commands/ttl) and [PTTL](https://redis.io/commands/pttl)

## Roadmap
| Version | Features                                                                 | Schedule  | Status      |
|---------|--------------------------------------------------------------------------|-----------|-------------|
| 0.1.0   | Support String, Hash, Set, List operations                               | Oct. 2017 | In Progress |
| 0.2.0   | Support bit operations <br/>Start adapter implementation (`scala-redis`) |           |             |
| 0.5.0   | Support `scala-redis` and `jedis` adapters                               |           |             |
