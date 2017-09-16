package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.protocol.RESP.{ArrayValue, BulkStringValue, IntegerValue, RESPValue}
import com.github.dzhg.tedis.TedisStorage

/**
  * @author dzhg 8/11/17
  */
trait TedisCommand[T] extends CommandHelper {
  def exec(storage: TedisStorage): T
  def execToRESP(storage: TedisStorage): RESPValue = resultToRESP(exec(storage))
  def resultToRESP(v: T): RESPValue = OK
  def requireLock: Boolean = true
}

trait AsIntegerResult {
  this: TedisCommand[Long] =>
  override def resultToRESP(v: Long): RESPValue = IntegerValue(v)
}

trait AsBulkStringResult {
  this: TedisCommand[Option[String]] =>
  override def resultToRESP(v: Option[String]): RESPValue = BulkStringValue(v)
}

trait AsNonNilBulkStringResult {
  this: TedisCommand[String] =>
  override def resultToRESP(v: String): RESPValue = BulkStringValue(Some(v))
}

trait AsArrayResult {
  this: TedisCommand[Seq[String]] =>
  override def resultToRESP(v: Seq[String]): RESPValue = ArrayValue(Some(v.map(s => BulkStringValue(Some(s)))))
}

