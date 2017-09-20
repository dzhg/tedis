package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.TedisErrors
import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}

class ListSpec extends TedisSuite with ServerAndClient with TedisErrors {

  "TedisServer" when {
    "rpush(key, a, b, c)" must {
      "create a list if key does not exist" in {
        val l = client.rpush("key", "a", "b", "c")
        l.value must be (3)

        val v = client.rpop("key")
        v.value must be ("c")
      }

      "append a b c to the end of list" in {
        client.rpush("key", "0", "1", "2")
        val l = client.rpush("key", "a", "b", "c")
        l.value must be (6)

        val v = client.rpop("key")
        v.value must be ("c")

        client.llen("key").value must be (5)
      }

      "throw error if key is not list" in {
        client.set("key", "value")
        an [Exception] mustBe thrownBy (client.rpush("key", "a", "b", "c"))
      }
    }

    "rpop(key)" must {
      "return and remove the last element from the list" in {
        client.rpush("key", "a", "b", "c")
        client.rpop("key").value must be ("c")
        client.rpop("key").value must be ("b")
        client.rpop("key").value must be ("a")
      }

      "return None if the list is empty" in {
        client.rpush("key", "a")
        client.rpop("key").value must be ("a")
        client.rpop("key") mustBe empty
      }

      "return None if the list does not exist" in {
        client.rpop("key") mustBe empty
      }

      "throw error if key is not list" in {
        client.set("k", "v")
        an [Exception] mustBe thrownBy (client.rpop("k"))
      }
    }

    "llen(key)" must {
      "return len of the list" in {
        client.rpush("k", "1", "2")

        val l = client.llen("k")
        l.value must be (2)
      }

      "return 0 if the key does not exist" in {
        client.llen("key").value must be (0)
      }

      "return 0 if list is empty" in {
        client.rpush("key", "a")
        client.rpop("key")

        val l = client.llen("key")
        l.value must be (0)
      }

      "throw error if key is not a list" in {
        client.set("k", "v")
        an [Exception] mustBe thrownBy (client.llen("k"))
      }
    }
  }
}
