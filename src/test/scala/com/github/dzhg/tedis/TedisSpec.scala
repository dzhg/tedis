package com.github.dzhg.tedis

import org.scalatest.{MustMatchers, WordSpec}

/**
  * @author dzhg 8/11/17
  */
class TedisSpec extends WordSpec with MustMatchers {

  "Tedis" must {
    "support string operations" in {
      val tedis = Tedis()
      val b = tedis.set("key", "value")
      b must be (true)

      val v = tedis.get("key")
      v mustBe defined
      v.get must be ("value")
    }

    "support hash operations" in {
      val tedis = Tedis()
      tedis.hset("k1", "f1", "v1")
      val v = tedis.hget("k1", "f1")
      v mustBe defined
      v.get must be ("v1")
    }
  }
}
