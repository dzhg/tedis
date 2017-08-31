package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.TedisErrors
import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}

class RangeSpec extends TedisSuite with ServerAndClient with TedisErrors {

  "TedisServer" when {
    "setrange" must {
      "set the range correctly" in {
        client.set("key", "Hello World")
        val length = client.setrange("key", 6, "Tedis!")
        length.value must be (12)

        val v = client.get("key")
        v.value must be ("Hello Tedis!")
      }

      "padded with zero-bytes if the length of original value is less than offset" in {
        client.set("key", "ABC")
        val length = client.setrange("key", 6, "Tedis!")
        length.value must be (12)

        val v = client.get("key")
        v.value must be ("ABC\u0000\u0000\u0000Tedis!")
      }

      "padded with zero-bytes if key does not exist" in {
        val length = client.setrange("key", 6, "Tedis!")
        length.value must be (12)

        val v = client.get("key")
        v.value must be ("\u0000\u0000\u0000\u0000\u0000\u0000Tedis!")
      }

      "return error if key is not string" in {
        client.hset1("key", "f1", "v1")
        val ex = the [Exception] thrownBy client.setrange("key", 6, "Tedis!")
        ex.getMessage must be (s"${WRONG_TYPE.error} ${WRONG_TYPE.msg}")
      }
    }

    "getrange" must {
      "return correct range" in {
        client.set("key", "abcdef")
        val v = client.getrange("key", 3, 5)
        v.value must be ("def")
      }

      "return empty string if start > end" in {
        client.set("key", "abcdef")
        val v = client.getrange("key", 5, 3)
        v.value must be ("")
      }

      "return correct range with negative end" in {
        client.set("key", "abcdef")
        val v = client.getrange("key", 0, -4)
        v.value must be ("abc")
      }

      "return correct range with negative start" in {
        client.set("key", "abcdef")
        val v = client.getrange("key", -2, 100)
        v.value must be ("ef")
      }

      "return correct range with negative start and end"in {
        client.set("key", "abcdef")
        val v = client.getrange("key", -3, -1)
        v.value must be ("def")
      }

      "return empty string if key does not exist" in {
        val v = client.getrange("key", 0, 100)
        v.value must be ("")
      }
    }
  }
}
