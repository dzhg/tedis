package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.TedisStorage

/**
  * @author dzhg 8/11/17
  */
object KeyCommands {
  case class TtlCmd(key: String) extends TedisCommand[Option[Long]] {
    override def exec(storage: TedisStorage): Option[Long] = Option(storage.get(key)) flatMap (_.keyInfo.ttl)
  }
}
