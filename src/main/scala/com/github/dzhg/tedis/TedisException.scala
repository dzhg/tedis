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
  val WRONG_NUMBER_OF_ARGS = TedisError("wrong number of arguments for '%s' command")
  val PROTOCOL_ERROR = TedisError("protocol error")
  val WRONG_NUMBER_FORMAT = TedisError("value is not an integer or out of range")
  val HASH_VALUE_NOT_AN_INTEGER = TedisError("hash value is not an integer")

  def wrongNumberOfArguments[T](cmd: String): T = throw TedisException(WRONG_NUMBER_OF_ARGS.error, WRONG_NUMBER_OF_ARGS.msg.format(cmd))
  def wrongType[T](): T = throw TedisException(WRONG_TYPE)
  def unknownCommand[T](cmd: String): T = throw TedisException(UNKNOWN_COMMAND.error, UNKNOWN_COMMAND.msg.format(cmd))
  def syntaxError[T](): T = throw TedisException(SYNTAX_ERROR)
  def multiNested[T](): T = throw TedisException(MULTI_NESTED)
  def protocolError[T](): T = throw TedisException(PROTOCOL_ERROR)
  def numberFormatError[T](): T = throw TedisException(WRONG_NUMBER_FORMAT)
  def hashValueNotAnInteger[T](): T = throw TedisException(HASH_VALUE_NOT_AN_INTEGER)
}
