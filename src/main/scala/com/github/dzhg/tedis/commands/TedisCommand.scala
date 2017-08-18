package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.protocol.RESP.RESPValue
import com.github.dzhg.tedis.TedisStorage

/**
  * @author dzhg 8/11/17
  */
trait TedisCommand[T] extends CommandHelper {
  def exec(storage: TedisStorage): T
  def execToRESP(storage: TedisStorage): RESPValue = resultToRESP(exec(storage))
  def resultToRESP(v: T): RESPValue = OK
  def requireLock: Boolean = true
}

