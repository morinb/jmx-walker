package com.github.morinb.jmx.walker

/**
  *
  * @author morinb.
  */
case class Credentials(val server: String, val port: Int, val login: String, val password: Array[Char]) {
  override def toString: String = s"$server:$port"
}
