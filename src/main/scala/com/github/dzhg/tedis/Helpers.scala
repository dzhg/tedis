package com.github.dzhg.tedis

import com.github.dzhg.tedis.storage.{TedisEntry, TedisKeyInfo, TedisValue}

/**
  * @author dzhg 8/11/17
  */
trait Helpers {

  def keyInfo(key: String): TedisKeyInfo = TedisKeyInfo(key, None, System.currentTimeMillis())

  def keyInfo(key: String, time: Long): TedisKeyInfo = TedisKeyInfo(key, Some(time), System.currentTimeMillis())

  def entry(key: String, value: TedisValue): TedisEntry = TedisEntry(keyInfo(key), value)
}
