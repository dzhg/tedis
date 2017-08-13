package com.github.dzhg.tedis

import com.github.dzhg.tedis.operations.{HashOperations, KeyOperations, ServerOperations, StringOperations}

/**
  * A single class provides all Redis operations
  *
  * This class shall be used to write Redis server or client adapters.
  *
  * It supports both method calls and command pattern APIs.
  *
  * `Command` pattern is useful when creating server adapters.
  *
  * `CommandExecutor` trait provides the command pattern support.
  *
  * Method APIs are useful when creating client adapters.
  *
  * Redis APIs are defined in separate *Operations traits.
  * Such as, StringOperations, HashOperations, etc.
  *
  * @see com.github.dzhg.tedis.TedisServer
  * @author dzhg 8/11/17
  */
class Tedis extends DefaultStorage with CommandExecutor
  with ServerOperations
  with StringOperations
  with KeyOperations
  with HashOperations {
}

object Tedis {
  def apply(): Tedis = new Tedis()
}
