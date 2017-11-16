package com.github.dzhg.tedis.commands

import com.github.dzhg.tedis.commands.HashCommands._
import com.github.dzhg.tedis.{CommandParser, TedisErrors, TedisException}
import com.github.dzhg.tedis.utils.TedisSuite

class HashCommandsParserSpec extends TedisSuite with TedisErrors {

  val parser: CommandParser = HashCommands.Parsers

  "HashCommandsParser" must {
    "parse 'hset key field value'" in {
      val params = CommandParams("HSET", List("key", "field", "value"))
      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [HsetCmd]
      val hsetCmd = cmd.asInstanceOf[HsetCmd]
      hsetCmd.key must be ("key")
      hsetCmd.field must be ("field")
      hsetCmd.value must be ("value")
    }

    "throw wrong number of arguments for 'hset key f1'" in {
      val params = CommandParams("HSET", List("key", "f1"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("HSET"))
    }

    "parse 'hget key f1'" in {
      val params = CommandParams("HGET", List("key", "f1"))

      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [HgetCmd]
      val hgetCmd = cmd.asInstanceOf[HgetCmd]
      hgetCmd.key must be ("key")
      hgetCmd.field must be ("f1")
    }

    "parse 'hmset key f1 v1 f2 v2'" in {
      val params = CommandParams("HMSET", List("key", "f1", "v1", "f2", "v2"))
      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [HmsetCmd]
      val hmsetCmd = cmd.asInstanceOf[HmsetCmd]
      hmsetCmd.key must be ("key")
      hmsetCmd.kvs must have size 2
      hmsetCmd.kvs.head must be (("f1", "v1"))
      hmsetCmd.kvs(1) must be (("f2", "v2"))
    }

    "parse 'hmget key f1 f2'" in {
      val params = CommandParams("HMGET", List("key", "f1", "f2"))
      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [HmgetCmd]
      val hmgetCmd = cmd.asInstanceOf[HmgetCmd]
      hmgetCmd.key must be ("key")
      hmgetCmd.fields must have size 2
      hmgetCmd.fields.head must be ("f1")
      hmgetCmd.fields(1) must be ("f2")
    }

    "parse 'hexists key field'" in {
      val params = CommandParams("HEXISTS", List("key", "field"))

      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [HexistsCmd]

      val hexistsCmd = cmd.asInstanceOf[HexistsCmd]
      hexistsCmd.key must be ("key")
      hexistsCmd.field must be ("field")
    }

    "throw wrong number of arguments for 'hexists key'" in {
      val params = CommandParams("HEXISTS", List("key"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("HEXISTS"))
    }

    "parse 'hdel key f1 f2'" in {
      val params = CommandParams("HDEL", List("key", "f1", "f2"))
      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [HdelCmd]

      val hdelCmd = cmd.asInstanceOf[HdelCmd]
      hdelCmd.key must be ("key")
      hdelCmd.fields must be (Seq("f1", "f2"))
    }

    "throw wrong number of arguments for 'hdel key'" in {
      val params = CommandParams("HDEL", List("key"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("HDEL"))
    }

    "parse 'hkeys hash'" in {
      val params = CommandParams("HKEYS", List("hash"))
      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [HkeysCmd]

      val hkeysCmd = cmd.asInstanceOf[HkeysCmd]
      hkeysCmd.key must be ("hash")
    }

    "throw wrong number of arguments for 'hkeys hash aaa'" in {
      val params = CommandParams("HKEYS", List("hash", "aaa"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("HKEYS"))
    }

    "parse 'hincrby key field 1" in {
      val params = CommandParams("HINCRBY", List("key", "field", "1"))

      parser.isDefinedAt(params) must be (true)

      val cmd = parser(params)
      cmd mustBe a [HincrbyCmd]

      val hincrbyCmd = cmd.asInstanceOf[HincrbyCmd]
      hincrbyCmd.key must be ("key")
      hincrbyCmd.field must be ("field")
      hincrbyCmd.value must be (1L)
    }

    "throw wrong number of arguments for 'hincrby hash field'" in {
      val params = CommandParams("HINCRBY", List("hash", "field"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("HINCRBY"))
    }

    "throw wrong number of arguments for 'hincrby hash field 1 2'" in {
      val params = CommandParams("HINCRBY", List("hash", "field", "1", "2"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("HINCRBY"))
    }

    "throw wrong number format for 'hincrby hash field abc'" in {
      val params = CommandParams("HINCRBY", List("hash", "field", "abc" ))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_FORMAT.error)
      ex.msg must be (WRONG_NUMBER_FORMAT.msg)
    }

    "parse 'hincrbyfloat key field 1.0" in {
      val params = CommandParams("HINCRBYFLOAT", List("key", "field", "1.0"))

      parser.isDefinedAt(params) must be (true)

      parser(params) must matchPattern {
        case HincrybyfloatCmd("key", "field", 1.0F) =>
      }
    }

    "throw wrong float number format for 'hincrbyfloat key field some_value'" in {
      val params = CommandParams("HINCRBYFLOAT", List("key", "field", "some_value"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_FLOAT_NUMBER_FORMAT.error)
      ex.msg must be (WRONG_FLOAT_NUMBER_FORMAT.msg)
    }

    "throw wrong number of arguments for 'hincrbyfloat key field'" in {
      val params = CommandParams("HINCRBYFLOAT", List("key", "field"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("HINCRBYFLOAT"))
    }

    "parse 'hstrlen key field'" in {
      val params = CommandParams("HSTRLEN", List("key", "field"))

      parser.isDefinedAt(params) must be (true)

      parser(params) must matchPattern {
        case HstrlenCmd("key", "field") =>
      }
    }

    "throw wrong number of arguments for 'hstrlen key field something'" in {
      val params = CommandParams("HSTRLEN", List("key", "field", "something"))

      val ex = the [TedisException] thrownBy parser(params)
      ex.error must be (WRONG_NUMBER_OF_ARGS.error)
      ex.msg must be (WRONG_NUMBER_OF_ARGS.msg.format("HSTRLEN"))
    }
  }
}
