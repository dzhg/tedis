package com.github.dzhg.tedis.protocol

import java.io.InputStream

import com.github.dzhg.tedis.protocol.RESP._

import scala.collection.mutable

class RESPReader(in: InputStream) {

  def readValue(): RESPValue = {
    val c = in.read().toByte
    c match {
      case SIMPLE_STRING => readSimpleString()
      case INTEGER => readIntegerValue()
      case ERROR => readError()
      case BULK_STRING => readBulkString()
      case ARRAY => readArray()
      case EOF => EOFValue
    }
  }

  def readSimpleString(): SimpleStringValue = {
    val s = readUntil(CR)
    readUntil(LF)
    SimpleStringValue(s)
  }

  def readIntegerValue(): IntegerValue = {
    val s = readUntil(CR)
    readUntil(LF)
    IntegerValue(s)
  }

  def readError(): ErrorValue = {
    val s: String = readUntil(CR)
    readUntil(LF)
    val i = s.indexOf(" ")
    s.splitAt(i)
  }

  def readBulkString(): BulkStringValue = {
    val length: Long = readUntil(CR)
    readUntil(LF)
    length match {
      case -1 =>
        BulkStringValue(None)
      case 0 =>
        readUntil(LF)
        BulkStringValue(Some(""))
      case n =>
        val v = readBy(n.toInt)
        readUntil(LF)
        BulkStringValue(Some(v))
    }
  }

  def readArray(): ArrayValue = {
    val count: Long = readUntil(CR)
    readUntil(LF)
    count match {
      case -1 => ArrayValue(None)
      case 0 => ArrayValue(Some(Seq.empty))
      case n => 0.until(n.toInt).map(_ => readValue())
    }
  }

  def readBy(length: Int): Array[Int] = {
    val arr: mutable.ArrayBuilder[Int] = mutable.ArrayBuilder.make()
    0.until(length).foreach { _ =>
      arr += in.read()
    }
    arr.result()
  }

  def readUntil(end: Int): Array[Int] = {
    val arr: mutable.ArrayBuilder[Int] = mutable.ArrayBuilder.make()
    Iterator.from(1).takeWhile { _ =>
      val c = in.read()
      val done = c == end
      if (!done) arr += c
      !done
    }.foreach(noop)
    arr.result()
  }

  def noop(i: Int): Unit = {}
}
