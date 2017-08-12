package com.github.dzhg.tedis

/**
  * @author dzhg 8/11/17
  */
trait TedisCommand[T] {
  def exec(storage: TedisStorage): T
}

