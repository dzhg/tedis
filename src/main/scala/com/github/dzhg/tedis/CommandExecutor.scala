package com.github.dzhg.tedis

/**
  * @author dzhg 8/11/17
  */
trait CommandExecutor {
  this: LockedStorage =>

  def execute[T](cmd: TedisCommand[T]): T = withLock { storage =>
    cmd.exec(storage)
  }

  def executeAll(cmds: Seq[TedisCommand[_]]): Seq[Any] = withLock { storage =>
    cmds.map(_.exec(storage))
  }
}
