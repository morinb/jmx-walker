package com.bnpparibas.grp.jmx

import javax.management.{MBeanOperationInfo, ObjectName}

import com.bnpparibas.grp.jmx.AnsiColor._
import com.bnpparibas.grp.jmx.ClassUtil.convert
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
  // let the parenthesis arout the multistring else it won't work !
  val banner: String = BRIGHT_GREEN + (
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
    """.stripMargin) + DEFAULT
  implicit val reader = new ConsoleReader()
  val lineReader = new LineReader
  // Little trick since we only define reader here
  import lineReader.readLine

  var completer = new StringsCompleter()
  reader.setCompletionHandler(new CandidateListCompletionHandler())

  def main(args: Array[String]): Unit = {
    val startMenu = new Menu(banner, Some("1"))
    startMenu += new MenuItem("1", "Connect to an environment", " - ")(() => {
      displayConnectLoop()
      true
    })
    startMenu += Separator
    startMenu += new MenuItem("q", "Quit the application", " - ")(() => {
      println("Exiting...")
      sys.exit(0)
    })

    startMenu.loop()
  }

  def displayConnectLoop(): Unit = {
    var loop = true
    reader.getCompleters.foreach(c => reader.removeCompleter(c))
    do {
      printBanner()
      val server = readLine(Some("What is the server address"), None, Some("acetp-ude-m1"))
      val admin = readLine(Some("Is this an admin instance (Yes/No)"), None, Some("No"))
      val isAdmin = admin.toLowerCase.matches("[yY][eE]?[sS]?")
      val protocol = readLine(Some("What is the server protocol (t3, iiop, rmi)"), None, Some("t3"))
      val port = readLine(Some("What is the server port"), None, if(isAdmin) Some("9091") else Some("9080"))
      val login = readLine(Some("What is the server login"), None, Some("weblogic"))
      val password = readLine(Some("What is the server password"), Some('\0')) // don't ever display password

      try {
        val creds = new Credentials(server, port.toInt, login, password.toCharArray)

        println(s"Trying to connect ${if (isAdmin) s" $RED(admin)$DEFAULT " else ""} to $creds ...")
        val walker = new JmxWalker(protocol, creds, isAdmin)
        walker.connect()
        val mbeanNames = asScalaSet(walker.getConnection.queryNames(null, null)).toList
        displayWalkerLoop(walker, mbeanNames)
        walker.disconnect()
      } catch {
        case e: Exception => println("An exception occured : ")
          e.printStackTrace()
          typeKeyToContinue()
      }
      loop = false

    } while (loop)

  }

  def displayWalkerLoop(walker: JmxWalker, names: List[ObjectName]): Unit = {

    val walkerMenu = new Menu(banner + s"\n  [$CYAN${walker.credentials.toString}$DEFAULT]")

    walkerMenu += new MenuItem("1", "List all MBeans", " - ")(() => {
      printAllMBeans(names.zipWithIndex)
      true
    })
    walkerMenu += new MenuItem("2", "List all MBeans that contains a string", " - ")(() => {
      val string = readLine(Some("String"))
      printAllMBeans(names.zipWithIndex.filter(tuple => tuple._1.getCanonicalName.contains(string)), Some(string))
      true
    })
    walkerMenu += new MenuItem("3", "Info of a MBean by its name ", " - ")(() => {
      val mbeanName = readLine(Some("MBean Name"))
      displayMBeanInfoDetailsLoop(walker, mbeanName)
      true
    })
    walkerMenu += new MenuItem("4", "Info of a MBean by its number", " - ")(() => {
      val number = readLine(Some("MBean Number"))
      val name = names(number.toInt).getCanonicalName
      displayMBeanInfoDetailsLoop(walker, name)
      true
    })
    walkerMenu += Separator
    walkerMenu += new MenuItem("q", "Quit this Menu", " - ")(() => {
      false
    })

    clearScreen()
    walkerMenu.loop(clear = false, redisplayMenu = true, redisplayName = false)
  }

  def impact(impact: Int) = impact match {
    case MBeanOperationInfo.INFO => s"${BRIGHT_GREEN}Should not alter the state of the MBean component$DEFAULT"
    case MBeanOperationInfo.ACTION => s"${BRIGHT_RED}Change the state of the MBean component$DEFAULT"
    case MBeanOperationInfo.ACTION_INFO => s"${RED}Behaves la a read/write operation$DEFAULT"
    case MBeanOperationInfo.UNKNOWN => s"${DEFAULT}Reserved for standard MBeans$DEFAULT"
    case _ => impact.toString

  }

  def isRunnable(oper: MBeanOperationInfo) = {
    oper.getSignature.isEmpty || oper.getSignature.forall(p => convert(p.getType) == "String")
  }

  def display(result: Option[AnyRef]): String = {
    result match {
      case None => s"${BRIGHT_YELLOW}No result$DEFAULT"
      case Some(a: Array[String]) => GREEN+a.mkString("\n")+DEFAULT
      case Some(a) => GREEN+a.toString+DEFAULT
    }
  }

  def displayMBeanInfoDetailsLoop(walker: JmxWalker, mbeanName: String): Unit = {


    val mbeanInfos = walker.getConnection.getMBeanInfo(new ObjectName(mbeanName))
    def getHeader: String = {
      val sb = new StringBuilder

      sb.append(s" ${BRIGHT_WHITE}Constructors:$DEFAULT\n")
      mbeanInfos.getConstructors.foreach { ctor =>
        val signature: String = ctor.getSignature.map(s => s"${s.getName}: ${convert(s.getType)}").mkString(", ")
        sb.append(s"\t${ctor.getName}($signature)\n")
      }
      sb.append(s"\n ${BRIGHT_WHITE}Attributes:$DEFAULT\n")
      mbeanInfos.getAttributes.foreach { attr =>
        sb.append(s"\t${attr.getName} : ${convert(attr.getType)}\n")
      }

      sb.append(s"\n ${BRIGHT_WHITE}Notifications:$DEFAULT\n")
      mbeanInfos.getNotifications.foreach { noti =>
        val notifTypes: String = noti.getNotifTypes.map(t => convert(t)).mkString(", ")
        sb.append(s"\t${noti.getName} : $notifTypes\n")
      }

      sb.append(s"\n ${BRIGHT_WHITE}Operations:$DEFAULT\n")
      mbeanInfos.getOperations.foreach { oper =>
        val runnable = isRunnable(oper)
        val signature: String = oper.getSignature.map(s => s"${s.getName}: $GREEN${convert(s.getType)}$DEFAULT").mkString(", ")
        sb.append(s"\t$CYAN${oper.getName}$DEFAULT($signature):$GREEN${convert(oper.getReturnType)}$DEFAULT (${impact(oper.getImpact)})     ${
          if (runnable) {
            s"$BRIGHT_GREEN[ Runnable ]$DEFAULT"
          } else {
            s"$RED[ Not Runnable for the moment]$DEFAULT"
          }
        }\n")
      }
      sb.toString()
    }


    val detailMenu = new Menu(banner + s"\n  $CYAN[$mbeanName]$DEFAULT\n\n$getHeader")
    detailMenu += new MenuItem("1", "Constructor ", " - ")(() => {
      true
    })
    detailMenu += new MenuItem("2", "Attribute value", " - ")(() => {
      val attributeMenu = new Menu(banner)

      for (attr <- mbeanInfos.getAttributes) {
        attributeMenu += new MenuItem(attr.getName, convert(attr.getType), " : ")(() => {
          val value = walker.getConnection.getAttribute(new ObjectName(mbeanName), attr.getName)
          println(s"\n${attr.getName} = $value\n")
          true
        })
      }
      attributeMenu += Separator
      attributeMenu += new MenuItem("q", "Quit the menu", " - ")(() => {
        false
      })

      attributeMenu.loop(clear = false, redisplayMenu = false)

      true
    })
    detailMenu += new MenuItem("3", "Notification", " - ")(() => {
      true
    })
    detailMenu += new MenuItem("4", "Call Operation", " - ")(() => {
      val notificationMenu = new Menu(banner)

      for (oper <- mbeanInfos.getOperations) {
        val signature: String = oper.getSignature.map(s => s"${s.getName}: ${convert(s.getType)}").mkString(", ")
        if (isRunnable(oper)) {
          notificationMenu += new MenuItem(oper.getName, s"($signature):${convert(oper.getReturnType)}", "")(() => {
            if (oper.getSignature.isEmpty) {
              val result = walker.getConnection.invoke(new ObjectName(mbeanName), oper.getName, null, null)
              println(display(Option(result)))
            } else {
              val params: Array[AnyRef] = Array.ofDim(oper.getSignature.length)
              val sign: Array[String] = Array.ofDim(oper.getSignature.length)
              for ((param, b) <- oper.getSignature.zipWithIndex) {
                params(b) = readLine(Some(param.getName))
                sign(b) = classOf[String].getName
              }
              val result = walker.getConnection.invoke(new ObjectName(mbeanName), oper.getName, params, sign)
              println(display(Option(result)))
            }
            true
          })
        }
      }
      notificationMenu += Separator
      notificationMenu += new MenuItem("q", "Quit this Menu", " - ")(() => {
        false
      })

      notificationMenu.loop(clear = false, redisplayMenu = false)

      true
    })
    detailMenu += Separator
    detailMenu += new MenuItem("q", "Quit to menu", " - ")(() => {
      clearScreen()
      println(banner)
      false
    })


    try {
      detailMenu.loop()
    } catch {
      case e: Exception => e.printStackTrace()
        typeKeyToContinue()
    }
  }


  def printAllMBeans(names: List[(ObjectName, Int)], highlight: Option[String] = None): Unit = {
    println
    if (names.size > pagination) {
      clearScreen()
      paginate(names, pagination, highlight = highlight)
      clearScreen()
      println(banner)
    } else {
      for (name <- names) {
        val canonicalName: String = name._1.getCanonicalName
        val colorizedName = highlight match {
          case None => canonicalName
          case Some(string) => canonicalName.replaceAll(highlight.get, BRIGHT_MAGENTA + highlight.get + DEFAULT)
        }
        println(s" $BRIGHT_WHITE${name._2}$DEFAULT - $colorizedName")
      }
    }
    println

  }

  @tailrec
  def paginate(names: List[(ObjectName, Int)], pageSize: Int, startIndex: Int = 0, highlight: Option[String] = None): Unit = {

    if (pageSize + startIndex >= names.size) {
      // Display last item
      println(s"\n${CYAN}Displaying$DEFAULT $startIndex ${CYAN}to$DEFAULT $BRIGHT_WHITE${names.size}$DEFAULT $CYAN-$DEFAULT $BRIGHT_WHITE${names.size}$DEFAULT ${CYAN}items$DEFAULT")
      for (i <- startIndex until names.size) {
        val name: String = names(i)._1.getCanonicalName
        val colorizedName = highlight match {
          case None => name
          case Some(string) => name.replaceAll(highlight.get, BRIGHT_MAGENTA + highlight.get + DEFAULT)
        }
        println(s"\n $BRIGHT_WHITE${names(i)._2}$DEFAULT - $colorizedName")
      }
      typeKeyToContinue()
    } else {

      println(s"\n${CYAN}Displaying$DEFAULT $BRIGHT_WHITE${startIndex + pageSize}$DEFAULT ${CYAN}to$DEFAULT $BRIGHT_WHITE${names.size}$DEFAULT $CYAN-$DEFAULT $BRIGHT_WHITE${names.size}$DEFAULT ${CYAN}items$DEFAULT")

      // Display the next pageSize items
      for (i <- startIndex until startIndex + pageSize) {
        val name: String = names(i)._1.getCanonicalName
        val colorizedName = highlight match {
          case None => name
          case Some(string) => name.replaceAll(highlight.get, BRIGHT_MAGENTA + highlight.get + DEFAULT)
        }
        println(s"\n $BRIGHT_WHITE${names(i)._2}$DEFAULT - $colorizedName")
      }
      typeEnterKeyToContinueOrQToAbort match {
        case None =>
        case Some(_) => paginate(names, pageSize, startIndex + pageSize) // Display next 
      }

    }
  }

  def typeKeyToContinue(): Int = {
    println("\nType any key to continue > ")
    reader.readCharacter()
  }

  def typeEnterKeyToContinueOrQToAbort: Option[Int] = {
    println("\nType any key to continue, or q to abort > ")
    reader.readCharacter() match {
      case 'q' => None
      case 'Q' => None
      case c => Some(c)
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

}
