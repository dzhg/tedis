package com.github.dzhg.tedis

import org.scalatest.{MustMatchers, WordSpec}

class PipelineSpec extends WordSpec with MustMatchers with WithTedisServer {

  "Tedis Pipeline" must {
    "support MULTI" in {
      val result = redisClient.pipeline { pipeline => }
      result mustBe defined
      result.get mustBe empty
    }

    "handle multiple commands" in {
      val result = redisClient.pipeline { pipeline =>
        pipeline.set("key1", "value1")
        pipeline.set("key2", "value2")
        pipeline.get("key1")
        pipeline.get("key2")
      }

      result mustBe defined
      result.get must have size 4

      val list = result.get
      list.head mustBe a [java.lang.Boolean]
      list.head.asInstanceOf[Boolean] must be (true)

      list(1) mustBe a [java.lang.Boolean]
      list(1).asInstanceOf[Boolean] must be (true)

      list(2) mustBe a [Option[_]]
      list(2).asInstanceOf[Option[String]] must be (Some("value1"))

      list(3) mustBe a [Option[_]]
      list(3).asInstanceOf[Option[String]] must be (Some("value2"))
    }

    "be clear after pipeline execution" in {
      val r1 = redisClient.set("key1", "v1")
      r1 must be (true)

      val r2 = redisClient.pipeline { pipeline =>
        pipeline.set("key1", "v2")
        pipeline.get("key1")
      }

      r2 mustBe defined
      r2.get must have size 2

      val r3 = redisClient.set("key1", "v3")
      r3 must be (true)

      val r4 = redisClient.get("key1")

      r4 mustBe defined
      r4.get must be ("v3")
    }
  }
}
