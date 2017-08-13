package com.github.dzhg.tedis

import com.github.dzhg.tedis.commands.ServerCommands.PingCmd

trait ServerOperations {
  this: CommandExecutor =>

  def ping(msg: Option[String]): String = executeUnlocked(PingCmd(msg))
}
