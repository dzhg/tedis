# Tedis (A Redis Implementation in Scala for Unit Testing)

[![Build Status](https://img.shields.io/travis/dzhg/tedis/master.svg)](https://travis-ci.org/dzhg/tedis)
[![Coveralls](https://img.shields.io/coveralls/dzhg/tedis/master.svg)](https://coveralls.io/github/dzhg/tedis?branch=master)
[![Codecov](https://img.shields.io/codecov/c/github/dzhg/tedis.svg)](https://codecov.io/gh/dzhg/tedis)

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

![Command Support Progress](https://img.shields.io/badge/progress-40%2F200-orange.svg)

Total number of commands available in Redis: [200](https://redis.io/commands)

Currently supported by Tedis: 40

### Keys

[DEL](https://redis.io/commands/del), DUMP, [EXISTS](https://redis.io/commands/exists), EXPIRE, EXPIREAT, KEYS, MIGRATE, MOVE, OBJECT, PERSIST, PEXPIRE, PEXPAIREAT, [PTTL](https://redis.io/commands/pttl), RANDOMKEY, RENAME, RENAMENX, RESTORE, SORT, TOUCH, [TTL](https://redis.io/commands/ttl), TYPE, UNLINK, WAIT, SCAN

### Strings

[APPEND](https://redis.io/commands/append), BITCOUNT, BITFIELD, BITOP, BITPOS, [DECR](https://redis.io/commands/decr), [DECRBY](https://redis.io/commands/decrby), [GET](https://redis.io/commands/get), GETBIT, [GETRANGE](https://redis.io/commands/getrange), [GETSET](https://redis.io/commands/getset), [INCR](https://redis.io/commands/incr), [INCRBY](https://redis.io/commands/incrby), [INCRBYFLOAT](https://redis.io/commands/incrbyfloat), [MGET](https://redis.io/commands/mget), [MSET](https://redis.io/commands/mset), [MSETNX](https://redis.io/commands/msetnx), [PSETEX](https://redis.io/commands/psetex), [SET](https://redis.io/commands/set), SETBIT, [SETEX](https://redis.io/commands/setex), SETNX, [SETRANGE](https://redis.io/commands/setrange), [STRLEN](https://redis.io/commands/strlen)

### Hashes

[HDEL](https://redis.io/commands/hdel), [HEXISTS](https://redis.io/commands/hexists), [HGET](https://redis.io/commands/hget), [HGETALL](https://redis.io/commands/hgetall), [HINCRBY](https://redis.io/commands/hincrby), [HINCRBYFLOAT](https://redis.io/commands/hincrbyfloat), [HKEYS](https://redis.io/commands/hkeys), [HLEN](https://redis.io/commands/hlen), [HMGET](https://redis.io/commands/hmget), [HMSET](https://redis.io/commands/hmset), [HSET](https://redis.io/commands/hset), [HSETNX](https://redis.io/commands/hsetnx), HSTRLEN, [HVALS](https://redis.io/commands/hvals), HSCAN

### Lists

BLPOP, BRPOP, BRPOPLPUSH, LINDEX, LINSERT, [LLEN](https://redis.io/commands/llen), LPOP, LPUSH, LPUSHX, LRANGE, LREM, LSET, LTRIM, [RPOP](https://redis.io/commands/rpop), RPOPLPUSH, [RPUSH](https://redis.io/commands/rpush), RPUSHX

### Transactions

[DISCARD](https://redis.io/commands/discard), [EXEC](https://redis.io/commands/exec), [MULTI](https://redis.io/commands/multi), UNWATCH, WATCH

### Connection

AUTH, ECHO, [PING](https://redis.io/commands/ping), QUIT, SELECT, SWAPDB

## Roadmap

| Version | Features                                                                 | Schedule  | Status      |
|---------|--------------------------------------------------------------------------|-----------|-------------|
| 0.1.0   | Support String, Hash, Set, List operations                               | Oct. 2017 | In Progress |
| 0.2.0   | Support bit operations <br/>Start adapter implementation (`scala-redis`) |           |             |
| 0.5.0   | Support `scala-redis` and `jedis` adapters                               |           |             |
