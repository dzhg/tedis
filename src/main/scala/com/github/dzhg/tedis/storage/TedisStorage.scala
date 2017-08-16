package com.github.dzhg.tedis.storage

import java.util

import scala.collection.JavaConversions._

trait TedisStorage {
  /**
    * TTL aware API to get the TedisEntry
    *
    * @return None if the key does not exist or already expired, otherwise, Some(entry)
    */
  def get(key: String): Option[TedisEntry] = getEntry(key) flatMap { entry => if (expired(entry.keyInfo)) None else Some(entry) }

  /**
    * Does not handle TTL
    */
  protected def getEntry(key: String): Option[TedisEntry]

  /**
    * @return previous value. None if the key doesn't exist and Some(entry) if the key exists
    */
  def put(key: String, entry: TedisEntry): Option[TedisEntry]

  def putAll(values: Map[String, TedisEntry]): Unit

  def keys: Seq[String]

  /**
    * @return true if the key exists or false if the key does not exist
    */
  def removeKey(key: String): Boolean

  protected def expired(keyInfo: TedisKeyInfo): Boolean = {
    keyInfo.ttl exists { millis =>
      val remains = calculateRemains(millis, keyInfo.createdAt)
      val expired = remains <= 0
      if (expired) removeKey(keyInfo.name)
      expired
    }
  }

  private def calculateRemains(millis: Long, createdAt: Long): Long = {
    millis - (System.currentTimeMillis() - createdAt)
  }
}

/**
  * HashMap based TedisStorage implementation.
  *
  * The internal storage is a Java HashMap. It doesn't provide any thread-safety.
  *
  * Must be used with LockedStorage to provide thread level locks for Redis operations.
  */
class HashMapTedisStorage extends TedisStorage {
  val internal: java.util.HashMap[String, TedisEntry] = new util.HashMap[String, TedisEntry]()

  /**
    * @return previous value. None if the key doesn't exist and Some(entry) if the key exists
    */
  override def put(key: String, entry: TedisEntry): Option[TedisEntry] = Option(internal.put(key, entry))

  override def putAll(values: Map[String, TedisEntry]): Unit = internal.putAll(values)

  override def keys: Seq[String] = internal.keySet().toSeq.filter(key => get(key).isDefined)

  override def removeKey(key: String): Boolean = Option(internal.remove(key)).isDefined

  override protected def getEntry(key: String): Option[TedisEntry] = Option(internal.get(key))
}