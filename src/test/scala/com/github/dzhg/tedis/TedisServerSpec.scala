package com.github.dzhg.tedis

import org.scalatest.{MustMatchers, WordSpec}

class TedisServerSpec extends WordSpec with MustMatchers with WithTedisServer {

  "TedisServer" must {
    "accept client connection" in {
      val result = redisClient.ping
      result must be (Some("PONG"))
    }

    "support simple string commands (set, get)" in {
      val setResult = redisClient.set("key1", "value1")
      setResult must be (true)
      val value = redisClient.get("key1")
      value mustBe defined
      value.get must be ("value1")
    }
  }
}
