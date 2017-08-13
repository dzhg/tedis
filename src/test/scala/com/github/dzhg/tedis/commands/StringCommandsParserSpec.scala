package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.commands.StringCommands.{SetCmd, SimpleSetCmd}
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
  }
}
