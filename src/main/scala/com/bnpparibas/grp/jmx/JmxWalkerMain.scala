package com.bnpparibas.grp.jmx

import javax.management.ObjectName

import com.bnpparibas.grp.jmx.menu.{Menu, MenuItem, Separator}
import jline.console.ConsoleReader
import jline.console.completer.{CandidateListCompletionHandler, StringsCompleter}

import scala.annotation.tailrec
import scala.collection.JavaConversions._


/**
  *
  * @author morinb.
  */
object JmxWalkerMain {
  val pagination = 20

  val banner: String =
    """
      |  ____  ___ ___ __ __                       
      | |    ||   |   |  |  |                      
      | |__  || _   _ |  |  |                      
      | __|  ||  \_/  |_   _|                      
      |/  |  ||   |   |     |                      
      |\  `  ||   |   |  |  |                      
      | \____||___|___|__|__|                      
      |     __    __  ____ _     __  _   ___ ____  
      |    |  |__|  |/    | |   |  |/ ] /  _]    \ 
      |    |  |  |  |  o  | |   |  ' / /  [_|  D  )
      |    |  |  |  |     | |___|    \|    _]    / 
      |    |  `  '  |  _  |     |     \   [_|    \ 
      |     \      /|  |  |     |  .  |     |  .  \
      |      \_/\_/ |__|__|_____|__|\_|_____|__|\_|
      |                                            
    """.stripMargin
  implicit val reader = new ConsoleReader()

  var completer = new StringsCompleter()
  reader.setCompletionHandler(new CandidateListCompletionHandler())

  def main(args: Array[String]): Unit = {
    val startMenu = new Menu(banner)
    startMenu += new MenuItem("1", "Connect to an environment", "-")(() => {
      displayConnectLoop()
      true
    })
    startMenu += Separator
    startMenu += new MenuItem("q", "Quit the application", "-")(() => {
      println("Exiting...")
      sys.exit(0)
    })




    startMenu.loop()

  }

  def displayConnectLoop(): Unit = {
    var loop = true

    do {
      printBanner()
      val server = readLine(Some("What is the server address"), None, Some("acetp-ude-m1"))
      val admin = readLine(Some("Is this an admin instance (Yes/No)"), None, Some("No"))
      val isAdmin = admin.toLowerCase.matches("[yY][eE]?[sS]?")
      val protocol = readLine(Some("What is the server protocol (t3, iiop, rmi)"), None, Some("t3"))
      val port = readLine(Some("What is the server port"), None, Some("9080"))
      val login = readLine(Some("What is the server login"), None, Some("weblogic"))
      val password = readLine(Some("What is the server password"), Some('\0'), Some("bnpparibas!")) // don't ever display password

      val creds = new Credentials(server, port.toInt, login, password.toCharArray)

      println(s"Trying to connect ${if (isAdmin) " (admin) " else ""} to $creds ...")
      try {
        val walker = new JmxWalker(protocol, creds, isAdmin)
        walker.connect()
        val mbeanNames = asScalaSet(walker.getConnection.queryNames(null, null)).toList
        displayWalkerLoop(walker, mbeanNames)
        walker.disconnect()
        loop = false
      } catch {
        case e: Exception => println("An exception occured : ")
          e.printStackTrace()
          typeEnterKeyToContinue()
      }

    } while (loop)

  }

  def displayWalkerLoop(walker: JmxWalker, names: List[ObjectName]): Unit = {

    val walkerMenu = new Menu(banner + s"\n  [${walker.credentials}]")

    walkerMenu += new MenuItem("1", "List all MBeans", "-")(() => {
      printAllMBeans(names.zipWithIndex)
      true
    })
    walkerMenu += new MenuItem("2", "List all MBeans that contains a string", "-")(() => {
      val string = readLine(Some("String"))
      printAllMBeans(names.zipWithIndex.filter(tuple => tuple._1.getCanonicalName.contains(string)))
      true
    })
    walkerMenu += new MenuItem("3", "Info of a MBean by its name ", "-")(() => {
      val mbeanName = readLine(Some("MBean Name"))
      displayMBeanInfoDetailsLoop(walker, mbeanName)
      true
    })
    walkerMenu += new MenuItem("4", "Info of a MBean by its number", "-")(() => {
      val number = readLine(Some("MBean Number"))
      val name = names(number.toInt).getCanonicalName
      displayMBeanInfoDetailsLoop(walker, name)
      true
    })
    walkerMenu += Separator
    walkerMenu += new MenuItem("q", "Quit this Menu", "-")(() => {
      false
    })

    walkerMenu.loop()
    /*
        var loop = true
    
        do {
          printBanner()
          println(
            """
              | 1 - List all MBeans
              | 2 - List all MBeans that contains a string
              | 3 - Info of a MBean by its name 
              | 4 - Info of a MBean by its number
              |
              | q - Quit this Menu
            """.stripMargin)
    
          val line = readLine(Some("Your choice"))
          try {
            line match {
              case "1" => printAllMBeans(names.zipWithIndex)
              case "2" => val string = readLine(Some("String"))
                printAllMBeans(names.zipWithIndex.filter(tuple => tuple._1.getCanonicalName.contains(string)))
              case "3" => val mbeanName = readLine(Some("MBean Name"))
                displayMBeanInfoDetailsLoop(walker, mbeanName)
              case "4" => val number = readLine(Some("MBean Number"))
                val name = names(number.toInt).getCanonicalName
                displayMBeanInfoDetailsLoop(walker, name)
              case "q" => loop = false
              case _ =>
            }
          } catch {
            case e: Exception => println("An exception occured : ")
              e.printStackTrace()
              typeEnterKeyToContinue()
          }
        } while (loop)*/
  }

