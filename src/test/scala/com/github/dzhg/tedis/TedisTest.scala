package com.github.dzhg.tedis

/**
  * @author dzhg 8/11/17
  */
class TedisTest(val internal: TedisStorage) extends LockedStorage with CommandExecutor {
  def withLock[T](block: TedisStorage => T): T = {
    internal.synchronized {
      block(internal)
    }
  }

  def withLockUnit(block: TedisStorage => Unit): Unit = {
    internal.synchronized {
      block(internal)
    }
  }
}
