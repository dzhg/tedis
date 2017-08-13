package com.github.dzhg.tedis.storage

import java.util

import scala.language.implicitConversions

/**
  * @author dzhg 8/11/17
  */
sealed trait TedisValue

object TedisValue {
  implicit def stringToValue(s: String): TedisString = TedisString(s)
  implicit def valueToString(v: TedisString): String = v.value

  implicit def mapToValue(hash: util.Map[String, String]): TedisHash = TedisHash(hash)
  implicit def valueToMap(v: TedisHash): util.Map[String, String] = v.hash
}

case class TedisString(value: String) extends TedisValue
case class TedisHash(hash: util.Map[String, String]) extends TedisValue
case object BadTedisValue extends TedisValue // this one is useful for testing