  def displayMBeanInfoDetailsLoop(walker: JmxWalker, mbeanName: String): Unit = {
    var loop = true

    do {

      println(s"You asked for information on $mbeanName")
      val mbeanInfos = walker.getConnection.getMBeanInfo(new ObjectName(mbeanName))
      println("\nConstructors:")
      for (ctor <- mbeanInfos.getConstructors) {
        val signature: String = ctor.getSignature.map(s => s"${s.getName}: ${s.getType}").mkString(", ")
        println(s"\t${ctor.getName}($signature)")
      }
      println("\nAttributes:")

      val attributeNames = mbeanInfos.getAttributes.map(a => a.getName)
      for (attr <- mbeanInfos.getAttributes) {
        println(s"\t${attr.getName} : ${attr.getType}")
      }
      println("\nNotifications:")
      for (noti <- mbeanInfos.getNotifications) {
        val notifTypes: String = noti.getNotifTypes.mkString(",")
        println(s"\t${noti.getName} : $notifTypes")
      }
      println("\nOperations:")
      for (oper <- mbeanInfos.getOperations) {
        val signature: String = oper.getSignature.map(s => s"${s.getName}: ${s.getType}").mkString(", ")
        println(s"\t${oper.getName}($signature):${oper.getReturnType}")
      }

      println(
        """
          | 1 - Constructor 
          | 2 - Attribute value
          | 3 - Notification
          | 4 - Call Operation
          |
          | q - Quit to menu
        """.stripMargin)
      try {
        val line = readLine(Some("Your Choice"))
        line match {
          case "q" => loop = false
          case "2" =>
            var loop2 = true
            do {
              try {

                reader.getCompleters.foreach(c => reader.removeCompleter(c))
                reader.addCompleter(new StringsCompleter(attributeNames.toList))
                val attrName = readLine(Some("Attribute Name, or q to interrupt")).trim
                attrName match {
                  case "q" => loop2 = false
                    reader.getCompleters.foreach(c => reader.removeCompleter(c))
                  case _ =>
                    val value = walker.getConnection.getAttribute(new ObjectName(mbeanName), attrName)
                    println(s"\n$attrName = $value\n")

                }
              } catch {
                case e: Exception => println("Invalid attribute name !")
                  loop2 = false
              }
            } while (loop2)
            typeEnterKeyToContinue()
          case _ =>
        }
      } catch {
        case e: Exception => println("An exception occured : ")
          e.printStackTrace()
          typeEnterKeyToContinue()
      }
      clearScreen()

    } while (loop)
  }


  def printAllMBeans(names: List[(ObjectName, Int)]): Unit = {
    if (names.size > pagination) {
      paginate(names, pagination)
    } else {
      for (name <- names) {
        println(name._2 + " : " + name._1.getCanonicalName)
      }
      typeEnterKeyToContinue()
    }

  }

  @tailrec
  def paginate(names: List[(ObjectName, Int)], pageSize: Int, startIndex: Int = 0): Unit = {

    if (pageSize + startIndex >= names.size) {
      // Display last item
      println(s"Displaying $startIndex to ${names.size} / ${names.size} items")
      for (i <- startIndex until names.size) {
        println(s"\n${names(i)._2} - ${names(i)._1.getCanonicalName}")
      }
      typeEnterKeyToContinue()
    } else {

      println(s"Displaying $startIndex to ${startIndex + pageSize} / ${names.size} items")
      // Display the next pageSize items
      for (i <- startIndex until startIndex + pageSize) {
        println(s"\n${names(i)._2} - ${names(i)._1.getCanonicalName}")
      }
      typeEnterKeyToContinueOrQToAbort match {
        case None =>
        case Some(_) => paginate(names, pageSize, startIndex + pageSize) // Display next 
      }

    }
  }

  def typeEnterKeyToContinue(): Unit = {
    readLine(Some("Type Enter key to continue"), Some('\0'), Some("")) // Allow empty result
  }

  def typeEnterKeyToContinueOrQToAbort: Option[String] = {
    val line = readLine(Some("Type Enter key to continue, or q to abort."), None, Some("")) // Allow empty result
    line match {
      case "q" => None
      case _ => Some(line)
    }
  }

  def printBanner(): Unit = {
    // Clear screen
    clearScreen()
    println(banner)
  }


  def clearScreen(): Unit = {
    //reader.clearScreen()
    System.out.print("\033[H\033[2J")
    System.out.flush()
  }


  @tailrec
  def readLine(prompt: Option[String] = None, mask: Option[Char] = None, defaultValue: Option[String] = None): String = {
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

  def showMenu(entries: List[Option[(String, String)]], defaultValue: Option[String], completions: List[String] = Nil): String = {
    for (entry <- entries) {
      entry match {
        case None => println
        case Some(item) => println(s" ${item._1} - ${item._2}")
      }
    }

    reader.getCompleters.foreach(c => reader.removeCompleter(c))
    reader.addCompleter(new StringsCompleter(completions))
    var line = ""
    do {
      line = readLine(Some("Your choice"), None, defaultValue)
    } while (line.isEmpty)
    line
  }

}
