package com.github.dzhg.tedis

/**
  * @author dzhg 8/11/17
  */
class Tedis extends LockedMapStorage with CommandExecutor
  with StringOperations
  with Operations
  with HashOperations {
}

object Tedis {
  def apply(): Tedis = new Tedis()
}
