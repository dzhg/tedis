package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.{TedisErrors, TedisException}
import com.github.dzhg.tedis.commands.KeyCommands.TtlCmd
import com.github.dzhg.tedis.utils.TedisSuite

class KeyCommandsParserSpec extends TedisSuite with TedisErrors {

  private val parser = KeyCommands.Parser

  "KeyCommandsParser" must {
    "parse 'ttl key'" in {
      val params = CommandParams("TTL", List("key"))

      parser.isDefinedAt(params) must be (true)
      val cmd = parser(params)
      cmd mustBe a [TtlCmd]

      cmd.asInstanceOf[TtlCmd].key must be ("key")
    }

    "throw wrong number of arguments error for 'ttl key1 key2'" in {
      val params = CommandParams("TTL", List("key1", "key2"))

      parser.isDefinedAt(params) must be (true)
      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("TTL"))
    }

    "throw wrong number of arguments error for 'ttl'" in {
      val params = CommandParams("TTL", List.empty)

      parser.isDefinedAt(params) must be (true)
      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("TTL"))
    }
  }
}
