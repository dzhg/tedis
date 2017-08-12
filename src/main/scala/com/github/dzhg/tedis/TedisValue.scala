package com.github.dzhg.tedis

import java.util

import scala.language.implicitConversions

/**
  * @author dzhg 8/11/17
  */
sealed trait TedisValue

object TedisValue {
  implicit def stringToValue(s: String): StringValue = StringValue(s)
  implicit def valueToString(v: StringValue): String = v.value

  implicit def mapToValue(hash: util.Map[String, String]): HashValue = HashValue(hash)
  implicit def valueToMap(v: HashValue): util.Map[String, String] = v.hash
}

case class StringValue(value: String) extends TedisValue
case class HashValue(hash: util.Map[String, String]) extends TedisValue
case object ErrorValue extends TedisValue // this one is useful for testing

