package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis._
import com.github.dzhg.tedis.protocol.RESP.{BulkStringValue, IntegerValue}
import com.github.dzhg.tedis.storage.{TedisEntry, TedisHash}

import scala.collection.JavaConverters._
import scala.util.{Success, Try}

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

  case class HexistsCmd(key: String, field: String) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = {
      storage.get(key) match {
        case Some(TedisEntry(_, TedisHash(kvs))) => if (kvs.containsKey(field)) 1 else 0
        case None => 0
        case _ => wrongType()
      }
    }
  }

  case class HdelCmd(key: String, fields: Seq[String]) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = {
      storage.get(key) match {
        case Some(TedisEntry(_, TedisHash(hash))) =>
          fields.foldLeft(0L) { (count, field) =>
            if (hash.containsKey(field)) {
              hash.remove(field)
              count + 1
            } else {
              count
            }
          }
        case None => 0
        case _ => wrongType()
      }
    }
  }

  case class HlenCmd(key: String) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = {
      storage.get(key) match {
        case Some(TedisEntry(_, TedisHash(hash))) => hash.keySet().size().toLong
        case None => 0L
        case _ => wrongType()
      }
    }
  }

  case class HkeysCmd(key: String) extends TedisCommand[Seq[String]] with AsArrayResult {
    override def exec(storage: TedisStorage): Seq[String] = {
      storage.get(key) match {
        case Some(TedisEntry(_, TedisHash(hash))) => hash.keySet().asScala.toSeq
        case None => Seq.empty
        case _ => wrongType()
      }
    }
  }

  case class HvalsCmd(key: String) extends TedisCommand[Seq[String]] with AsArrayResult {
    override def exec(storage: TedisStorage): Seq[String] = {
      storage.get(key) match {
        case Some(TedisEntry(_, TedisHash(hash))) => hash.values().asScala.toSeq
        case None => Seq.empty
        case _ => wrongType()
      }
    }
  }

  case class HgetallCmd(key: String) extends TedisCommand[Seq[String]] with AsArrayResult {
    override def exec(storage: TedisStorage): Seq[String] = {
      storage.get(key) match {
        case Some(TedisEntry(_, TedisHash(hash))) => hash.entrySet().asScala.toSeq flatMap { entry =>
          Seq(entry.getKey, entry.getValue)
        }
        case None => Seq.empty
        case _ => wrongType()
      }
    }
  }

  case class HincrbyCmd(key: String, field: String, value: Long) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = {
      storage.get(key) match {
        case None =>
          storage.put(key, entry(key, TedisHash((field, value.toString))))
          value
        case Some(TedisEntry(_, TedisHash(hash))) =>
          Option(hash.get(field)) map { str =>
            Try(str.toLong) map { v =>
              val nv = v + value
              hash.put(field, nv.toString)
              nv
            } getOrElse hashValueNotAnInteger()
          } getOrElse {
            hash.put(field, value.toString)
            value
          }
        case _ => wrongType()
      }
    }
  }

  case class HincrybyfloatCmd(key: String, field: String, value: Float) extends TedisCommand[Float] with AsFloatResult {
    override def exec(storage: TedisStorage): Float = {
      storage.get(key) match {
        case None =>
          storage.put(key, entry(key, TedisHash((field, value.toString))))
          value
        case Some(TedisEntry(_, TedisHash(hash))) =>
          Option(hash.get(field)) map { str =>
            Try(str.toFloat) map { v =>
              val nv = v + value
              hash.put(field, nv.toString)
              nv
            } getOrElse hashValueNotAnInteger()
          } getOrElse {
            hash.put(field, value.toString)
            value
          }
        case _ => wrongType()
      }
    }
  }

  val COMMANDS: Set[String] = Set("HSET", "HGET", "HSETNX", "HMSET", "HMGET", "HEXISTS", "HDEL", "HLEN",
    "HKEYS", "HVALS", "HGETALL", "HINCRBY", "HINCRBYFLOAT")

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
    case CommandParams("HEXISTS", BulkStringValue(Some(key)) :: BulkStringValue(Some(field)) :: Nil) => HexistsCmd(key, field)
    case CommandParams("HDEL", BulkStringValue(Some(key)) :: BulkStringValue(Some(field)) :: others) =>
      val otherFields = others.map {
        case BulkStringValue(Some(s)) => s
        case _ => syntaxError()
      }
      HdelCmd(key, field +: otherFields)
    case CommandParams("HLEN", BulkStringValue(Some(key)) :: Nil) => HlenCmd(key)
    case CommandParams("HKEYS", BulkStringValue(Some(key)) :: Nil) => HkeysCmd(key)
    case CommandParams("HVALS", BulkStringValue(Some(key)) :: Nil) => HvalsCmd(key)
    case CommandParams("HGETALL", BulkStringValue(Some(key)) :: Nil) => HgetallCmd(key)
    case CommandParams("HINCRBY", BulkStringValue(Some(key)) :: BulkStringValue(Some(field)) :: BulkStringValue(Some(value)) :: Nil) =>
      Try(value.toLong) map (HincrbyCmd(key, field, _)) getOrElse numberFormatError()
    case CommandParams("HINCRBYFLOAT", BulkStringValue(Some(key)) :: BulkStringValue(Some(field)) :: BulkStringValue(Some(value)) :: Nil) =>
      Try(value.toFloat) map (HincrybyfloatCmd(key, field, _)) getOrElse floatNumberFormatError()
    case CommandParams(cmd, _) if COMMANDS.contains(cmd) => wrongNumberOfArguments(cmd)
  }
}
