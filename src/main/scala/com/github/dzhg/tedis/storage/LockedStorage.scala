package com.github.dzhg.tedis.storage

import java.util

import com.github.dzhg.tedis.TedisStorage

/**
  * @author dzhg 8/11/17
  */
trait LockedStorage {
  def withoutLock[T](block: TedisStorage => T): T

  def withLock[T](block: TedisStorage => T): T

  def withLockUnit(block: TedisStorage => Unit): Unit
}

trait LockedMapStorage extends LockedStorage {
  private val internal: TedisStorage = new util.HashMap[String, TedisEntry]()

  override def withoutLock[T](block: (TedisStorage) => T): T = block(internal)

  override def withLock[T](block: TedisStorage => T): T = {
    internal.synchronized {
      block(internal)
    }
  }

  override def withLockUnit(block: TedisStorage => Unit): Unit = {
    internal.synchronized {
      block(internal)
    }
  }
}
