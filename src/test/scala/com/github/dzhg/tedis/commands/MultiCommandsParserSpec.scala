package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.{CommandParser, TedisErrors, TedisException}
import com.github.dzhg.tedis.commands.MultiCommands.{DiscardCmd, ExecCmd, MultiCmd}
import com.github.dzhg.tedis.utils.TedisSuite

class MultiCommandsParserSpec extends TedisSuite with TedisErrors {

  val parser: CommandParser = MultiCommands.Parser

  "MultiCommandsParser" must {
    "parse MULTI" in {
      val params = CommandParams("MULTI", List.empty)

      parser.isDefinedAt(params)
      val cmd = parser(params)
      cmd must be (MultiCmd)
    }

    "throw wrong number of arguments error for 'MULTI some_param'" in {
      val params = CommandParams("MULTI", List("some_param"))

      val e = the [TedisException] thrownBy parser(params)
      e.error must be (WRONG_NUMBER_OF_ARGS.error)
      e.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("MULTI"))
    }

    "parse EXEC" in {
      val params = CommandParams("EXEC", List.empty)

      parser.isDefinedAt(params)
      val cmd = parser(params)
      cmd must be (ExecCmd)
    }

    "throw wrong number of arguments error for 'EXEC some_param'" in {
      val params = CommandParams("EXEC", List("some_param"))

      val e = the [TedisException] thrownBy parser(params)
      e.error must be (WRONG_NUMBER_OF_ARGS.error)
      e.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("EXEC"))
    }

    "parse DISCARD" in {
      val params = CommandParams("DISCARD", List.empty)

      parser.isDefinedAt(params)
      val cmd = parser(params)
      cmd must be (DiscardCmd)
    }

    "throw wrong number of arguments error for 'DISCARD some_param'" in {
      val params = CommandParams("DISCARD", List("some_param"))

      val e = the [TedisException] thrownBy parser(params)
      e.error must be (WRONG_NUMBER_OF_ARGS.error)
      e.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("DISCARD"))
    }
  }
}
