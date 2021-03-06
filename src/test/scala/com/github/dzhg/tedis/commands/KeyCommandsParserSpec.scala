package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.{TedisErrors, TedisException}
import com.github.dzhg.tedis.commands.KeyCommands.{DelCmd, ExistsCmd, PttlCmd, TtlCmd}
import com.github.dzhg.tedis.protocol.RESP.IntegerValue
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

    "parse 'pttl key'" in {
      val params = CommandParams("PTTL", List("key"))

      parser.isDefinedAt(params) must be (true)
      val cmd = parser(params)
      cmd mustBe a [PttlCmd]

      cmd.asInstanceOf[PttlCmd].key must be ("key")
    }

    "throw wrong number of arguments error for 'pttl key1 key2'" in {
      val params = CommandParams("PTTL", List("key1", "key2"))

      parser.isDefinedAt(params) must be (true)
      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("PTTL"))
    }

    "throw wrong number of arguments error for 'pttl'" in {
      val params = CommandParams("PTTL", List.empty)

      parser.isDefinedAt(params) must be (true)
      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("PTTL"))
    }

    "parse 'exists key'" in {
      val params = CommandParams("EXISTS", List("key"))

      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [ExistsCmd]
      val existsCmd = cmd.asInstanceOf[ExistsCmd]
      existsCmd.keys must be (Seq("key"))
    }

    "error if wrong syntax" in {
      val params = CommandParams("EXISTS", List(IntegerValue(1)))
      a [TedisException] mustBe thrownBy (parser(params))
    }

    "parse 'del key'" in {
      val params = CommandParams("DEL", List("key"))

      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [DelCmd]
      cmd must matchPattern { case DelCmd("key" :: Nil) => }
    }

    "parse 'del key1 key2'" in {
      val params = CommandParams("DEL", List("key1", "key2"))

      val cmd = parser(params)
      cmd must matchPattern { case DelCmd("key1" :: "key2" :: Nil) => }
    }
  }
}
