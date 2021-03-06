package com.github.dzhg.tedis.operations

import com.github.dzhg.tedis.storage.{BadTedisValue, HashMapTedisStorage, TedisEntry, TedisKeyInfo}
import com.github.dzhg.tedis.utils.{TedisSuite, TedisTest}
import com.github.dzhg.tedis.{TedisException, TedisStorage}

/**
  * @author dzhg 8/11/17
  */
class StringOperationsSpec extends TedisSuite {

  class TedisStringTest(internal: TedisStorage) extends TedisTest(internal) with StringOperations with KeyOperations

  def instance(): TedisStringTest = new TedisStringTest(new HashMapTedisStorage)

  "set(key, value) command" must {
    "set value correctly" in {
      val ops = instance()
      val result = ops.set("key1", "value1")
      result must be(true)

      verifyStringValue(ops, "key1", "value1")
    }

    "overwrite value correctly" in {
      val ops = instance()

      ops.set("key1", "value1")
      val result = ops.set("key1", "value2")

      result must be(true)

      val value = ops.get("key1")
      value.value must be("value2")
    }

    "not overwrite value if keys are different" in {
      val ops = instance()
      ops.set("key1", "value1")
      ops.set("key2", "value2")

      val v1 = ops.get("key1")
      v1.value must be("value1")

      val v2 = ops.get("key2")
      v2.value must be("value2")
    }
  }

  "get(key) command" must {
    "return None if no value has been set" in {
      val ops = instance()
      val v = ops.get("key")
      v mustBe empty
    }

    "throw exception if the value is not a string" in {
      val ops = instance()
      ops.internal.put("key1",
        TedisEntry(TedisKeyInfo("key1", None, System.currentTimeMillis()), BadTedisValue))

      a [TedisException] mustBe thrownBy (ops.get("key1"))
    }
  }

  "set(key, value, time)" must {
    "set ttl correctly" in {
      val ops = instance()
      ops.set("key1", "value1", Some(5000L))

      val ttl = ops.ttl("key1")
      ttl must be > 0L
    }

    "not set ttl if time is not assigned" in {
      val ops = instance()
      ops.set("key1", "value1", None)

      val ttl = ops.ttl("key1")
      ttl must be (-1L)
    }
  }

  "set(key, value, onlyIfExists, time)" must {
    "set value if key exists and onlyIfExists is true" in {
      val ops = instance()
      ops.set("key1", "value1")
      val result = ops.set("key1", "value2", onlyIfExists = true, None)
      result must be (true)

      val v = ops.get("key1")
      v.value must be ("value2")
    }

    "not set value if key exists and onlyIfExists is false" in {
      val ops = instance()
      ops.set("key1", "value1")
      val result = ops.set("key1", "value2", onlyIfExists = false, None)
      result must be (false)

      val v = ops.get("key1")
      v.value must be ("value1")
    }

    "set value if key does not exist and onlyIfExists is false" in {
      val ops = instance()
      val result = ops.set("key1", "value1", onlyIfExists = false, None)
      result must be (true)

      verifyStringValue(ops, "key1", "value1")
    }

    "not set value if key does not exist and onlyIfExists is true" in {
      val ops = instance()
      val result = ops.set("key1", "value1", onlyIfExists = true, None)
      result must be (false)

      val v = ops.get("key1")
      v mustBe empty
    }
  }

  "mset(kvs)" must {
    "set multiple keys" in {
      val ops = instance()
      val result = ops.mset(("key1", "value1"), ("key2", "value2"), ("key3", "value3"))
      result must be (true)

      verifyStringValue(ops, "key1", "value1")
      verifyStringValue(ops, "key2", "value2")
      verifyStringValue(ops, "key3", "value3")
    }

    "set single key-value pair" in {
      val ops = instance()
      val result = ops.mset(("key", "value"))
      result must be (true)

      verifyStringValue(ops, "key", "value")
    }

    "replace existing keys" in {
      val ops = instance()
      ops.set("key1", "valueX")
      val result = ops.mset(("key1", "value1"), ("key2", "value2"))
      result must be (true)

      verifyStringValue(ops, "key1", "value1")
    }
  }

  "mget(keys)" must {
    "return multiple values" in {
      val ops = instance()
      ops.mset(("key1", "value1"), ("key2", "value2"), ("key3", "value3"))

      val vs = ops.mget("key1", "key2", "key3")
      vs must have size 3

      vs.head.value must be ("value1")

      vs(1).value must be ("value2")

      vs(2).value must be ("value3")
    }

    "return None for non-existing keys" in {
      val ops = instance()
      ops.set("key", "value")

      val vs = ops.mget("key", "key1", "key2")
      vs must have size 3

      vs.head.value must be ("value")

      vs(1) mustBe empty
      vs(2) mustBe empty
    }

    "return None for non-string keys" in {
      val ops = instance()
      ops.set("key", "value")
      ops.internal.put("key1",
        TedisEntry(TedisKeyInfo("key1", None, System.currentTimeMillis()), BadTedisValue))

      val vs = ops.mget("key", "key1")
      vs must have size 2

      vs.head.value must be ("value")

      vs(1) mustBe empty
    }
  }

  private def verifyStringValue(ops: StringOperations, key: String, expect: String) = {
    val v = ops.get(key)
    v.value must be (expect)
  }
}
