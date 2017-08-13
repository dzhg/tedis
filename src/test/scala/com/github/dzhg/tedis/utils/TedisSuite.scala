package com.github.dzhg.tedis.utils

import com.github.dzhg.tedis.protocol.RESP.BulkStringValue
import org.scalatest.{MustMatchers, OptionValues, WordSpec}

import scala.language.implicitConversions

trait TedisSuite extends WordSpec with MustMatchers with OptionValues {
  implicit def stringToBulkStringValue(s: String): BulkStringValue = BulkStringValue(Some(s))
}
