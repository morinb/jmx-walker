package com.github.morinb.jmx.walker

import com.github.morinb.jmx.walker.AnsiColor._
import jline.console.ConsoleReader

import scala.annotation.tailrec

/**
  *
  * @author morinb.
  */
class LineReader(implicit val reader: ConsoleReader) {
  @tailrec
  final def readLine(prompt: Option[String] = None, mask: Option[Char] = None, defaultValue: Option[String] = None): String = {
    val newPrompt = defaultValue match {
      case Some(text) =>
        val defaultOption = if (text.isEmpty) text
        else mask match {
          case Some(c) => s" [${text.replaceAll(".", "" + c)}]"
          case None => s" [$text]"

        }
        s"${prompt.getOrElse("")}$BRIGHT_WHITE$defaultOption$DEFAULT"
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
