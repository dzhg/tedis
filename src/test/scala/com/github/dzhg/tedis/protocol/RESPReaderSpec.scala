package com.github.dzhg.tedis.protocol

import java.io.{ByteArrayInputStream, InputStream}

import com.github.dzhg.tedis.protocol.RESP.{ArrayValue, BulkStringValue, ErrorValue, IntegerValue, SimpleStringValue}
import com.github.dzhg.tedis.utils.TedisSuite

class RESPReaderSpec extends TedisSuite with RESPFixture {

  "RESPReader" must {
    "read SimpleString" in {
      val bytes = Array(RESP.SIMPLE_STRING) ++ "value".getBytes(RESP.ENCODING) ++ RESP.CRLF
      val in = inputStream(bytes)
      val reader = RESP.reader(in)
      val s = reader.readValue()
      in.close()

      s mustBe a [SimpleStringValue]
      s.asInstanceOf[SimpleStringValue].value must be ("value")
    }

    "read Integer" in {
      val bytes = Array(RESP.INTEGER) ++ "35".getBytes(RESP.ENCODING) ++ RESP.CRLF
      val in = inputStream(bytes)
      val reader = RESP.reader(in)
      val i = reader.readValue()
      in.close()

      i mustBe a [IntegerValue]
      i.asInstanceOf[IntegerValue].value must be (35)
    }

    "read Error" in {
      val bytes = Array(RESP.ERROR) ++ "ERR error msg".getBytes(RESP.ENCODING) ++ RESP.CRLF
      val in = inputStream(bytes)
      val reader = RESP.reader(in)
      val err = reader.readValue()
      in.close()

      err mustBe a [ErrorValue]
      err.asInstanceOf[ErrorValue] must be (ErrorValue("ERR", "error msg"))
    }

    "read BulkString" in {
      val bytes = Array(RESP.BULK_STRING) ++ "5".getBytes(RESP.ENCODING) ++ RESP.CRLF ++ "scala".getBytes(RESP.ENCODING) ++ RESP.CRLF
      val in = inputStream(bytes)
      val reader = RESP.reader(in)
      val s = reader.readValue()
      in.close()

      s mustBe a [BulkStringValue]
      val bulkString = s.asInstanceOf[BulkStringValue]
      bulkString.value.value must be ("scala")
    }

    "read empty BulkString" in {
      val in = inputStream(EMPTY_BULK_STRING_BYTES)
      val reader = RESP.reader(in)
      val s = reader.readValue()
      in.close()

      s mustBe a [BulkStringValue]
      val bulkString = s.asInstanceOf[BulkStringValue]
      bulkString must be (EMPTY_BULK_STRING)
    }

    "read nil BulkString" in {
      val in = inputStream(NIL_BULK_STRING_BYTES)
      val reader = RESP.reader(in)
      val s = reader.readValue()
      in.close()

      s mustBe a [BulkStringValue]
      val bulkString = s.asInstanceOf[BulkStringValue]
      bulkString must be (NIL_BULK_STRING)
    }

    "read Array" in {
      val bytes = Array(RESP.ARRAY) ++ "2".getBytes(RESP.ENCODING) ++ RESP.CRLF ++
        Array(RESP.SIMPLE_STRING) ++ "java".getBytes(RESP.ENCODING) ++ RESP.CRLF ++
        Array(RESP.INTEGER) ++ "9".getBytes(RESP.ENCODING) ++ RESP.CRLF
      val in = inputStream(bytes)
      val reader = RESP.reader(in)
      val s = reader.readValue()
      in.close()

      s mustBe a [ArrayValue]
      val arrayValue = s.asInstanceOf[ArrayValue]
      arrayValue.values mustBe defined

      val values = arrayValue.values.get
      values must have size 2
      values.head mustBe a [SimpleStringValue]
      values(1) mustBe a [IntegerValue]
    }

    "read empty Array" in {
      val bytes = Array(RESP.ARRAY) ++ "0".getBytes(RESP.ENCODING) ++ RESP.CRLF
      val in = inputStream(bytes)
      val reader = RESP.reader(in)
      val s = reader.readValue()
      in.close()

      s mustBe a [ArrayValue]
      val arrayValue = s.asInstanceOf[ArrayValue]
      arrayValue.values mustBe defined

      val values = arrayValue.values.get
      values mustBe empty
    }

    "read nil Array" in {
      val bytes = Array(RESP.ARRAY) ++ "-1".getBytes(RESP.ENCODING) ++ RESP.CRLF
      val in = inputStream(bytes)
      val reader = RESP.reader(in)
      val s = reader.readValue()
      in.close()

      s mustBe a [ArrayValue]
      val arrayValue = s.asInstanceOf[ArrayValue]
      arrayValue.values mustBe empty
    }
  }

  def inputStream(v: Array[Byte]): InputStream = new ByteArrayInputStream(v)
}
