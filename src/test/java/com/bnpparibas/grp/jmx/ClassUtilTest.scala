package com.bnpparibas.grp.jmx

import org.scalatest.FunSuite

/**
  *
  * @author morinb.
  */
class ClassUtilTest extends FunSuite {

  test("Array of ObjectName") {
    assert("Array[ObjectName]" === ClassUtil.convert("[Ljavax.management.ObjectName;"))
  }

  test("Array of Array of ... of Int") {
    assert("Array[Array[Array[Array[Array[Array[Array[Int]]]]]]]" === ClassUtil.convert("[[[[[[[I"))
  }

  test("Array of Array of Long") {
    assert("Array[Array[Long]]" === ClassUtil.convert("[[J"))
  }

  test("java.lang.Long short") {
    assert("Long" === ClassUtil.convert("java.lang.Long"))
  }

  test("java.lang.Long not short") {
    assert("java.lang.Long" === ClassUtil.convert("java.lang.Long", short = false))
  }
}
