package com.github.dzhg.tedis.operations

import com.github.dzhg.tedis.CommandExecutor
import com.github.dzhg.tedis.commands.KeyCommands.TtlCmd

/**
  * @author dzhg 8/11/17
  */
trait KeyOperations {
  this: CommandExecutor =>

  def ttl(key: String): Long = execute(TtlCmd(key))
}
