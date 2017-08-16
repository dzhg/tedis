package com.github.dzhg.tedis.operations

import com.github.dzhg.tedis.TedisStorage
import com.github.dzhg.tedis.storage.HashMapTedisStorage
import com.github.dzhg.tedis.utils.{TedisSuite, TedisTest}

class ServerOperationsSpec extends TedisSuite {
  class TedisServerOpsTest(internal: TedisStorage) extends TedisTest(internal) with ServerOperations

  def instance(): TedisServerOpsTest= new TedisServerOpsTest(new HashMapTedisStorage)

  "ping" must {
    "PONG" in {
      val ops = instance()
      val pong = ops.ping(None)

      pong must be ("PONG")
    }

    "response message" in {
      val ops = instance()
      val msg = ops.ping(Some("hello"))

      msg must be ("hello")
    }
  }
}
