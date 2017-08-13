package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.CommandParser
import com.github.dzhg.tedis.commands.ServerCommands.PingCmd
import com.github.dzhg.tedis.utils.TedisSuite

class ServerCommandsParserSpec extends TedisSuite {

  val parser: CommandParser = ServerCommands.Parser

  "ServerCommandsParser" must {
    "parse 'ping msg'" in {
      val params = CommandParams("PING", List("hello"))
      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [PingCmd]
      val pingCmd = cmd.asInstanceOf[PingCmd]
      pingCmd.msg.value must be ("hello")
    }

    "parse 'ping'" in {
      val params = CommandParams("PING", List.empty)
      parser.isDefinedAt(params) must be (true)
      val cmd = parser(params)
      cmd mustBe a [PingCmd]
      val pingCmd = cmd.asInstanceOf[PingCmd]
      pingCmd.msg mustBe empty
    }
  }

}
