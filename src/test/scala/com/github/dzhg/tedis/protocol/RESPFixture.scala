package com.github.dzhg.tedis.protocol

import com.github.dzhg.tedis.protocol.RESP.BulkStringValue

trait RESPFixture {

  val EMPTY_BULK_STRING: BulkStringValue = BulkStringValue(Some(""))

  val EMPTY_BULK_STRING_BYTES: Array[Byte] =
    Array(RESP.BULK_STRING) ++ "0".getBytes(RESP.ENCODING) ++ RESP.CRLF ++
    "".getBytes(RESP.ENCODING) ++ RESP.CRLF

  val NIL_BULK_STRING: BulkStringValue = BulkStringValue(None)

  val NIL_BULK_STRING_BYTES: Array[Byte] = Array(RESP.BULK_STRING) ++ "-1".getBytes(RESP.ENCODING) ++ RESP.CRLF
}
