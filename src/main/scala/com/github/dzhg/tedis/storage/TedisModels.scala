package com.github.dzhg.tedis.storage

/**
  * @author dzhg 8/11/17
  */
case class TedisKeyInfo(name: String, ttl: Option[Long], createdAt: Long)

case class TedisEntry(keyInfo: TedisKeyInfo, value: TedisValue)

