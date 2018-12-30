package io.geekabyte.parsers


import atto.Atto._
import atto._
import cats.implicits._

object Util {

  val pipe: Parser[Char] =  char('|')

  val summaryPipe: Parser[String] = (pipe, char('*'), pipe).mapN((p1: Char, c: Char, p2: Char) => s"$p1$c$p2")

  val eol: Parser[Char] = char('\n')

  val skipToEndOfLine: Parser[Unit] = many(skip(_ != '\n')).map(_ => ())

  val skipToStartOfNextLine: Parser[Unit] = skipToEndOfLine <~ char('\n')

  def range(value: Int, lower: Int, upper: Int, errMsg: String): Parser[Int] = {
    if (value < lower || value > upper) {
      err[Int](errMsg)
    } else {
      ok(value)
    }
  }

  def anyCharExcept(excepts: String*): Parser[Char] = {
    anyChar.filter((character: Char) => !excepts.contains(character.toString))
  }

  def anyStringOrDigit: Parser[String] = {
    many(letterOrDigit).map(_.mkString)
  }

  def fixed(n:Int): Parser[Int] = {
    count(n, digit).map(_.mkString).flatMap { s =>
      try ok(s.toInt) catch { case e: NumberFormatException => err(e.toString) }
    }
  }
}
