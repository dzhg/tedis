package com.github.dzhg.tedis

import java.util

/**
  * @author dzhg 8/11/17
  */
trait LockedStorage {
  def withLock[T](block: TedisStorage => T): T

  def withLockUnit(block: TedisStorage => Unit): Unit
}

trait LockedMapStorage extends LockedStorage {
  private val internal: TedisStorage = new util.HashMap[String, TedisEntry]()

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
