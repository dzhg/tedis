package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.TedisErrors
import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}

class HsetAndHgetSpec extends TedisSuite with ServerAndClient with TedisErrors {

  "TedisServer" when {
    "hset(key, field, value)" must {
      "set hash field" in {
        val result = client.hset("key", "field", "value")

        result must be (true)

        val v = client.hget("key", "field")
        v.value must be ("value")
      }

      "throw error if key is not a hash" in {
        client.set("key", "value")
        val ex = the [Exception] thrownBy client.hset("key", "field", "value")
        ex.getMessage must be (s"${WRONG_TYPE.error} ${WRONG_TYPE.msg}")
      }

      "set multiple fields with multiple calls" in {
        client.hset("key", "f1", "v1")
        client.hset("key", "f2", "v2")

        val v1 = client.hget("key", "f1")
        v1.value must be ("v1")
        val v2 = client.hget("key", "f2")
        v2.value must be ("v2")
      }
    }

    "hget(key, field)" must {
      "return correct value" in {
        client.hset("key", "f1", "v1")
        client.hget("key", "f1").value must be ("v1")
      }

      "return nil if key does not exist" in {
        val v = client.hget("key", "f")
        v mustBe empty
      }

      "return nil if field does not exist" in {
        client.hset("key", "f1", "v1")
        val v = client.get("key", "f2")
        v mustBe empty
      }

      "throw error if key is not a hash" in {
        client.set("key", "value")
        val ex = the [Exception] thrownBy client.hget("key", "f1")
        ex.getMessage must be (s"${WRONG_TYPE.error} ${WRONG_TYPE.msg}")
      }
    }

    "hsetnx(key, field, value)" must {
      "set the value if key does not exist" in {
        val result = client.hsetnx("key", "f1", "v1")
        result must be (true)

        val v = client.hget("key", "f1")
        v.value must be ("v1")
      }

      "not set the value if field exists" in {
        val result = client.hset("key", "f1", "v1")
        result must be (true)

        val s = client.hsetnx("key", "f1", "v2")
        s must be (false)

        val v = client.hget("key", "f1")
        v.value must be ("v1")
      }

      "set the value if field does not exist in the hash" in {
        client.hset("key", "f1", "v1")
        val b = client.hsetnx("key", "f2", "v2")
        b must be (true)

        val v = client.hget("key", "f2")
        v.value must be ("v2")
      }

      "throw error if key is not a hash" in {
        client.set("key", "value")
        val ex = the [Exception] thrownBy client.hsetnx("key", "f1", "v1")
        ex.getMessage must be (s"${WRONG_TYPE.error} ${WRONG_TYPE.msg}")
      }
    }
  }
}
