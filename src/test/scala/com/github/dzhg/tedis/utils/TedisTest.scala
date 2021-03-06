package com.github.dzhg.tedis.utils

import com.github.dzhg.tedis.storage.LockedStorage
import com.github.dzhg.tedis.{CommandExecutor, TedisStorage}

/**
  * @author dzhg 8/11/17
  */
class TedisTest(val internal: TedisStorage) extends LockedStorage with CommandExecutor {

  override def withoutLock[T](block: (TedisStorage) => T): T = block(internal)

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
