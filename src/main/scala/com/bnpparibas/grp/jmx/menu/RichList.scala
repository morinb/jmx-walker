package com.bnpparibas.grp.jmx.menu

import scala.language.implicitConversions

/**
  *
  * @author morinb.
  */
class RichList[T](val list: List[T]) {
  def search(criteria: T => Boolean): Option[T] = {
    val result = list.filter(criteria)
    if (result.size == 1) {
      Some(result.head)
    } else {
      None
    }

  }
}

object RichList {
  implicit def enrichList[T](list: List[T]): RichList[T] = new RichList[T](list)
}
