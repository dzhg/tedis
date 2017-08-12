package com.github.dzhg.tedis

import com.github.dzhg.tedis.commands.CommonCommands.TtlCmd

/**
  * @author dzhg 8/11/17
  */
trait Operations {
  this: CommandExecutor =>

  def ttl(key: String): Option[Long] = execute(TtlCmd(key))
}
