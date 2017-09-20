package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.protocol.RESP.BulkStringValue
import com.github.dzhg.tedis.storage.{TedisEntry, TedisList}
import com.github.dzhg.tedis.{CommandParser, TedisErrors, TedisStorage}

import scala.collection.JavaConverters._

/**
  * @author dzhg 9/18/2017
  */
object ListCommands extends TedisErrors {
  case class RpushCmd(key: String, values: Seq[String]) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = {
      storage.get(key) match {
        case Some(TedisEntry(_, TedisList(list))) =>
          list.addAll(values.asJava)
          list.size()
        case None =>
          storage.put(key, entry(key, TedisList(values)))
          values.size
        case _ => wrongType()
      }
    }
  }

  case class RpopCmd(key: String) extends TedisCommand[Option[String]] with AsBulkStringResult {
    override def exec(storage: TedisStorage): Option[String] = {
      storage.get(key) match {
        case Some(TedisEntry(_, TedisList(list))) =>
          if (list.size() > 0) {
            Some(list.remove(list.size() - 1))
          } else {
            None
          }
        case None => None
        case _ => wrongType()
      }
    }
  }

  case class LlenCmd(key: String) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = {
      storage.get(key) match {
        case Some(TedisEntry(_, TedisList(list))) => list.size()
        case None => 0
        case _ => wrongType()
      }
    }
  }

  val COMMANDS: Set[String] = Set("RPUSH", "RPOP", "LLEN")

  val Parsers: CommandParser = {
    case CommandParams("RPUSH", BulkStringValue(Some(key)) :: vs) if vs.nonEmpty =>
      RpushCmd(key, vs.map {
        case BulkStringValue(Some(v)) => v
        case _ => syntaxError()
      })
    case CommandParams("RPOP", BulkStringValue(Some(key)) :: Nil) => RpopCmd(key)
    case CommandParams("LLEN", BulkStringValue(Some(key)) :: Nil) => LlenCmd(key)
    case CommandParams(cmd, _) if COMMANDS.contains(cmd) => wrongNumberOfArguments(cmd)
  }
}
