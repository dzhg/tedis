package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.commands.HashCommands.HsetCmd
import com.github.dzhg.tedis.{CommandParser, TedisErrors}
import com.github.dzhg.tedis.utils.TedisSuite

class HashCommandsParserSpec extends TedisSuite with TedisErrors {

  val parser: CommandParser = HashCommands.Parsers

  "HashCommandsParser" must {
    "parse 'hset key field value'" in {
      val params = CommandParams("HSET", List("key", "field", "value"))
      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [HsetCmd]
      val hsetCmd = cmd.asInstanceOf[HsetCmd]
      hsetCmd.key must be ("key")
      hsetCmd.field must be ("field")
      hsetCmd.value must be ("value")
    }
  }
}
