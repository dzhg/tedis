package com.github.dzhg.tedis

class TedisException(val error: String, val msg: String) extends RuntimeException(s"$error $msg")

object TedisException {
  def apply(error: String, msg: String): TedisException = new TedisException(error, msg)

  def apply(err: TedisError): TedisException = new TedisException(err.error, err.msg)
}

case class TedisError(error: String, msg: String)

object TedisError {
  def apply(msg: String): TedisError = TedisError("ERR", msg)
}

trait TedisErrors {

  val WRONG_TYPE = TedisError("WRONGTYPE", "Operation against a key holding the wrong kind of value")
  val UNKNOWN_COMMAND = TedisError("unknown command '%s'")
  val SYNTAX_ERROR = TedisError("syntax error")
  val MULTI_NESTED = TedisError("MULTI calls can not be nested")
  val PROTOCOL_ERROR = TedisError("protocol error")

  def wrongType[T](): T = throw TedisException(WRONG_TYPE)
  def unknownCommand[T](cmd: String): T = throw TedisException(UNKNOWN_COMMAND.error, String.format(UNKNOWN_COMMAND.msg, cmd))
  def syntaxError[T](): T = throw TedisException(SYNTAX_ERROR)
  def multiNested[T](): T = throw TedisException(MULTI_NESTED)
  def protocolError[T](): T = throw TedisException(PROTOCOL_ERROR)
}
