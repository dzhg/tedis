package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.protocol.RESP
import com.github.dzhg.tedis.protocol.RESP.{BulkStringValue, IntegerValue}
import com.github.dzhg.tedis.storage.{TedisEntry, TedisKeyInfo}
import com.github.dzhg.tedis.{CommandParser, TedisErrors, TedisStorage}

/**
  * @author dzhg 8/11/17
  */
object KeyCommands extends TedisErrors {

  trait BaseTtl extends TedisCommand[Long] {

    val key: String

    def calculateRemains(millis: Long, createdAt: Long): Long = {
      val remains = millis - (System.currentTimeMillis() - createdAt)
      if (remains <= 0) -2L else remains
    }

    override def exec(storage: TedisStorage): Long = storage.get(key) map {
      case TedisEntry(TedisKeyInfo(_, ttl, createdAt), _) =>
        ttl map (millis => calculateRemains(millis, createdAt)) getOrElse -1L
    } getOrElse(-2L)

    override def resultToRESP(v: Long): RESP.RESPValue = v match {
      case x if x < 0 => IntegerValue(x)
      case _ => IntegerValue(convert(v))
    }

    protected def convert(v: Long): Long = v / 1000
  }

  /**
    * The command returns <code>-2</code> if the key does not exist.
    * The command returns <code>-1</code> if the key exists but has no associated expire.
    */
  case class TtlCmd(key: String) extends TedisCommand[Long] with BaseTtl

  case class PttlCmd(key: String) extends TedisCommand[Long] with BaseTtl {
    override protected def convert(v: Long): Long = v
  }

  case class ExistsCmd(keys: String*) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = {
      keys.map(key => storage.get(key).map(_ => 1).getOrElse(0)).sum
    }
  }

  case class DelCmd(keys: List[String]) extends TedisCommand[Long] with AsIntegerResult {
    override def exec(storage: TedisStorage): Long = {
      keys.map(key => storage.removeKey(key)).map(b => if (b) 1 else 0).sum
    }
  }

  val COMMANDS: Set[String] = Set("TTL", "PTTL", "EXISTS")

  val Parser: CommandParser = {
    case CommandParams("TTL", BulkStringValue(Some(key)) :: Nil) => TtlCmd(key)
    case CommandParams("PTTL", BulkStringValue(Some(key)) :: Nil) => PttlCmd(key)
    case CommandParams("EXISTS", keys) if keys.nonEmpty => ExistsCmd(keys map {
      case BulkStringValue(Some(key)) => key
      case _ => syntaxError()
    }: _*)
    case CommandParams("DEL", keys) if keys.nonEmpty => DelCmd(keys map {
      case BulkStringValue(Some(key)) => key
      case _ => syntaxError()
    })
    case CommandParams(cmd, _) if COMMANDS.contains(cmd) => wrongNumberOfArguments(cmd)
  }
}
