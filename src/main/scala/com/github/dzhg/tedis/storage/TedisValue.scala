package com.github.dzhg.tedis.storage

import java.util.{HashMap => JHashMap, Map => JMap, List => JList, ArrayList => JArrayList}

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
  def putAll(kvs: Seq[(String, String)]): Unit = data.putAll(kvs.toMap.asJava)
}

object TedisHash {
  def apply(kvs: (String, String)*): TedisHash = {
    TedisHash(new JHashMap[String, String](kvs.toMap.asJava))
  }
}

case class TedisList(data: JList[String]) extends TedisValue

object TedisList {
  def apply(): TedisList = TedisList(new JArrayList[String]())

  def apply(vs: Seq[String]): TedisList = TedisList(new JArrayList[String](vs.asJava))
}

case object BadTedisValue extends TedisValue // this one is useful for testing

