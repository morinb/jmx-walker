package com.bnpparibas.grp.jmx.menu


import com.bnpparibas.grp.jmx.menu.RichList._
import jline.console.ConsoleReader
import jline.console.completer.StringsCompleter

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  *
  * @author morinb.
  */
class Menu(val name: String, var defaultSelection: Option[String] = None)(implicit val reader: ConsoleReader) {
  var _items: mutable.MutableList[MenuItem] = mutable.MutableList()

  def +=(menuItem: MenuItem) = _items += menuItem

  def -=(menuItem: MenuItem) = _items = _items.filterNot(i => i == menuItem)

  def add(menuItem: MenuItem) = this += menuItem

  def remove(menuItem: MenuItem) = this -= menuItem

  def removeAll() = _items.clear()

  def items() = _items.toList

  private[this] def displayMenuToUser(clear: Boolean = true): Unit = {
    if (clear) {
      clearScreen()
    }
    println(name)
    println
    for (item <- _items) {
      println(s"  ${item.prefix} ${item.separator} ${item.text}")
    }
    println
  }

  def loop(clear: Boolean = true, redisplayMenu: Boolean = true) = {
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
            if(redisplayMenu) {
              displayMenuToUser(clear)
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

  @tailrec
  private[this] final def readLine(prompt: Option[String] = None, mask: Option[Char] = None, defaultValue: Option[String] = None): String = {
    val newPrompt = defaultValue match {
      case Some(text) =>
        val newText = if (text.isEmpty) text
        else mask match {
          case Some(c) => s" [${text.replaceAll(".", "" + c)}]"
          case None => s" [$text]"
        }
        s"${prompt.getOrElse("")}$newText"
      case None => prompt.getOrElse("")
    }
    val read = mask match {
      case Some(char) => reader.readLine(newPrompt + " > ", char)
      case None => reader.readLine(newPrompt + " > ")
    }
    if (read == null) {
      // Ctrl-D or something pressed
      println("Exiting...")
      System.exit(0)
      null // just for the return types, program won't go further previous line.
    } else {
      if (read == "") {
        if (defaultValue.isDefined) {
          defaultValue.get
        } else {
          readLine(prompt, mask, defaultValue) // Ask again ...
        }
      } else {
        read
      }
    }
  }


}
