package com.bnpparibas.grp.jmx

/**
  *
  * @author morinb.
  */
object Protocol extends Enumeration {
  type Protocol = Value
  val RMI = Value("rmi")
  val IIOP = Value("iiop")
  val T3 = Value("t3")
}
