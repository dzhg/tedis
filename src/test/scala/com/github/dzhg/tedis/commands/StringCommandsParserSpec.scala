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

    "parse 'incr key'" in {
      val params = CommandParams("INCR", List("key"))
      parser.isDefinedAt(params) must be (true)
      val cmd = parser(params)
      cmd mustBe a [IncrCmd]
      val incrCmd = cmd.asInstanceOf[IncrCmd]
      incrCmd.key must be ("key")
    }

    "parse 'incrby key 3'" in {
      val params = CommandParams("INCRBY", List("key", "3"))
      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [IncrByCmd]
      val incrByCmd = cmd.asInstanceOf[IncrByCmd]
      incrByCmd.key must be ("key")
      incrByCmd.by must be (3L)
    }

    "parse 'incrbyfloat key 1.25'" in {
      val params = CommandParams("INCRBYFLOAT", List("key", "1.25"))

      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe an [IncrByFloatCmd]
      val incrByFloatCmd = cmd.asInstanceOf[IncrByFloatCmd]
      incrByFloatCmd.key must be ("key")
      incrByFloatCmd.by must be (1.25F)
    }

    "parse 'decr key'" in {
      val params = CommandParams("DECR", List("key"))
      parser.isDefinedAt(params) must be (true)
      val cmd = parser(params)
      cmd mustBe a [DecrCmd]
      val decrCmd = cmd.asInstanceOf[DecrCmd]
      decrCmd.key must be ("key")
    }

    "parse 'decrby key 3'" in {
      val params = CommandParams("DECRBY", List("key", "3"))
      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [DecrByCmd]
      val decrByCmd = cmd.asInstanceOf[DecrByCmd]
      decrByCmd.key must be ("key")
      decrByCmd.by must be (3L)
    }

    "throw wrong number of arguments for 'incr key 123'" in {
      val params = CommandParams("INCR", List("key", "123"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("INCR"))
    }

    "throw wrong number of arguments for 'incrby key'" in {
      val params = CommandParams("INCRBY", List("key"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("INCRBY"))
    }

    "throw wrong number format for 'incrby key abc'" in {
      val params = CommandParams("INCRBY", List("key", "abc"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_FORMAT.error)
      ex.msg must be (WRONG_NUMBER_FORMAT.msg)
    }
  }
}
