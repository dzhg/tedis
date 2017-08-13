package com.github.dzhg.tedis.operations

import com.github.dzhg.tedis.CommandExecutor
import com.github.dzhg.tedis.commands.ServerCommands.PingCmd

trait ServerOperations {
  this: CommandExecutor =>

  def ping(msg: Option[String]): String = executeUnlocked(PingCmd(msg))
}
