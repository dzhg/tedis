package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.TedisErrors
import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}

class AppendSpec extends TedisSuite with ServerAndClient with TedisErrors {

  "TedisServer" when {
    "append" must {
      "return correct length" in {
        client.set("key", "Hello ")
        val l = client.append("key", "Tedis!")
        l.value must be ("Hello Tedis!".length)
      }

      "return correct length if key does not exist" in {
        val l = client.append("key", "hello")
        l.value must be ("hello".length)
      }

      "throw exception if key is not holding a string" in {
        client.hset1("key", "f1", "v1")
        a [Exception] mustBe thrownBy (client.append("key", "hello"))
      }
    }
  }
}
