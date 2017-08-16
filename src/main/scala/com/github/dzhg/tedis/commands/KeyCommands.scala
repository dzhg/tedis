package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.protocol.RESP
import com.github.dzhg.tedis.protocol.RESP.{BulkStringValue, IntegerValue}
import com.github.dzhg.tedis.{CommandParser, TedisErrors, TedisStorage}
import com.github.dzhg.tedis.storage.{TedisEntry, TedisKeyInfo}

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

  val Parser: CommandParser = {
    case CommandParams("TTL", BulkStringValue(Some(key)) :: Nil) => TtlCmd(key)
    case CommandParams("PTTL", BulkStringValue(Some(key)) :: Nil) => PttlCmd(key)
    case CommandParams(cmd, _) if cmd == "TTL" || cmd == "PTTL" => wrongNumberOfArguments(cmd)
  }
}
