package io.geekabyte.ristex.parsers

import atto.Atto._
import atto._
import Util.lb

object CommentLines {

  val initComment: Parser[String] = {
    (many(notChar('#')) ~ char('#').map(_.toString)) ~> {
      many(letterOrDigit | spaceChar).map(_.mkString) <~ manyN(0, lb)
    }
  }

  val comment = {
    char('#') ~> many(letterOrDigit | spaceChar).map(_.mkString) <~ manyN(0, lb)
  }

  val all: Parser[List[String]] = {
    val aComment: Parser[String] = many(notChar('#')) ~> initComment
    many(aComment)
  }

}
