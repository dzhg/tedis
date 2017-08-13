package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis._
import com.github.dzhg.tedis.protocol.RESP.SimpleStringValue

object MultiCommands extends Helpers with TedisErrors {

  case object MultiCmd extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean = true

    override def resultToRESP(v: Boolean) = SimpleStringValue("OK")

    override def needLock = false
  }

  case object ExecCmd extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean =
      throw TedisException("ERR", "EXEC without MULTI")

    override def needLock = false
  }

  case object DiscardCmd extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean =
      throw TedisException("ERR", "DISCARD without MULTI")

    override def needLock = false
  }

  val Parser: CommandParser = {
    case CommandParams("MULTI", _) => MultiCmd
    case CommandParams("EXEC", _) => ExecCmd
    case CommandParams("DISCARD", _) => DiscardCmd
  }
}
