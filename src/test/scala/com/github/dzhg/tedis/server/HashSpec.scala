package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.TedisErrors
import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}

class HashSpec extends TedisSuite with ServerAndClient with TedisErrors {

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

    "hmset(key, ...)" must {
      "set the value if key does not exist" in {
        val result = client.hmset("key", Seq(("f1", "v1"), ("f2", "v2")))
        result must be (true)

        val v1 = client.hget("key", "f1")
        v1.value must be ("v1")

        val v2 = client.hget("key", "f2")
        v2.value must be ("v2")
      }

      "set values if key exists" in {
        client.hset("key", "f1", "v1")
        client.hmset("key", Seq("f1" -> "v", "f2" -> "v2"))

        val v1 = client.hget("key", "f1")
        v1.value must be ("v")

        val v2 = client.hget("key", "f2")
        v2.value must be ("v2")
      }
    }

    "hmget(key, ...)" must {
      "return all values for fields" in {
        client.hmset("key", Seq(("f1", "v1"), ("f2", "v2")))
        val v = client.hmget("key", "f1", "f2")
        v.value must be (Map("f1" -> "v1", "f2" -> "v2"))
      }

      "return correct values if not all fields exist" in {
        client.hmset("key", Seq(("f1", "v1"), ("f3", "v3")))
        val v = client.hmget("key", "f1", "f2", "f3")
        v.value must be (Map("f1" -> "v1", "f3" -> "v3"))
      }

      "return empty map if the key does not exist" in {
        val v = client.hmget("key", "f1", "f2")
        v.value must have size 0
      }
    }

    "hexists(key, field)" must {
      "return true if the hash contains the field" in {
        client.hset("k1", "f1", "v1")
        val v = client.hexists("k1", "f1")
        v must be (true)
      }

      "return false if key does not exist" in {
        val v = client.hexists("k1", "f1")
        v must be (false)
      }

      "return false if the hash does not contain the field" in {
        client.hset("k1", "f1", "v1")
        val v = client.hexists("k1", "f2")
        v must be (false)
      }

      "throw error if key is not hash" in {
        client.set("key", "value")
        an [Exception] mustBe thrownBy (client.hexists("key", "field"))
      }
    }

    "hdel(key, field, fields)" must {
      "delete existing fields" in {
        client.hmset("key", Seq(("f1", "v1"), ("f2", "v2"), ("f3", "v3")))

        val v = client.hdel("key", "f1", "f3")
        v.value must be (2)

        val v1 = client.hget("key", "f1")
        v1 mustBe empty

        val v2 = client.hget("key", "f2")
        v2.value must be ("v2")
      }

      "return 0 if key does not exist" in {
        val v = client.hdel("key", "f1", "f2")
        v.value must be (0)
      }

      "does not count non-existing fields" in {
        client.hmset("key", Seq(("f1", "v1"), ("f2", "v2")))

        val v = client.hdel("key", "f1", "f2", "f3", "f4")
        v.value must be (2)
      }

      "throw error if key is not a hash" in {
        client.set("key", "value")

        an [Exception] mustBe thrownBy (client.hdel("key", "f1"))
      }
    }

    "hlen(key)" must {
      "return correct length for hash" in {
        client.hmset("key", Seq(("f1", "v1"), ("f2", "v2"), ("f3", "v3")))
        val v = client.hlen("key")
        v.value must be (3)
      }

      "return 0 if key does not exist" in {
        val v = client.hlen("key")
        v.value must be (0)
      }

      "return 0 if hash is empty" in {
        client.hset("key", "f", "v")
        client.hdel("key", "f")
        val v = client.hlen("key")
        v.value must be (0)
      }

      "throw error if key is not a hash" in {
        client.set("key", "value")
        an [Exception] mustBe thrownBy (client.hlen("key"))
      }
    }

    "hkeys(key)" must {
      "return correct keys for hash" in {
        client.hmset("key", Seq(("f1", "v1"), ("f2", "v2"), ("f3", "v3")))
        val v = client.hkeys("key")
        v.value must be (List("f1", "f2", "f3"))
      }

      "return empty list if key does not exist" in {
        val v = client.hkeys("key")
        v.value must have size 0
      }

      "return empty list if hash is empty" in {
        client.hset("key", "f1", "v1")
        client.hdel("key", "f1")
        val v = client.hkeys("key")
        v.value must have size 0
      }
    }

    "hvals(key)" must {
      "return correct values for hash" in {
        client.hmset("key", Seq(("f1", "v1"), ("f2", "v2"), ("f3", "v3")))
        val v = client.hvals("key")
        v.value must be (List("v1", "v2", "v3"))
      }

      "return empty list if hash does not exist" in {
        val v = client.hvals("key")
        v.value must have size 0
      }

      "return empty list if hash is empty" in {
        client.hset("key", "f1", "v1")
        client.hdel("key", "f1")
        val v = client.hvals("key")
        v.value must have size 0
      }

      "throw error if key is not a hash" in {
        client.set("key", "value")
        an [Exception] mustBe thrownBy (client.hvals("key"))
      }
    }

    "hgetall(key)" must {
      "return correct key-value pairs" in {
        client.hmset("key", Seq(("f1", "v1"), ("f2", "v2"), ("f3", "v3")))
        val v = client.hgetall1("key")
        v.value must be (Map("f1" -> "v1", "f2" -> "v2", "f3" -> "v3"))
      }

      "return None if hash does not exist" in {
        val v = client.hgetall1("key")
        v mustBe empty
      }

      "return None if hash is empty" in {
        client.hset("key", "f1", "v1")
        client.hdel("key", "f1")
        val v = client.hgetall1("key")
        v mustBe empty
      }

      "throw error if key is not a hash" in {
        client.set("key", "value")
        an [Exception] mustBe thrownBy (client.hgetall1("key"))
      }
    }

    "hincrby(key, field, value)" must {
      "return correct value after increment" in {
        client.hset("key", "field", 1)
        client.hincrby("key", "field", 1)
        val v = client.hget("key", "field")
        v.value must be ("2")
      }

      "return correct value if key does not exist" in {
        client.hincrby("key", "field", 5)
        val v = client.hget("key", "field")
        v.value must be ("5")
      }

      "return correct value if field does not exist" in {
        client.hset("key", "f1", 1)
        client.hincrby("key", "f2", 5)
        val v = client.hget("key", "f2")
        v.value must be ("5")
      }

      "return correct value if increment is negative number" in {
        client.hset("key", "field", 6)
        client.hincrby("key", "field", -5)
        val v = client.hget("key", "field")
        v.value must be ("1")
      }
    }
  }
}
