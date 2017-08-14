package com.github.dzhg.tedis.server

import com.github.dzhg.tedis.utils.{ServerAndClient, TedisSuite}

class PingSpec extends TedisSuite with ServerAndClient {

  "TedisServer" must {
    "support ping()" in {
      val result = client.ping
      result.value must be ("PONG")
    }
  }
}
