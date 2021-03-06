package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}
import com.redis.Seconds

class GetAndSetSpec extends TedisSuite with ServerAndClient {

  "TedisServer" when {
    "set(key, value)" must {
      "set value" in {
        val set = client.set("key", "value")
        set must be (true)

        val v = client.get("key")
        v.value must be ("value")
      }
    }

    "set(key, value, onlyIfExists = true, time)" must {
      "not set value if key does not exist" in {
        val result = client.set("key", "value", onlyIfExists = true, Seconds(30))
        result must be(false)

        val v = client.get("key")
        v mustBe empty
      }

      "set value if key exists" in {
        client.set("k1", "v1")
        val result = client.set("k1", "v2", onlyIfExists = true, Seconds(30))
        result must be (true)

        val s = client.get("k1")
        s.value must be ("v2")
      }
    }

    "set(key, value, onlyIfExists = false, time)" must {
      "not set value if key exists" in {
        client.set("k1", "v1")
        val result = client.set("k1", "v2", onlyIfExists = false, Seconds(30))
        result must be (false)

        val v = client.get("k1")
        v.value must be ("v1")
      }

      "set value and expiry if key does not exist" in {
        val result = client.set("key", "value", onlyIfExists = false, Seconds(5))
        result must be(true)

        val v = client.get("key")
        v.value must be("value")

        val ttl = client.ttl("key")
        ttl.value must be > 0L
      }
    }

    "get(key)" must {
      "return some value if key exists" in {
        client.set("key", "value")
        val v = client.get("key")
        v.value must be ("value")
      }

      "return none if key does not exist" in {
        val v = client.get("key")
        v mustBe empty
      }
    }

    "getset(key, value)" must {
      "return the old value and set the new value" in {
        client.set("key", "1")
        val v = client.getset("key", "0")
        v.value must be ("1")
      }

      "return nil and set the new value if the key does not exist" in {
        val v = client.getset("key", "0")
        v mustBe empty
      }
    }

    "setex(key, expiry, value)" must {
      "set the value and expiry" in {
        val result = client.setex("key", 5, "value")
        result must be (true)

        val v = client.get("key")
        v.value must be ("value")

        val ttl = client.ttl("key")
        ttl.value must be > 0L
      }
    }

    "psetex(key, expiry, value)" must {
      "set the value and expiry" in {
        val result = client.psetex("key", 5000, "value")
        result must be (true)

        val v = client.get("key")
        v.value must be ("value")

        val ttl = client.ttl("key")
        ttl.value must be > 0L
      }
    }
  }
}
