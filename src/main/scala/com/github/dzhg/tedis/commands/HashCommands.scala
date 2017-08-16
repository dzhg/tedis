package com.github.dzhg.tedis.commands

import java.util.{HashMap => JMap}

import com.github.dzhg.tedis._
import com.github.dzhg.tedis.storage.TedisHash

/**
  * @author dzhg 8/11/17
  */
object HashCommands extends Helpers with TedisErrors {
  case class HsetCmd(key: String, field: String, value: String) extends TedisCommand[Long] {
    override def exec(storage: TedisStorage): Long = {
      storage.get(key) match {
        case Some(entry) =>
          entry.value match {
            case m: TedisHash => Option(m.put(field, value)).map(_ => 0L).getOrElse(1L)
            case _ => wrongType()
          }
        case None =>
          val m = new JMap[String, String]()
          m.put(field, value)
          storage.put(key, entry(key, m))
          1L
      }
    }
  }

  case class HgetCmd(key: String, field: String) extends TedisCommand[Option[String]] {
    override def exec(storage: TedisStorage): Option[String] = {
      storage.get(key) map { entry =>
        entry.value match {
          case m: TedisHash => Option(m.get(field))
          case _ => wrongType()
        }
      } getOrElse None
    }
  }
}
