package com.github.dzhg.tedis.operations

import com.github.dzhg.tedis.commands.HashCommands.{HgetCmd, HsetCmd}
import com.github.dzhg.tedis.commands.CommandHelper
import com.github.dzhg.tedis.{CommandExecutor, TedisException}

/**
  * @author dzhg 8/11/17
  */
trait HashOperations extends CommandHelper {
  this: CommandExecutor =>

  /**
    * Sets field in the hash stored at key to value.
    *
    * If key does not exist, a new key holding a hash is created.
    * If field already exists in the hash, it is overwritten.
    *
    * @return <code>1</code> if <code>field</code> is a new field in the hash and value was set.
    *         <code>0</code> if <code>field</code> already exists in the hash and the value was updated.
    * @throws TedisException if <code>key</code> is not holding a hash
    */
  def hset(key: String, field: String, value: String): Long = execute(HsetCmd(key, field, value))

  def hget(key: String, field: String): Option[String] = execute(HgetCmd(key, field))
}
