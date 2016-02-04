package com.bnpparibas.grp.jmx.menu


import com.bnpparibas.grp.jmx.AnsiColor._
import com.bnpparibas.grp.jmx.LineReader
import com.bnpparibas.grp.jmx.menu.RichList._
import jline.console.ConsoleReader
import jline.console.completer.StringsCompleter

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  *
  * @author morinb.
  */
class Menu(val name: String, var defaultSelection: Option[String] = None)(implicit override val reader: ConsoleReader) extends LineReader {
  var _items: mutable.MutableList[MenuItem] = mutable.MutableList()

  def +=(menuItem: MenuItem) = _items += menuItem

  def -=(menuItem: MenuItem) = _items = _items.filterNot(i => i == menuItem)

  def add(menuItem: MenuItem) = this += menuItem

  def remove(menuItem: MenuItem) = this -= menuItem

  def removeAll() = _items.clear()

  def items() = _items.toList

  private[this] def displayMenuToUser(clear: Boolean = true, redisplayName: Boolean = true): Unit = {
    if (clear) {
      clearScreen()
    }
    if (redisplayName) {
      println(name)
      println
    }
    for (item <- _items) {
      println(s"  $BRIGHT_WHITE${item.prefix}$DEFAULT${item.separator}${item.text}")
    }
    println
  }

  def loop(clear: Boolean = true, redisplayMenu: Boolean = true, redisplayName: Boolean = true) = {
    displayMenuToUser()
    reader.getCompleters.foreach(
      completer => reader.removeCompleter(completer)
    )
    reader.addCompleter(new StringsCompleter(_items.map(i => i.prefix).toList))
    var loop = true
    do {
      val line = readLine(Some("Your Choice"), None, defaultSelection).trim
      _items.toList.search(mi => mi.prefix == line) match {
        case Some(mi) => val stayInMenu = mi.action()
          if (stayInMenu) {
            if (redisplayMenu) {
              displayMenuToUser(clear, redisplayName)
            }
          } else {
            loop = false
          }
        case None =>
      }
    } while (loop)
  }

  private[this] def clearScreen(): Unit = {
    //reader.clearScreen()
    System.out.print("\033[H\033[2J")
    System.out.flush()
  }

}
