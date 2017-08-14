package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis._
import com.github.dzhg.tedis.protocol.RESP.RESPValue

object MultiCommands extends Helpers with TedisErrors {

  case object MultiCmd extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean = true

    override def resultToRESP(v: Boolean): RESPValue = OK

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
    case CommandParams("MULTI", Nil) => MultiCmd
    case CommandParams("EXEC", Nil) => ExecCmd
    case CommandParams("DISCARD", Nil) => DiscardCmd
    case CommandParams(cmd, _) => wrongNumberOfArguments(cmd)
  }
}
