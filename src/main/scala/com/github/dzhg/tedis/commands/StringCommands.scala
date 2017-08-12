package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis._

import scala.collection.JavaConversions._

/**
  * @author dzhg 8/11/17
  */
object StringCommands extends Helpers {
  case class SimpleSetCmd(key: String, value: String, time: Option[Long]) extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean = {
      val ki = TedisKeyInfo(key, time, System.currentTimeMillis())
      val entry = TedisEntry(ki, value)
      storage.put(key, entry)
      true
    }
  }

  case class SetCmd(key: String, value: String, onlyIfExists: Boolean, time: Option[Long]) extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean = {
      val entry = storage.get(key)

      if ((onlyIfExists && entry != null) || (!onlyIfExists && entry == null)) {
        SimpleSetCmd(key, value, time).exec(storage)
      } else { false }
    }
  }

  case class GetCmd(key: String) extends TedisCommand[Option[String]] {
    override def exec(storage: TedisStorage): Option[String] = {
      Option(storage.get(key)) map {
        case TedisEntry(_, s: StringValue) => s
        case _ => throw wrongType
      }
    }
  }

  case class MsetCmd(kvs: (String, String)*) extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean = {
      val t = System.currentTimeMillis()
      val m = kvs.toSeq.map(kv => (kv._1, (kv._1, kv._2))).toMap
        .mapValues(v => TedisEntry(TedisKeyInfo(v._1, None, t), v._2))
      storage.putAll(m)
      true
    }
  }

  case class MgetCmd(keys: String*) extends TedisCommand[Seq[Option[String]]] {
    override def exec(storage: TedisStorage): Seq[Option[String]] = {
      keys.map { k => Option(storage.get(k)) flatMap extractStringValue }
    }
  }

  private def extractStringValue(entry: TedisEntry): Option[String] = entry.value match {
    case s: StringValue => Some(s)
    case _ => None
  }
}
