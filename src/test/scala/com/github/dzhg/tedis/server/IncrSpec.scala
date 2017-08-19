package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.TedisErrors
import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}

class IncrSpec extends TedisSuite with ServerAndClient with TedisErrors {

  "TedisServer" when {
    "incr(key)" must {
      "increase the value" in {
        client.set("key", "5")
        val v = client.incr("key")
        v.value must be (6L)
      }
    }

    "incr(key) with non-existing key" must {
      "set the value to 1" in {
        val v = client.incr("key")
        v.value must be (1L)
      }
    }

    "incr(key) with non-integer value" must {
      "throw error" in {
        client.set("key", "abc")
        val ex = the [Exception] thrownBy client.incr("key")
        ex.getMessage must be (s"${WRONG_NUMBER_FORMAT.error} ${WRONG_NUMBER_FORMAT.msg}")
      }
    }

    "incrby(key, 3)" must {
      "increase the value by 3" in {
        client.set("key", "5")
        val v = client.incrby("key", 3)
        v.value must be (8L)
      }
    }

    "incrbyfloat(key, 1.5)" must {
      "increase the value by 1.5" in {
        client.set("key", "5")
        val v = client.incrbyfloat("key", 1.5F)
        v.value must be (6.5F)
      }
    }

    "decr(key)" must {
      "decrease the value by 1" in {
        client.set("key", "5")
        client.decr("key").value must be (4L)
        client.decr("key").value must be (3L)
      }
    }

    "decrby(key, 3)" must {
      "decrease the value by 3" in {
        client.set("key", 6)
        client.decrby("key", 3L).value must be (3L)
        client.decrby("key", 3L).value must be (0L)
      }
    }
  }
}
