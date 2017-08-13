package com.github.dzhg.tedis

import java.util

import com.github.dzhg.tedis.storage.TedisEntry
import org.scalatest.{MustMatchers, WordSpec}

/**
  * @author dzhg 8/11/17
  */
class HashOperationsSpec extends WordSpec with MustMatchers {
  class TedisHashTest(internal: util.Map[String, TedisEntry]) extends TedisTest(internal)
    with HashOperations with StringOperations with KeyOperations

  def instance(): TedisHashTest = new TedisHashTest(new util.HashMap[String, TedisEntry]())

  "hset(key, field, value)" must {
    "set hash value" in {
      val ops = instance()
      val result = ops.hset("h1", "f1", "v1")
      result must be (1L)

      val v = ops.hget("h1", "f1")
      v mustBe defined
      v.get must be ("v1")
    }

    "replace hash value" in {
      val ops = instance()
      ops.hset("h1", "f1", "v1")

      val result = ops.hset("h1", "f1", "v2")
      val v= ops.hget("h1", "f1")
      v mustBe defined
      v.get must be ("v2")
    }

    "throw exception if key is not holding hash" in {
      val ops = instance()
      ops.set("k1", "v1")
      a [TedisException] mustBe thrownBy(ops.hset("k1", "f1", "v1"))
    }
  }
}
