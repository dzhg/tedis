package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.TedisErrors
import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}

class KeyCommandsSpec extends TedisSuite with ServerAndClient with TedisErrors {

  "TedisServer" when {
    "exists(keys)" must {
      "return false if single key does not exist" in {
        val l = client.exists("key")
        l must be (false)
      }

      "return true if key exists" in {
        client.set("key", "value")
        val l = client.exists("key")
        l must be (true)
      }
    }

    "del(keys)" must {
      "remove keys" in {
        client.mset("key1" -> "value1", "key2" -> "value2")
        val l = client.del("key1", "key2")
        l.value must be (2)

        val v = client.mget("key1", "key2")
        v.value must be (List(None, None))
      }

      "return correct number" in {
        client.mset("k1" -> "v1", "k2" -> "v2", "k3" -> "v3")
        val l = client.del("k1", "k2", "k3", "k4")
        l.value must be (3)
      }

      "remove correct keys" in {
        client.mset("k1" -> "v1", "k2" -> "v2")
        val l = client.del("k2", "k3")
        l.value must be (1)

        val v = client.mget("k1", "k2", "k3")
        v.value must be (List(Some("v1"), None, None))
      }
    }
  }
}
