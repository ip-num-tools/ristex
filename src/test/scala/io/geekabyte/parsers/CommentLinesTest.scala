package io.geekabyte.parsers

import atto.Atto._
import atto._
import org.scalatest.{FunSuite, OptionValues}

import scala.io.Source

class CommentLinesTest extends  FunSuite with OptionValues {

  test("Parsing a comment") {
    val recordsWithComments: Iterator[String] = Source.fromResource("records_with_comments").getLines()
    val parseResult: ParseResult[String] = CommentLines.comment.parseOnly(recordsWithComments.toList.mkString("\n"))
    assert(parseResult.option.value == "comment1")
  }

  test("Parsing all comments") {
    val recordsWithComments: Iterator[String] = Source.fromResource("records_with_comments").getLines()
    val parseResult: ParseResult[List[String]] = CommentLines.all.parseOnly(recordsWithComments.toList.mkString("\n"))
    assert(parseResult.option.value == List("comment1", "comment2", "comment3"))
  }

}
