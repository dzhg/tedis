package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis._
import com.github.dzhg.tedis.protocol.RESP
import com.github.dzhg.tedis.protocol.RESP.{ArrayValue, BulkStringValue, RESPValue, SimpleStringValue}
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

    override def resultToRESP(v: Boolean): RESP.RESPValue = OK
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

    override def resultToRESP(v: Boolean): RESPValue = OK
  }

  case class MgetCmd(keys: String*) extends TedisCommand[Seq[Option[String]]] {
    override def exec(storage: TedisStorage): Seq[Option[String]] = {
      keys.map { k => Option(storage.get(k)) flatMap extractStringValue }
    }

    override def resultToRESP(vs: Seq[Option[String]]): RESPValue = ArrayValue(Some(vs.map(BulkStringValue)))
  }

  private def extractStringValue(entry: TedisEntry): Option[String] = entry.value match {
    case s: TedisString => Some(s)
    case _ => None
  }

  val Parser: CommandParser = {
    case CommandParams("SET", params) => parseSetCmd(params)
    case CommandParams("GET", params) => parseGetCmd(params)
    case CommandParams("MSET", params) =>
      if (params.size % 2 != 0) {
        syntaxError()
      } else {
        val kvs = params.grouped(2).foldLeft(Seq.empty[(String, String)]) { (kvs, pair) =>
          pair match {
            case BulkStringValue(Some(key)) :: BulkStringValue(Some(value)) :: Nil => kvs :+ (key, value)
            case _ => syntaxError()
          }
        }
        MsetCmd(kvs: _*)
      }
    case CommandParams("MGET", params) => MgetCmd(params.map {
      case BulkStringValue(Some(k)) => k
      case _ => syntaxError()
    }: _*)
  }

  def parseSetCmd(params: List[RESPValue]): TedisCommand[_] = params match {
    case BulkStringValue(Some(key)) :: BulkStringValue(Some(value)) :: Nil => SimpleSetCmd(key, value, None)
    case BulkStringValue(Some(key)) :: BulkStringValue(Some(value)) :: others =>
      // has EX PX NX or XX
      // there are only 3 cases:
      // 1: only NX or XX
      // 2: EX <SECONDS> | PX <MILLIS>
      // 3: case 1 with NX or XX
      others.size match {
        case 1 => others.head match {
          case BulkStringValue(Some(flag)) =>
            SetCmd(key, value, onlyIfExists = "XX" == flag.toUpperCase(), None)
          case _ => syntaxError()
        }
        case 2 => (others.head, others(1)) match {
          case (BulkStringValue(Some(exFlag)), BulkStringValue(Some(exValue))) => exFlag.toUpperCase() match {
            case "EX" => SimpleSetCmd(key, value, Some(exValue.toLong * 1000))
            case "PX" => SimpleSetCmd(key, value, Some(exValue.toLong))
            case _ => syntaxError()
          }
          case _ => syntaxError()
        }
        case 3 => (others.head, others(1), others(2)) match {
          case (BulkStringValue(Some(v1)), BulkStringValue(Some(v2)), BulkStringValue(Some(v3))) =>
            val (a, b, c) = (v1.toUpperCase(), v2.toUpperCase(), v3.toUpperCase())
            if ((a == "NX" || a == "XX") && (b == "EX" || b == "PX")) {
              val ex = if (b == "PX") c.toLong else c.toLong * 1000
              SetCmd(key, value, a == "XX", Some(ex))
            } else if ((a == "EX" || a == "PX") && (c == "NX" || c == "XX")) {
              val ex = if (a == "PX") b.toLong else b.toLong * 1000
              SetCmd(key, value, c == "XX", Some(ex))
            } else {
              syntaxError()
            }
          case _ => syntaxError()
        }
        case _ => syntaxError()
      }
    case _ => syntaxError()
  }

  def parseGetCmd(values: List[RESP.RESPValue]): TedisCommand[_] = values match {
    case BulkStringValue(Some(key)) :: Nil => GetCmd(key)
    case _ => syntaxError()
  }
}
