package io.geekabyte.parsers

import atto.Atto._
import atto._
import Util.eol

object CommentLines {

  val comment: Parser[String] = {
    (many(notChar('#')) ~ char('#').map(_.toString)) ~> {
      many(letterOrDigit).map(_.mkString) <~ manyN(0, eol)
    }
  }

  val all: Parser[List[String]] = {
    val aComment: Parser[String] = many(notChar('#')) ~> comment
    many(aComment)
  }

}
