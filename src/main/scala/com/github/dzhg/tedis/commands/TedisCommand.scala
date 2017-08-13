package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.TedisStorage
import com.github.dzhg.tedis.protocol.RESP.{RESPValue, SimpleStringValue}

/**
  * @author dzhg 8/11/17
  */
trait TedisCommand[T] {
  def exec(storage: TedisStorage): T
  def execToRESP(storage: TedisStorage): RESPValue = resultToRESP(exec(storage))
  def resultToRESP(v: T): RESPValue = SimpleStringValue("PONG")
  def needLock: Boolean = true
}

