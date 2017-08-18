package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.protocol.RESP._
import com.github.dzhg.tedis.{CommandParser, TedisErrors}

case class CommandParams(cmd: String, params: List[RESPValue])

trait CommandFactory extends CommandHelper with TedisErrors {
  def make(input: ArrayValue): TedisCommand[_] = input match {
    case ArrayValue(None) => protocolError()
    case ArrayValue(Some(vs)) =>
      vs.head match {
      case BulkStringValue(Some(cmd)) =>
        val params = CommandParams(cmd.toUpperCase(), vs.tail.toList)
        parsers.applyOrElse(params, default)
      case _ => protocolError()
    }
  }

  def parsers: CommandParser =
      ServerCommands.Parser orElse
      StringCommands.Parser orElse
      MultiCommands.Parser orElse
      KeyCommands.Parser

  def default: CommandParser = {
    case x: CommandParams => unknownCommand(x.cmd)
  }
}

object CommandFactory extends CommandFactory
