package com.github.dzhg.tedis.commands

import java.util

import com.github.dzhg.tedis._

/**
  * @author dzhg 8/11/17
  */
object HashCommands extends Helpers {
  case class HsetCmd(key: String, field: String, value: String) extends TedisCommand[Long] {
    override def exec(storage: TedisStorage): Long = {
      Option(storage.get(key)) match {
        case Some(entry) =>
          entry.value match {
            case m: HashValue => Option(m.put(field, value)).map(_ => 0L).getOrElse(1L)
            case _ => throw wrongType
          }
        case None =>
          val m = new util.HashMap[String, String]()
          m.put(field, value)
          storage.put(key, entry(key, m))
          1L
      }
    }
  }

  case class HgetCmd(key: String, field: String) extends TedisCommand[Option[String]] {
    override def exec(storage: TedisStorage): Option[String] = {
      Option(storage.get(key)) map { entry =>
        entry.value match {
          case m: HashValue => Option(m.get(field))
          case _ => throw wrongType
        }
      } getOrElse None
    }
  }
}
