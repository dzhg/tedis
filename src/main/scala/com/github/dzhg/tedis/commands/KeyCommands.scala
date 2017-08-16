package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.protocol.RESP
import com.github.dzhg.tedis.protocol.RESP.{BulkStringValue, IntegerValue}
import com.github.dzhg.tedis.{CommandParser, TedisErrors, TedisStorage}
import com.github.dzhg.tedis.storage.{TedisEntry, TedisKeyInfo}

/**
  * @author dzhg 8/11/17
  */
object KeyCommands extends TedisErrors {

  /**
    * The command returns <code>-2</code> if the key does not exist.
    * The command returns <code>-1</code> if the key exists but has no associated expire.
    */
  case class TtlCmd(key: String) extends TedisCommand[Long] {
    override def exec(storage: TedisStorage): Long = storage.get(key) map {
      case TedisEntry(TedisKeyInfo(_, ttl, createdAt), _) =>
        ttl map (millis => calculateRemains(millis, createdAt)) getOrElse -1L
    } getOrElse(-2L)

    private def calculateRemains(millis: Long, createdAt: Long): Long = {
      val remains = millis - (System.currentTimeMillis() - createdAt)
      if (remains <= 0) -2L else remains
    }

    override def resultToRESP(v: Long): RESP.RESPValue = v match {
      case x if x < 0 => IntegerValue(x.toInt)
      case _ => IntegerValue((v / 1000).toInt)
    }
  }

  val Parser: CommandParser = {
    case CommandParams("TTL", BulkStringValue(Some(key)) :: Nil) => TtlCmd(key)
    case CommandParams("TTL", _) => wrongNumberOfArguments("TTL")
  }
}
