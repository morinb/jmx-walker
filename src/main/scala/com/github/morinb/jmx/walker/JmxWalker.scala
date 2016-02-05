package com.github.morinb.jmx.walker

import java.io.IOException
import java.util.Scanner
import javax.management.ObjectName
import javax.management.remote.{JMXConnector, JMXConnectorFactory, JMXServiceURL}
import javax.naming.Context

import com.github.morinb.jmx.walker.AnsiColor._

import scala.collection.JavaConversions._

/**
  *
  * @author morinb.
  */
class JmxWalker(val protocol: String,
                val credentials: Credentials,
                val admin: Boolean) {
  private[this] val ADMIN_RUNTIME = "/jndi/weblogic.management.mbeanservers.domainruntime"
  private[this] val RUNTIME = "/jndi/weblogic.management.mbeanservers.runtime"

  var connector: Option[JMXConnector] = None

  def connect(): Unit = {
    println(s"${CYAN}Connecting to $credentials${DEFAULT}")
    val serviceURL = new JMXServiceURL(protocol.toString, credentials.server, credentials.port, if (admin) ADMIN_RUNTIME else RUNTIME)

    val context = Map(
      Context.SECURITY_PRINCIPAL -> credentials.login,
      Context.SECURITY_CREDENTIALS -> new String(credentials.password),
      JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES -> "weblogic.management.remote"
    )

    connector = Some(JMXConnectorFactory.connect(serviceURL, context)) // Automatically converted to java.util.Map by JavaConversion.mapAsJavaMap method
  }

  def connected: Boolean = connector match {
    case Some(c) => try {
      c.getMBeanServerConnection
      true
    } catch {
      case ioe: IOException => false
    }
    case None => false
  }

  def disconnect(): Unit = {
    if (connected) {
      println(s"${CYAN}Disconnecting from $credentials$DEFAULT")
      connector.get.close()
    }
    connector = None
  }

  def getConnection = connector.get.getMBeanServerConnection

  def repl(ons: Option[ObjectName] = None): Unit = {
    implicit val connection = connector.get.getMBeanServerConnection
    var objectNames = ons match {
      case Some(names) => Set(names)
      case None => asScalaSet(connection.queryNames(null, null)).toSet // conversion fro java.util.Set to Set
    }

    val sc = new Scanner(System.in)
    var loop = true
    while (loop) {
      for (on <- objectNames) {
        println(on.getCanonicalName)
      }
      println("\nType a ObjectName or 'quit' to quit : ")
      val line = sc.nextLine()
      
      if ("quit" == line) {
        loop = false
      } else {
        objectNames = Set(new ObjectName(line))
      }
    }
  }



}
