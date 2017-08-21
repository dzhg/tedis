package com.github.dzhg.tedis.commands

import java.util.{HashMap => JMap}

import com.github.dzhg.tedis._
import com.github.dzhg.tedis.protocol.RESP.{BulkStringValue, IntegerValue}
import com.github.dzhg.tedis.storage.TedisHash

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
          val m = new JMap[String, String]()
          m.put(field, value)
          storage.put(key, entry(key, m))
          1L
      }
    }

    override def resultToRESP(v: Long): RESP.RESPValue = IntegerValue(v)
  }

  case class HgetCmd(key: String, field: String) extends TedisCommand[Option[String]] with AsBulkStringResult {
    override def exec(storage: TedisStorage): Option[String] = {
      storage.get(key) map { entry =>
        entry.value match {
          case m: TedisHash => Option(m.get(field))
          case _ => wrongType()
        }
      } getOrElse None
    }
  }

  val COMMANDS: Set[String] = Set("HSET")

  var Parsers: CommandParser = {
    case CommandParams("HSET", BulkStringValue(Some(key)) :: BulkStringValue(Some(field)) :: BulkStringValue(Some(value)) :: Nil) =>
      HsetCmd(key, field, value)
    case CommandParams("HGET", BulkStringValue(Some(key)) :: BulkStringValue(Some(field)) :: Nil) => HgetCmd(key, field)
    case CommandParams(cmd, _) if COMMANDS.contains(cmd) => wrongNumberOfArguments(cmd)
  }
}
