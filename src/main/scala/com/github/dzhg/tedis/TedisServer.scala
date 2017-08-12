package com.github.dzhg.tedis

import java.net._

import com.github.dzhg.tedis.protocol.Request

import scala.util.control.NonFatal

/**
  * @author dzhg 8/11/17
  */
class TedisServer(port: Int) {

  def start(): Unit = {
    val server = new ServerSocket(port)
    while (true) {
      val s = server.accept()
      new TedisServerThread(s).start()
    }
  }

  class TedisServerThread(s: Socket) extends Thread {
    override def run(): Unit = {
      val in = s.getInputStream
      val out = s.getOutputStream

      while (true) {

        val req = Request(in)
        val count = req.consumeCount()
        println(s"COUNT: $count")
        0.until(count.toInt).foreach { _ =>
          val length = req.consumeLength()
          val part = req.consumeBy(length)
          println(s"LENGTH: $length, PART: $part")
          req.consumeUntil('\n')
        }

        //req.consumeEOF()

        println("----- response ----->")

        out.write("-ERR unknown command\r\n".getBytes())
        out.flush()
      }
      s.close()
    }
  }

}

object TedisServer extends App {
  try {
    val port = if (args.length == 0) 9999 else args(0).toInt
    println(s"Starting server at port: $port")
    new TedisServer(port).start()
  } catch {
    case NonFatal(e) =>
      println(s"Error: ${e.getMessage}")
      e.printStackTrace()
  }
}


