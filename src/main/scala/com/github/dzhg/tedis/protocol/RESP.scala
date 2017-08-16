package com.github.dzhg.tedis.protocol

import java.io.{InputStream, OutputStream}

import scala.language.implicitConversions

object RESP {
  sealed trait RESPValue
  case class SimpleStringValue(value: String) extends RESPValue
  case class IntegerValue(value: Long) extends RESPValue
  case class ErrorValue(error: String, msg: String) extends RESPValue
  case class BulkStringValue(value: Option[String]) extends RESPValue
  case class ArrayValue(values: Option[Seq[RESPValue]]) extends RESPValue
  case object EOFValue extends RESPValue

  val SIMPLE_STRING: Byte = '+'
  val ERROR: Byte = '-'
  val INTEGER: Byte = ':'
  val BULK_STRING: Byte = '$'
  val ARRAY: Byte = '*'
  val EOF: Byte = -1

  val CR: Byte = '\r'
  val LF: Byte = '\n'
  val CRLF: Array[Byte] = Array(CR.toByte, LF.toByte)

  val ENCODING: String = "UTF-8"

  implicit def arrayToString(array: Array[Int]): String = String.valueOf(array.map(_.toChar))
  implicit def arrayToInt(array: Array[Int]): Long = arrayToString(array).toLong
  implicit def pairToError(pair: (String, String)): ErrorValue = ErrorValue(pair._1.trim, pair._2.trim)
  implicit def seqToArray(vs: Seq[RESPValue]): ArrayValue = ArrayValue(Some(vs))

  def reader(in: InputStream): RESPReader = new RESPReader(in)
  def writer(out: OutputStream): RESPWriter = new RESPWriter(out)
}
