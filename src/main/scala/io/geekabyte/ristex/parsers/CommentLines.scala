package io.geekabyte.ristex.parsers

import atto.Atto._
import atto._
import Util.lb

object CommentLines {

  val firstComment: Parser[String] = {
    (many(notChar('#')) ~ char('#').map(_.toString)) ~> {
      many(letterOrDigit | spaceChar).map(_.mkString) <~ lb
    }
  }

  val comment = {
    char('#') ~> many(letterOrDigit | spaceChar).map(_.mkString) <~ lb
  }

  val all: Parser[List[String]] = {
    val aComment: Parser[String] = many(notChar('#')) ~> firstComment
    many(aComment)
  }

}
