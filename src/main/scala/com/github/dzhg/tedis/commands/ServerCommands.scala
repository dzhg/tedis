package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.protocol.RESP.BulkStringValue
import com.github.dzhg.tedis._

object ServerCommands extends Helpers with TedisErrors {

  case class PingCmd(msg: Option[String]) extends TedisCommand[String] {
    override def exec(storage: TedisStorage): String = msg.getOrElse("PONG")
  }

  val Parser: CommandParser = {
    case CommandParams("PING", params) =>
      if (params.isEmpty) {
        PingCmd(None)
      } else {
        params.head match {
          case BulkStringValue(Some(msg)) => PingCmd(Some(msg))
          case _ => syntaxError()
        }
      }
  }
}
