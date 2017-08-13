package com.github.dzhg.tedis.operations

import com.github.dzhg.tedis.CommandExecutor
import com.github.dzhg.tedis.commands.StringCommands._

/**
  * @author dzhg 8/11/17
  */
trait StringOperations {
  this: CommandExecutor =>

  def set(key: String, value: String): Boolean = set(key, value, None)

  def set(key: String, value: String, time: Option[Long]): Boolean = execute(SimpleSetCmd(key, value, time))

  def set(key: String, value: String, onlyIfExists: Boolean, time: Option[Long]): Boolean = execute(SetCmd(key, value, onlyIfExists, time))

  def mset(kvs: (String, String)*): Boolean = execute(MsetCmd(kvs: _*))

  def mget(keys: String*): Seq[Option[String]] = execute(MgetCmd(keys: _*))

  def get(key: String): Option[String] = execute(GetCmd(key))
}
