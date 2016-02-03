package com.bnpparibas.grp.jmx

import org.scalatest.FunSuite

/**
  *
  * @author morinb.
  */
class ProtocolTest extends FunSuite {
  test("Protocol T3") {
    assert("t3" === Protocol.T3.toString)
  }

  test("Protocol IIOP") {
    assert("iiop" === Protocol.IIOP.toString)
  }

  test("Protocol RMI") {
    assert("rmi" === Protocol.RMI.toString)
  }
}
