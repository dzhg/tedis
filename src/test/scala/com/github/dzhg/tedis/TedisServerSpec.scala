package com.github.dzhg.tedis

import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}

class TedisServerSpec extends TedisSuite with ServerAndClient {

  "TedisServer" must {
    "accept client connection" in {
      val result = client.ping
      result must be (Some("PONG"))
    }

    "support simple string commands (set, get)" in {
      val setResult = client.set("key1", "value1")
      setResult must be (true)
      val value = client.get("key1")
      value mustBe defined
      value.get must be ("value1")
    }
  }
}
