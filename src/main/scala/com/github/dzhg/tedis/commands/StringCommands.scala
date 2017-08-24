package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis._
import com.github.dzhg.tedis.protocol.RESP
import com.github.dzhg.tedis.protocol.RESP.{ArrayValue, BulkStringValue, RESPValue, SimpleStringValue}
import com.github.dzhg.tedis.storage.{TedisEntry, TedisKeyInfo, TedisString}

import scala.util.Try

/**
  * @author dzhg 8/11/17
  */
object StringCommands extends TedisErrors {
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

      if ((onlyIfExists && entry.isDefined) || (!onlyIfExists && entry.isEmpty)) {
        SimpleSetCmd(key, value, time).exec(storage)
      } else { false }
    }

    override def resultToRESP(v: Boolean): RESP.RESPValue = if (v) OK else BulkStringValue(None)
  }

  case class GetCmd(key: String) extends TedisCommand[Option[String]] with AsBulkStringResult {
    override def exec(storage: TedisStorage): Option[String] = {
      storage.get(key) map {
        case TedisEntry(_, s: TedisString) => s
        case _ => wrongType()
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
      keys.map { k => storage.get(k) flatMap extractStringValue }
    }

    override def resultToRESP(vs: Seq[Option[String]]): RESPValue = ArrayValue(Some(vs.map(BulkStringValue)))
  }

  case class SetexCmd(key: String, expiry: Long, value: String) extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean = {
      storage.put(key, TedisEntry(keyInfo(key, expiry * 1000), TedisString(value)))
      true
    }
  }

  case class PsetexCmd(key: String, expiry: Long, value: String) extends TedisCommand[Boolean] {
    override def exec(storage: TedisStorage): Boolean = {
      storage.put(key, TedisEntry(keyInfo(key, expiry), TedisString(value)))
      true
    }
  }

  case class GetsetCmd(key: String, value: String) extends TedisCommand[Option[String]] with AsBulkStringResult {
    override def exec(storage: TedisStorage): Option[String] = {
      storage.put(key, entry(key, TedisString(value))).map {
        case TedisEntry(_, TedisString(v)) => v
        case _ => wrongType()
      }
    }
  }

  case class IncrCmd(key: String) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = IncrByCmd(key, 1L).exec(storage)
  }

  trait IncrBy[IN, OUT] {
    def incrBy(v: String, by: IN): IN
    def output(v: IN): OUT

    def exec(storage: TedisStorage, key: String, by: IN): OUT = {
      storage.get(key) match {
        case Some(TedisEntry(keyInfo, TedisString(v))) =>
          Try(incrBy(v, by)).map(value => {
            storage.put(key, TedisEntry(keyInfo, TedisString(value.toString)))
            output(value)
          }).getOrElse(numberFormatError())
        case None =>
          SimpleSetCmd(key, by.toString, None).exec(storage)
          output(by)
        case _ => wrongType()
      }
    }
  }

  case class IncrByCmd(key: String, by: Long) extends TedisCommand[Long] with AsIntegerResult with IncrBy[Long, Long] {
    override def exec(storage: TedisStorage): Long = exec(storage, key, by)

    override def incrBy(v: String, by: Long): Long = v.toLong + by

    override def output(v: Long): Long = v
  }

  case class IncrByFloatCmd(key: String, by: Float) extends TedisCommand[String] with IncrBy[Float, String] {
    override def exec(storage: TedisStorage): String = exec(storage, key, by)

    override def incrBy(v: String, by: Float): Float = v.toFloat + by

    override def output(v: Float): String = v.toString

    override def resultToRESP(v: String): RESPValue = BulkStringValue(Some(v))
  }

  case class DecrCmd(key: String) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = IncrByCmd(key, -1).exec(storage)
  }

  case class DecrByCmd(key: String, by: Long) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = IncrByCmd(key, -by).exec(storage)
  }

  case class MsetnxCmd(kvs: (String, String)*) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = {
      val keys = kvs.map(_._1)
      val existed = MgetCmd(keys: _*).exec(storage).exists(_.isDefined)

      if (!existed && MsetCmd(kvs: _*).exec(storage)) 1
      else 0
    }
  }

  case class SetrangeCmd(key: String, offset: Long, value: String) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = {
      storage.get(key) match {
        case Some(TedisEntry(keyInfo, TedisString(str))) => setrange(storage, keyInfo, offset, str, value)
        case None => setrange(storage, keyInfo(key), offset, "", value)
        case _ => wrongType()
      }
    }

    private def setrange(storage: TedisStorage, keyInfo: TedisKeyInfo, offset: Long, original: String, value: String): Long = {
      val padded = padding(original, offset)
      val result = padded.substring(0, offset.toInt).concat(value)
      storage.put(keyInfo.name, TedisEntry(keyInfo, TedisString(result)))
      result.length.toLong
    }

    private def padding(v: String, length: Long): String = {
      if (v.length >= length) v
      else 0.to(length.toInt - v.length).foldLeft(v) { (s, _) => s + "\0" }
    }
  }

  case class GetrangeCmd(key: String, start: Long, end: Long) extends TedisCommand[String] with AsNonNilBulkStringResult {
    override def exec(storage: TedisStorage): String = {
      storage.get(key) map {
        case TedisEntry(_, TedisString(str)) =>
          val (beginIdx, endIdx) = calculateIndex(str.length, start, end)
          str.substring(beginIdx, endIdx)
        case _ => wrongType()
      } getOrElse ""
    }

    // start and end are both inclusive, so the calculated end index shall be increased by 1
    private def calculateIndex(length: Int, start: Long, end: Long): (Int, Int) = {
      val positiveStart = if (start < 0) length + start.toInt else start.toInt
      val positiveEnd = if (end < 0) length + end.toInt else end.toInt

      if (positiveStart > positiveEnd) (0, 0)
      else if (positiveEnd + 1 > length) (positiveStart, length)
      else (positiveStart, positiveEnd + 1)
    }
  }

  private def extractStringValue(entry: TedisEntry): Option[String] = entry.value match {
    case s: TedisString => Some(s)
    case _ => None
  }

  val COMMANDS: Set[String] = Set("SET", "GET", "MSET", "MGET", "GETSET", "SETEX", "PSETEX", "INCR", "INCRBY", "DECRBY",
    "INCRBYFLOAT", "MSETNX", "SETRANGE", "GETRANGE")

  val Parser: CommandParser = {
    case CommandParams("SET", params) => parseSetCmd(params)
    case CommandParams("GET", params) => parseGetCmd(params)
    case CommandParams("MSET", params) => parseMsetCmds[MsetCmd]("MSET", params, MsetCmd.apply)
    case CommandParams("MGET", params) => MgetCmd(params.map {
      case BulkStringValue(Some(k)) => k
      case _ => syntaxError()
    }: _*)
    case CommandParams("GETSET", BulkStringValue(Some(key)) :: BulkStringValue(Some(value)) :: Nil) => GetsetCmd(key, value)
    case CommandParams("SETEX", BulkStringValue(Some(key)) :: BulkStringValue(Some(expiry)) :: BulkStringValue(Some(value)) :: Nil) =>
      Try(SetexCmd(key, expiry.toLong, value)).getOrElse(numberFormatError())
    case CommandParams("PSETEX", BulkStringValue(Some(key)) :: BulkStringValue(Some(expiry)) :: BulkStringValue(Some(value)) :: Nil) =>
      Try(PsetexCmd(key, expiry.toLong, value)).getOrElse(numberFormatError())
    case CommandParams("INCR", BulkStringValue(Some(key)) :: Nil) => IncrCmd(key)
    case CommandParams("DECR", BulkStringValue(Some(key)) :: Nil) => DecrCmd(key)
    case CommandParams("INCRBY", BulkStringValue(Some(key)) :: BulkStringValue(Some(by)) :: Nil) =>
      Try(IncrByCmd(key, by.toLong)).getOrElse(numberFormatError())
    case CommandParams("DECRBY", BulkStringValue(Some(key)) :: BulkStringValue(Some(by)) :: Nil) =>
      Try(DecrByCmd(key, by.toLong)).getOrElse(numberFormatError())
    case CommandParams("INCRBYFLOAT", BulkStringValue(Some(key)) :: BulkStringValue(Some(by)) :: Nil) =>
      Try(IncrByFloatCmd(key, by.toFloat)).getOrElse(numberFormatError())
    case CommandParams("MSETNX", params) => parseMsetCmds[MsetnxCmd]("MSETNX", params, MsetnxCmd.apply)
    case CommandParams("SETRANGE", BulkStringValue(Some(key)) :: BulkStringValue(Some(offset)) :: BulkStringValue(Some(value)) :: Nil) =>
      Try(SetrangeCmd(key, offset.toLong, value)).getOrElse(numberFormatError())
    case CommandParams("GETRANGE", BulkStringValue(Some(key)) :: BulkStringValue(Some(start)) :: BulkStringValue(Some(end)) :: Nil) =>
      Try(GetrangeCmd(key, start.toLong, end.toLong)).getOrElse(numberFormatError())
    case CommandParams(cmd, _) if COMMANDS.contains(cmd) => wrongNumberOfArguments(cmd)
  }

  def parseMsetCmds[T](cmd: String, params: List[RESPValue], f: Seq[(String, String)] => T): T = {
    if (params.size % 2 != 0) {
      wrongNumberOfArguments(cmd)
    } else {
      val kvs = params.grouped(2).foldLeft(Seq.empty[(String, String)]) { (kvs, pair) =>
        pair match {
          case BulkStringValue(Some(key)) :: BulkStringValue(Some(value)) :: Nil => kvs :+ (key, value)
          case _ => syntaxError()
        }
      }
      f(kvs)
    }
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
