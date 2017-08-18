package com.github.dzhg.tedis

import com.github.dzhg.tedis.protocol.RESP.RESPValue
import com.github.dzhg.tedis.storage.LockedStorage

/**
  * @author dzhg 8/11/17
  */
trait CommandExecutor {
  this: LockedStorage =>

  def executeUnlocked[T](cmd: TedisCommand[T]): T = withoutLock { storage => cmd.exec(storage) }

  def execute[T](cmd: TedisCommand[T]): T = if (cmd.requireLock) withLock { storage => cmd.exec(storage) } else executeUnlocked(cmd)

  def executeAll(cmds: Seq[TedisCommand[_]]): Seq[Any] = withLock { storage => cmds.map(_.exec(storage)) }

  def executeToRESP(cmd: TedisCommand[_]): RESPValue = if (cmd.requireLock) {
    withLock { storage => cmd.execToRESP(storage) }
  } else {
    withoutLock { storage => cmd.execToRESP(storage) }
  }

  def executeAllToRESP(cmds: Seq[TedisCommand[_]]): Seq[RESPValue] = withLock { storage => cmds.map(_.execToRESP(storage))}
}
