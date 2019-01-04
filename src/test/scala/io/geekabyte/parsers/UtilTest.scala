package io.geekabyte.parsers

import atto.Atto._
import atto._
import atto.parser.character
import io.geekabyte.ristex.parsers.{Base, CommentLines, Util}
import org.scalatest.{FunSuite, OptionValues}

import scala.io.Source

class UtilTest extends  FunSuite with OptionValues {

  private val commentedRecords: String =
    Source
      .fromResource("records_with_comments")
      .getLines()
      .toList.mkString("\n")

  test("skipToEndOfLine") {

    val result: ParseResult[Char] = (
      Base.versionParser ~    // start by parsing version, then...
      Util.skipToEndOfLine ~> // skip to end of line,
      char('\n') // next parsed should be new line
      ).parseOnly(commentedRecords)

    assert(result.option.get.equals('\n'))
  }

  test("skipToStartOfNextLine") {

    val result: ParseResult[String] = (
      Base.versionParser ~    // start by parsing version, then...
        Util.skipToStartOfNextLine ~> // skip to start of next line,
        CommentLines.firstComment // next parsed should be comment
      ).parseOnly(commentedRecords)

    assert(result.option.get.equals(" this is comment1"))
  }

}
