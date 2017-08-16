package com.github.dzhg

/**
  * @author dzhg 8/11/17
  */
package object tedis {
  type TedisCommand[T] = commands.TedisCommand[T]
  type TedisStorage = storage.TedisStorage
  type CommandParams = commands.CommandParams
  type CommandParser = PartialFunction[CommandParams, TedisCommand[_]]
  type DefaultStorage = storage.LockedMapStorage
  private [tedis] val RESP = protocol.RESP
}
