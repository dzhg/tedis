package com.github.dzhg

import java.util

import com.github.dzhg.tedis.storage.TedisEntry

/**
  * @author dzhg 8/11/17
  */
package object tedis {
  type TedisCommand[T] = commands.TedisCommand[T]
  type TedisStorage = util.Map[String, TedisEntry]
  type CommandParams = commands.CommandParams
  type CommandParser = PartialFunction[CommandParams, TedisCommand[_]]
  type DefaultStorage = storage.LockedMapStorage
  val RESP = protocol.RESP
}
