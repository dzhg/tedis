package com.github.dzhg.tedis

/**
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
