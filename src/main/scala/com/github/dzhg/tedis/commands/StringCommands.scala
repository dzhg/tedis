package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis._
import com.github.dzhg.tedis.protocol.RESP
import com.github.dzhg.tedis.protocol.RESP.{BulkStringValue, RESPValue, SimpleStringValue}
import com.github.dzhg.tedis.storage.{TedisEntry, TedisKeyInfo, TedisString}

import scala.collection.JavaConversions._

/**
  * @author dzhg 8/11/17
  */
object StringCommands extends Helpers with TedisErrors {
  case class SimpleSetCmd(key: String, value: String, time: Option[Long]) extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean = {
      val ki = TedisKeyInfo(key, time, System.currentTimeMillis())
      val entry = TedisEntry(ki, value)
      storage.put(key, entry)
      true
    }

    override def resultToRESP(v: Boolean): RESP.RESPValue = SimpleStringValue("OK")
  }

  case class SetCmd(key: String, value: String, onlyIfExists: Boolean, time: Option[Long]) extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean = {
      val entry = storage.get(key)

      if ((onlyIfExists && entry != null) || (!onlyIfExists && entry == null)) {
        SimpleSetCmd(key, value, time).exec(storage)
      } else { false }
    }

    override def resultToRESP(v: Boolean): RESP.RESPValue = if (v) SimpleStringValue("OK") else BulkStringValue(None)
  }

  case class GetCmd(key: String) extends TedisCommand[Option[String]] {
    override def exec(storage: TedisStorage): Option[String] = {
      Option(storage.get(key)) map {
        case TedisEntry(_, s: TedisString) => s
        case _ => wrongType()
      }
    }

    override def resultToRESP(v: Option[String]): RESP.RESPValue = BulkStringValue(v)
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
    case s: TedisString => Some(s)
    case _ => None
  }

  val Parser: CommandParser = {
    case CommandParams("SET", params) => parseSetCmd(params)
    case CommandParams("GET", params) => parseGetCmd(params)
  }

  def parseSetCmd(params: List[RESPValue]): TedisCommand[_] = params match {
    case BulkStringValue(Some(key)) :: BulkStringValue(Some(value)) :: Nil => SimpleSetCmd(key, value, None)
    case _ => syntaxError()
  }

  def parseGetCmd(values: List[RESP.RESPValue]): TedisCommand[_] = values match {
    case BulkStringValue(Some(key)) :: Nil => GetCmd(key)
    case _ => syntaxError()
  }
}
