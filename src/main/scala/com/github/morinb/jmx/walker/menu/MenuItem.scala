package com.github.morinb.jmx.walker.menu

/**
  *
  * @author morinb.
  */
case class MenuItem(val prefix: String, val text: String, val separator: String)(val action: () => Boolean)

object Separator extends MenuItem("", "", "")(() => true)
