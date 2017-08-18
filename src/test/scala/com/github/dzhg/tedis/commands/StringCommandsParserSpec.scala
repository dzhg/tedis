package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.commands.StringCommands._
import com.github.dzhg.tedis.utils.TedisSuite
import com.github.dzhg.tedis.{CommandParser, TedisErrors, TedisException}

import scala.language.implicitConversions

class StringCommandsParserSpec extends TedisSuite with TedisErrors {

  val parser: CommandParser = StringCommands.Parser

  "StringCommandsParser" must {
    "parse 'set key value nx'" in {
      val params = CommandParams("SET", List("key_nx", "value_nx", "nx"))
      parser.isDefinedAt(params) must be (true)

      val cmd = parser.apply(params)
      cmd mustBe a [SetCmd]
      val setCmd = cmd.asInstanceOf[SetCmd]
      setCmd.key must be ("key_nx")
      setCmd.value must be ("value_nx")
      setCmd.onlyIfExists must be (false)
    }

    "parse 'set key value ex 100 xx'" in {
      val params = CommandParams("SET", List(
        "key_ex_xx", "value_ex_xx", "ex", "100", "xx"
      ))

      parser.isDefinedAt(params) must be (true)
      val cmd = parser.apply(params)
      cmd mustBe a [SetCmd]
      val setCmd = cmd.asInstanceOf[SetCmd]
      setCmd.key must be ("key_ex_xx")
      setCmd.value must be ("value_ex_xx")
      setCmd.onlyIfExists must be (true)
      setCmd.time.value must be (100000L)
    }

    "parse 'set key value px 20000'" in {
      val params = CommandParams("SET", List(
        "key_px", "value_px", "px", "20000"
      ))

      parser.isDefinedAt(params) must be (true)
      val cmd = parser.apply(params)
      cmd mustBe a [SimpleSetCmd]
      val setCmd = cmd.asInstanceOf[SimpleSetCmd]
      setCmd.time.value must be (20000L)
    }

    "parse 'set key xx ex 60'" in {
      val params = CommandParams("SET", List(
        "key_xx_ex", "value_xx_ex", "xx", "ex", "60"
      ))

      parser.isDefinedAt(params) must be (true)
      val cmd = parser.apply(params)
      cmd mustBe a [SetCmd]
      val setCmd = cmd.asInstanceOf[SetCmd]
      setCmd.time.value must be (60000L)
      setCmd.onlyIfExists must be (true)
    }

    "throw syntax error for 'set key value nx xx'" in {
      val params = CommandParams("SET", List(
        "key_nx_xx", "value_nx_xx", "nx", "xx"
      ))

      val e = the [TedisException] thrownBy parser.apply(params)
      e.error must be (SYNTAX_ERROR.error)
      e.msg must be (SYNTAX_ERROR.msg)
    }

    "throw syntax error for 'set key value ex 10 nx abc'" in {
      val params = CommandParams("SET", List(
        "key", "value", "ex", "10", "nx", "abc"
      ))

      val e = the [TedisException] thrownBy parser.apply(params)
      e.error must be (SYNTAX_ERROR.error)
      e.msg must be (SYNTAX_ERROR.msg)
    }

    "parse 'mset k1 v1 k2 v2 k3 v3'" in {
      val params = CommandParams("MSET", List(
        "k1", "v1", "k2", "v2", "k3", "v3"
      ))

      parser.isDefinedAt(params) must be (true)
      val cmd = parser(params)
      cmd mustBe a [MsetCmd]
      val msetCmd = cmd.asInstanceOf[MsetCmd]
      val kvs = msetCmd.kvs
      kvs must have size 3
      kvs must be (Seq(("k1", "v1"), ("k2", "v2"), ("k3", "v3")))
    }

    "throw syntax error for 'mset k1 v1 vvv'" in {
      val params = CommandParams("MSET", List(
        "k1", "v1", "vvv"
      ))

      val e = the [TedisException] thrownBy parser(params)
      e.error must be (WRONG_NUMBER_OF_ARGS.error)
      e.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("MSET"))
    }

    "parse 'getset key value'" in {
      val params = CommandParams("GETSET", List("key", "value"))

      parser.isDefinedAt(params) must be (true)
      val cmd = parser(params)
      cmd mustBe a [GetsetCmd]
      val getset = cmd.asInstanceOf[GetsetCmd]
      getset.key must be ("key")
      getset.value must be ("value")
    }

    "throw wrong number of arguments for 'getset key'" in {
      val params = CommandParams("GETSET", List("key"))

      parser.isDefinedAt(params) must be (true)
      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("GETSET"))
    }

    "parse 'setex key 100 value'" in {
      val params = CommandParams("SETEX", List("key", "100", "value"))

      parser.isDefinedAt(params) must be (true)
      val cmd = parser(params)
      cmd mustBe a [SetexCmd]
      val setex = cmd.asInstanceOf[SetexCmd]
      setex.key must be ("key")
      setex.value must be ("value")
      setex.expiry must be (100)
    }

    "throw wrong number of arguments for 'setex key 100'" in {
      val params = CommandParams("SETEX", List("key", "100"))

      parser.isDefinedAt(params) must be (true)
      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("SETEX"))
    }

    "parse 'psetex key 10000 value'" in {
      val params = CommandParams("PSETEX", List("key", "10000", "value"))

      parser.isDefinedAt(params) must be (true)
      val cmd = parser(params)
      cmd mustBe a [PsetexCmd]
      val psetex = cmd.asInstanceOf[PsetexCmd]
      psetex.key must be ("key")
      psetex.value must be ("value")
      psetex.expiry must be (10000)
    }

    "throw wrong number of arguments for 'psetex key 10000'" in {
      val params = CommandParams("PSETEX", List("key", "10000"))

      parser.isDefinedAt(params) must be (true)
      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("PSETEX"))
    }
  }
}
