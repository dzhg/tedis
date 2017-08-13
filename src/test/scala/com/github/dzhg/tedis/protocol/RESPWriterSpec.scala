package com.github.dzhg.tedis.protocol

import java.io.ByteArrayOutputStream

import com.github.dzhg.tedis.protocol.RESP.{BulkStringValue, ErrorValue, IntegerValue, SimpleStringValue}
import com.github.dzhg.tedis.utils.TedisSuite

class RESPWriterSpec extends TedisSuite with RESPFixture {

  "RESPWriter" must {
    "write SimpleString" in {
      val out = new ByteArrayOutputStream(256)
      val writer = RESP.writer(out)
      val s = SimpleStringValue("tedis")
      writer.writeValue(s)

      verifyBytes(out, Array(RESP.SIMPLE_STRING) ++ "tedis".getBytes(RESP.ENCODING) ++ RESP.CRLF)
    }

    "write Error" in {
      val out = new ByteArrayOutputStream(256)
      val writer = RESP.writer(out)
      val err = ErrorValue("ERR", "error1")
      writer.writeValue(err)

      verifyBytes(out, Array(RESP.ERROR) ++ "ERR error1".getBytes(RESP.ENCODING) ++ RESP.CRLF)
    }

    "write Integer" in {
      val out = new ByteArrayOutputStream(256)
      val writer = RESP.writer(out)
      val i = IntegerValue(100)
      writer.writeValue(i)

      verifyBytes(out, Array(RESP.INTEGER) ++ "100".getBytes(RESP.ENCODING) ++ RESP.CRLF)
    }

    "write empty BulkString" in {
      val out = new ByteArrayOutputStream(256)
      val writer = RESP.writer(out)
      writer.writeValue(EMPTY_BULK_STRING)

      verifyBytes(out, EMPTY_BULK_STRING_BYTES)
    }
  }

  private def verifyBytes(out: ByteArrayOutputStream, bytes: Array[Byte]): Unit =
    out.toByteArray must be (bytes)
}
