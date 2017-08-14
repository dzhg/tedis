package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}

class MGetAndMSetSpc extends TedisSuite with ServerAndClient {

  "TedisServer" must {
    "support mset((k,v)) and mget(k)" in {
      val result = client.mset(("k", "v"))
      result must be (true)

      val vs = client.mget("k")
      vs.value must have size 1

      vs.get.head.value must be ("v")
    }

    "support mset((k1, v1), (k2, v2)) and mget(k1, k2)" in {
      val result = client.mset(("k1", "v1"), ("k2", "v2"))
      result must be (true)

      val vs = client.mget("k1", "k2")
      vs.value must have size 2

      val values = vs.get
      values.head.value must be ("v1")
      values(1).value must be ("v2")
    }

    "support mset((k1, v1), (k2, v2), (k3, v3)) and mget(k1, k2, k3)" in {
      val result = client.mset(("k1", "v1"), ("k2", "v2"), ("k3", "v3"))
      result must be (true)

      val vs = client.mget("k1", "k2", "k3")
      vs.value must have size 3

      val values = vs.get
      values.head.value must be ("v1")
      values(1).value must be ("v2")
      values(2).value must be ("v3")
    }

    "support mset((k1, v1), (k2, v2)) and mget(k1, k2, k3)" in {
      val result = client.mset(("k1", "v1"), ("k2", "v2"))
      result must be (true)

      val vs = client.mget("k1", "k2", "k3")
      vs.value must have size 3

      val values = vs.get
      values.head.value must be ("v1")
      values(1).value must be ("v2")
      values(2) mustBe empty
    }
  }
}
