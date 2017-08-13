package com.github.dzhg.tedis

import com.redis.RedisClient
import org.scalatest.{BeforeAndAfter, WordSpec}

trait WithTedisServer extends BeforeAndAfter {
  this: WordSpec =>

  class ServerRunner(server: TedisServer) extends Thread {
    override def run(): Unit = server.start()
  }

  var server: TedisServer = _
  var redisClient: RedisClient = _

  def launchServer(): TedisServer = {
    val server = new TedisServer(0)
    new ServerRunner(server).start()
    server
  }

  def createClient(server: TedisServer): RedisClient = new RedisClient("localhost", server.socket.getLocalPort)

  before {
    server = launchServer()
    redisClient = createClient(server)
  }

  after {
    server.stop()
    redisClient.disconnect
  }
}


