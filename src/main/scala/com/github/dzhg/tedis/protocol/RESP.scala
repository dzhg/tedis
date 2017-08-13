package com.github.dzhg.tedis.protocol

import java.io.{InputStream, OutputStream}

import scala.language.implicitConversions

object RESP {
  sealed trait RESPValue
  case class SimpleStringValue(value: String) extends RESPValue
  case class IntegerValue(value: Int) extends RESPValue
  case class ErrorValue(error: String, msg: String) extends RESPValue
  case class BulkStringValue(value: Option[String]) extends RESPValue
  case class ArrayValue(values: Option[Seq[RESPValue]]) extends RESPValue
  case object EOFValue extends RESPValue

  val SIMPLE_STRING: Int = '+'
  val ERROR: Int = '-'
  val INTEGER: Int = ':'
  val BULK_STRING: Int = '$'
  val ARRAY: Int = '*'
  val EOF: Int = -1

  val CR: Int = '\r'
  val LF: Int = '\n'
  val CRLF: Array[Byte] = Array(CR.toByte, LF.toByte)

  val ENCODING: String = "UTF-8"

  implicit def arrayToString(array: Array[Int]): String = String.valueOf(array.map(_.toChar))
  implicit def arrayToInt(array: Array[Int]): Int = arrayToString(array).toInt
  implicit def pairToError(pair: (String, String)): ErrorValue = ErrorValue(pair._1, pair._2)
  implicit def seqToArray(vs: Seq[RESPValue]): ArrayValue = ArrayValue(Some(vs))

  def reader(in: InputStream): RESPReader = new RESPReader(in)
  def writer(out: OutputStream): RESPWriter = new RESPWriter(out)
}
