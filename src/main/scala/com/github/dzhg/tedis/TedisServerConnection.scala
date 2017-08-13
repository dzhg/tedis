package com.github.dzhg.tedis

import java.net.Socket

import com.github.dzhg.tedis.commands.CommandFactory
import com.github.dzhg.tedis.commands.MultiCommands.{DiscardCmd, ExecCmd, MultiCmd}
import com.github.dzhg.tedis.protocol.RESP.{ArrayValue, EOFValue, SimpleStringValue}
import com.github.dzhg.tedis.protocol.RESPWriter
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer

class TedisServerConnection(s: Socket, server: TedisServer) extends Thread with TedisErrors {
  private val LOGGER = LoggerFactory.getLogger(classOf[TedisServerConnection])

  var pipeline: Boolean = false
  val commands: ListBuffer[TedisCommand[_]] = new ListBuffer[TedisCommand[_]]

  override def run(): Unit = {
    LOGGER.info("New client connected")
    val in = RESP.reader(s.getInputStream)
    val out = RESP.writer(s.getOutputStream)

    var done = false
    try {
      while (!done && !server.serverStop) {

        val req = in.readValue()

        req match {
          case EOFValue => done = true
          case params: ArrayValue =>
            try {
              val cmd = CommandFactory.make(params)
              LOGGER.debug(s"CMD: $cmd")
              if (pipeline) {
                handlePipeline(cmd, out)
              } else {
                if (cmd == MultiCmd) pipeline = true
                val result = server.tedis.executeToRESP(cmd)
                LOGGER.debug(s"RESULT: $result")
                out.writeValue(result)
              }
            } catch {
              case e: TedisException =>
                val error = RESP.ErrorValue(e.error, e.msg)
                out.writeValue(error)
            }
          case _ => LOGGER.debug("NOOP")
        }

        out.flush()
      }
    } finally {
      s.close()
      LOGGER.info("Client disconnected")
    }
  }

  // TODO: Extract pipeline support to separate trait
  private def handlePipeline(cmd: TedisCommand[_], out: RESPWriter): Unit = {
    cmd match {
      case MultiCmd => throw multiNested()
      case DiscardCmd =>
        commands.clear()
        pipeline = false
        out.writeValue(SimpleStringValue("OK"))
      case ExecCmd => executePipeline(out)
      case other =>
        commands.append(other)
        out.writeValue(SimpleStringValue("QUEUED"))
    }
  }

  private def executePipeline(out: RESPWriter): Unit = {
    try {
      val results = server.tedis.executeAllToRESP(commands)
      val array = ArrayValue(Some(results))
      out.writeValue(array)
    } finally {
      commands.clear()
      pipeline = false
    }
  }
}
