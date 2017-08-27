package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.TedisErrors
import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}

class StrlenSpec extends TedisSuite with ServerAndClient with TedisErrors {

  "TedisServer" when {
    "strlen" must {
      "return correct length for key" in {
        client.set("key", "value123")
        val l = client.strlen("key")
        l.value must be ("value123".length)
      }

      "return 0 if key does not exist" in {
        val l = client.strlen("key")
        l.value must be (0L)
      }

      "throw exception if key does not hold a string" in {
        client.hset1("key", "f1", "v1")
        val ex = the [Exception] thrownBy client.strlen("key")
        ex.getMessage must be (s"${WRONG_TYPE.error} ${WRONG_TYPE.msg}")
      }
    }
  }
}
