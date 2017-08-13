package com.github.dzhg.tedis

import java.net._

import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

/**
  * @author dzhg 8/11/17
  */
class TedisServer(port: Int) {
  private val LOGGER = LoggerFactory.getLogger(classOf[TedisServer])

  var serverStop = false

  val socket = new ServerSocket(port)
  val tedis = Tedis()

  def stop(): Unit = {
    serverStop = true
    socket.close()
  }

  def start(): Unit = {
    LOGGER.info(s"Starting Tedis Server at: ${socket.getLocalPort}")
    while (!serverStop) {
      try {
        val s = socket.accept()
        new TedisServerConnection(s, this).start()
      } catch {
        case NonFatal(e) => LOGGER.info(e.getMessage)
      }
    }
  }
}

object TedisServer extends App {
  val port = if (args.length == 0) 0 else args(0).toInt
  try {
    new TedisServer(port).start()
  } catch {
    case NonFatal(e) =>
      println(s"Server crashed due to: ${e.getMessage}")
      e.printStackTrace()
  }
}
