package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis._
import com.github.dzhg.tedis.protocol.RESP.{BulkStringValue, IntegerValue}
import com.github.dzhg.tedis.storage.{TedisEntry, TedisHash}

/**
  * @author dzhg 8/11/17
  */
object HashCommands extends TedisErrors {
  case class HsetCmd(key: String, field: String, value: String) extends TedisCommand[Long] {
    override def exec(storage: TedisStorage): Long = {
      storage.get(key) match {
        case Some(entry) =>
          entry.value match {
            case m: TedisHash => Option(m.put(field, value)).map(_ => 0L).getOrElse(1L)
            case _ => wrongType()
          }
        case None =>
          storage.put(key, entry(key, TedisHash(field -> value)))
          1L
      }
    }

    override def resultToRESP(v: Long): RESP.RESPValue = IntegerValue(v)
  }

  case class HgetCmd(key: String, field: String) extends TedisCommand[Option[String]] with AsBulkStringResult {
    override def exec(storage: TedisStorage): Option[String] = {
      storage.get(key) map { entry =>
        entry.value match {
          case m: TedisHash => m(field)
          case _ => wrongType()
        }
      } getOrElse None
    }
  }

  case class HsetnxCmd(key: String, field: String, value: String) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = {
      storage.get(key) match {
        case Some(TedisEntry(_, TedisHash(values))) =>
          if (values.containsKey(field)) 0 else {
            values.put(field, value)
            1
          }
        case None =>
          storage.put(key, entry(key, TedisHash(field -> value)))
          1
        case _ => wrongType()
      }
    }
  }

  case class HmsetCmd(key: String, kvs: (String, String)*) extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean = {
      storage.get(key) match {
        case Some(TedisEntry(_, v)) if v.isInstanceOf[TedisHash] =>
          v.asInstanceOf[TedisHash].putAll(kvs.toSeq)
          true
        case None =>
          storage.put(key, entry(key, TedisHash(kvs:_*)))
          true
        case _ => wrongType()
      }
    }
  }

  case class HmgetCmd(key: String, fields: Seq[String]) extends TedisCommand[Seq[Option[String]]] {
    override def exec(storage: TedisStorage): Seq[Option[String]] = {
      storage.get(key) match {
        case Some(TedisEntry(_, TedisHash(kvs))) => fields.map(key => Option(kvs.get(key)))
        case None => fields.map(_ => None)
        case _ => wrongType()
      }
    }

    override def resultToRESP(v: Seq[Option[String]]): RESP.RESPValue = RESP.ArrayValue(Some(v.map(BulkStringValue)))
  }

  val COMMANDS: Set[String] = Set("HSET", "HGET", "HSETNX", "HMSET", "HMGET")

  var Parsers: CommandParser = {
    case CommandParams("HSET", BulkStringValue(Some(key)) :: BulkStringValue(Some(field)) :: BulkStringValue(Some(value)) :: Nil) =>
      HsetCmd(key, field, value)
    case CommandParams("HGET", BulkStringValue(Some(key)) :: BulkStringValue(Some(field)) :: Nil) => HgetCmd(key, field)
    case CommandParams("HSETNX", BulkStringValue(Some(key)) :: BulkStringValue(Some(field)) :: BulkStringValue(Some(value)) :: Nil) =>
      HsetnxCmd(key, field, value)
    case CommandParams("HMSET", BulkStringValue(Some(key)) :: kvs) =>
      if (kvs.size % 2 == 0) {
        val pairs = kvs.grouped(2).map {
          case BulkStringValue(Some(k)) :: BulkStringValue(Some(v)) :: Nil => (k, v)
          case _ => syntaxError()
        }
        HmsetCmd(key, pairs.toSeq: _*)
      } else {
        wrongNumberOfArguments("HMSET")
      }
    case CommandParams("HMGET", BulkStringValue(Some(key)) :: keys) =>
      val ks = keys.map {
        case BulkStringValue(Some(s)) => s
        case _ => syntaxError()
      }
      HmgetCmd(key, ks)
    case CommandParams(cmd, _) if COMMANDS.contains(cmd) => wrongNumberOfArguments(cmd)
  }
}
