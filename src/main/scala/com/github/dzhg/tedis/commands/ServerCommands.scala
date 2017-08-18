package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.protocol.RESP.{BulkStringValue, SimpleStringValue}
import com.github.dzhg.tedis._

object ServerCommands extends TedisErrors {

  case class PingCmd(msg: Option[String]) extends TedisCommand[String] {
    override def exec(storage: TedisStorage): String = msg.getOrElse("PONG")

    override def resultToRESP(v: String): RESP.RESPValue = SimpleStringValue(v)
  }

  val Parser: CommandParser = {
    case CommandParams("PING", params) => params match {
      case Nil => PingCmd(None)
      case BulkStringValue(msg) :: Nil => PingCmd(msg)
      case _ => wrongNumberOfArguments("PING")
    }
  }
}
