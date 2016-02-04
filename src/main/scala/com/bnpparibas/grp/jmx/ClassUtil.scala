package com.bnpparibas.grp.jmx

import scala.annotation.tailrec

/**
  *
  * @author morinb.
  */
object ClassUtil {

  /**
    * <pre>
    * Element Type    <--   Encoding
    * boolean               Z
    * byte                  B
    * char                  C
    * class or interface    Lclassname;
    * double                D
    * float                 F
    * int                   I
    * long                  J
    * short                 S 
    * </pre>
    */
  @tailrec
  def convert(classRepresentation: String, prefix: String = "", suffix: String = ""): String = {
    if (classRepresentation startsWith "[") {
      if (classRepresentation.startsWith("[L")) {
        convert(classRepresentation.substring(2, classRepresentation.length - 1), "Array[" + prefix, suffix + "]")
      } else {
        convert(classRepresentation.substring(1), "Array[" + prefix, suffix + "]")
      }
    } else {
      val res = classRepresentation match {
        case "Z" => "Boolean"
        case "B" => "Byte"
        case "C" => "Char"
        case "D" => "Double"
        case "F" => "Float"
        case "I" => "Int"
        case "J" => "Long"
        case "S" => "Short"
        case string => if (string.contains(".")) {
          string.substring(string.lastIndexOf(".")+1)
        }
        else {
          string
        }
      }
      prefix + res + suffix
    }


  }


}
