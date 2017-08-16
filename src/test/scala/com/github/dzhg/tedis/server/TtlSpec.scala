package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}
import com.redis.Seconds

class TtlSpec extends TedisSuite with ServerAndClient {

  "TedisServer" must {
    "support ttl command" in {
      client.set("key", "value", onlyIfExists = false, Seconds(10))

      val ttl = client.ttl("key")
      ttl.value must be > 0L
    }

    "return correct value for ttl" in {
      client.set("key", "value", onlyIfExists = false, Seconds(10))
      Thread.sleep(1000)

      val ttl = client.ttl("key")
      ttl.value must be > 0L
      ttl.value must be < 10L
    }

    "return -1 for existing key without expiry" in {
      client.set("key", "value")

      val ttl = client.ttl("key")
      ttl.value must be (-1L)
    }

    "return -2 for non-existing key" in {
      val ttl = client.ttl("key")
      ttl.value must be (-2L)
    }

    "support pttl command" in {
      client.set("key", "value", onlyIfExists = false, Seconds(10))

      val ttl = client.pttl("key")
      ttl.value must be > 1000L
    }

    "return -1 for existing key without expiry (pttl)" in {
      client.set("key", "value")

      val ttl = client.pttl("key")
      ttl.value must be (-1L)
    }

    "return -2 for non-existing key (pttl)" in {
      val ttl = client.pttl("key")
      ttl.value must be (-2L)
    }
  }
}
