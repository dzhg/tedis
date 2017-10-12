package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.{TedisErrors, TedisException}
import com.github.dzhg.tedis.commands.ListCommands.{RpopCmd, RpushCmd}
import com.github.dzhg.tedis.utils.TedisSuite

class ListCommandsParserSpec extends TedisSuite with TedisErrors {

  private val parser = ListCommands.Parsers

  "ListCommandsParser" must {
    "parse 'rpush key a b c'" in {
      val params = CommandParams("RPUSH", List("key", "a", "b", "c"))
      parser.isDefinedAt(params) must be (true)

      parser(params) must matchPattern {
        case RpushCmd("key", vs) if vs == Seq("a", "b", "c") =>
      }
    }

    "throw wrong number of arguments for 'rpush key'" in {
      val params = CommandParams("RPUSH", List("key"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("RPUSH"))
    }

    "parse 'rpop key'" in {
      val params = CommandParams("RPOP", List("key"))
      parser.isDefinedAt(params) must be (true)

      parser(params) must matchPattern {
        case RpopCmd("key") =>
      }
    }

    "throw wrong number of arguments for 'rpop key abc'" in {
      val params = CommandParams("RPOP", List("key", "abc"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("RPOP"))
    }
  }
}
