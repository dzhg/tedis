package com.github.dzhg.tedis.protocol

import java.io.InputStream

import com.github.dzhg.tedis.TedisException

import scala.collection.mutable

/**
  * @author dzhg 8/11/17
  */
case class Request(in: InputStream) {

  import Request._

  def consumeCount(): Long = {
    if (COUNT_PREFIX == consumeByte()) {
      val count = consumeLong()
      consumeUntil('\n')
      count
    } else {
      throw TedisException("ERR invalid count")
    }
  }

  def consumeLength(): Long = {
    if (LENGTH_PREFIX == consumeByte()) {
      val length = consumeLong()
      consumeUntil('\n')
      length
    } else {
      throw TedisException("ERR invalid length")
    }
  }

  def consumeLong(): Long = {
    val s = consumeUntil('\r')
    val ss = String.valueOf(s.map(_.toChar))
    ss.toLong
  }

  def consumeBy(length: Long): String = {
    val arr: mutable.ArrayBuilder[Int] = mutable.ArrayBuilder.make()
    0.until(length.toInt).foreach { _ => arr += consumeByte() }
    String.valueOf(arr.result().map(_.toChar))
  }

  def consumeUntil(b: Int): Array[Int] = {
    val arr: mutable.ArrayBuilder[Int] = mutable.ArrayBuilder.make()
    Iterator.from(1).takeWhile { _ =>
      val c = consumeByte()
      val done = c == b
      if (!done) arr += c
      !done
    }.foreach(noop)
    arr.result()
  }

  def noop(i: Int): Unit = {}

  def consumeEOF(): Unit = {
    Iterator.from(1).takeWhile { _ => -1 != consumeByte(true) } foreach noop
  }

  def consumeByte(eof: Boolean = false): Int = {
    val b = in.read()
    if (b == -1 && !eof) throw TedisException("ERR Unexpected EOF")
    b
  }
}

object Request {
  val COUNT_PREFIX: Int = '*'
  val LENGTH_PREFIX: Int = '$'
}
