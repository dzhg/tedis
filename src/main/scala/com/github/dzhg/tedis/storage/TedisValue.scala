package com.github.dzhg.tedis.storage

import java.util.{HashMap => JHashMap, Map => JMap}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

/**
  * @author dzhg 8/11/17
  */
sealed trait TedisValue

object TedisValue {
  implicit def stringToValue(s: String): TedisString = TedisString(s)
  implicit def valueToString(v: TedisString): String = v.value

  implicit def mapToValue(hash: JMap[String, String]): TedisHash = TedisHash(hash)
  implicit def valueToMap(v: TedisHash): JMap[String, String] = v.data
}

case class TedisString(value: String) extends TedisValue

case class TedisHash(data: JMap[String, String]) extends TedisValue {
  def apply(field: String): Option[String] = Option(data.get(field))
}

object TedisHash {
  def apply(kvs: (String, String)*): TedisHash = {
    TedisHash(new JHashMap[String, String](kvs.toMap.asJava))
  }
}

case object BadTedisValue extends TedisValue // this one is useful for testing

