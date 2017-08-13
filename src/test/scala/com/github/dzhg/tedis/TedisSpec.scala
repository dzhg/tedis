package com.github.dzhg.tedis

import com.github.dzhg.tedis.utils.TedisSuite

/**
  * @author dzhg 8/11/17
  */
class TedisSpec extends TedisSuite {

  "Tedis" must {
    "support string operations" in {
      val tedis = Tedis()
      val b = tedis.set("key", "value")
      b must be (true)

      val v = tedis.get("key")
      v.value must be ("value")
    }

    "support hash operations" in {
      val tedis = Tedis()
      tedis.hset("k1", "f1", "v1")
      val v = tedis.hget("k1", "f1")
      v.value must be ("v1")
    }
  }
}
