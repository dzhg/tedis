package com.github.dzhg.tedis.operations

import java.util

import com.github.dzhg.tedis.TedisStorage
import com.github.dzhg.tedis.storage.TedisEntry
import com.github.dzhg.tedis.utils.{TedisSuite, TedisTest}

class ServerOperationsSpec extends TedisSuite {
  class TedisServerOpsTest(internal: TedisStorage) extends TedisTest(internal) with ServerOperations

  def instance(): TedisServerOpsTest= new TedisServerOpsTest(new util.HashMap[String, TedisEntry]())

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
