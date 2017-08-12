package com.github.dzhg

import java.util

/**
  * @author dzhg 8/11/17
  */
package object tedis {
  type TedisStorage = util.Map[String, TedisEntry]
}
