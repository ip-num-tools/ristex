package io.geekabyte.parsers

import atto.Atto._
import atto._
import io.geekabyte.parsers.FileHeader.{SummaryLine, VersionLine}
import org.scalatest.{FunSpec, FunSuite, OptionValues}
import shapeless.PolyDefns.~>

import scala.io.Source

class FileHeaderTest extends  FunSpec with OptionValues {

  private val records: String =
    Source
      .fromResource("records_short")
      .getLines()
      .toList.mkString("\n")

  describe("FileHeader") {
    describe("VersionLine") {

      it("should initParse") {
        val parseResult: ParseResult[(Int, String, Int, Int, String, String, String)] =
          FileHeader
            .VersionLine
            .initParse
            .parseOnly(records)

        assert(parseResult.option.value == (2,"ripencc",1544569199,123397,"19830705","20181211","+0100"))
      }

      it("should parse version") {
        val parseResult: ParseResult[Int] =
          FileHeader
            .VersionLine
            .version
            .parseOnly(records)

        assert(parseResult.option.value == 2)
      }

      it("should parse registry") {
        val parseResult: ParseResult[String] =
          FileHeader
            .VersionLine
            .registry
            .parseOnly(records)

        assert(parseResult.option.value == "ripencc")
      }

      it("should parse serial") {
        val parseResult: ParseResult[Int] =
          FileHeader
            .VersionLine
            .serial
            .parseOnly(records)

        assert(parseResult.option.value == 1544569199)
      }

      it("should parse records") {
        val parseResult: ParseResult[Int] =
          FileHeader
            .VersionLine
            .records
            .parseOnly(records)

        assert(parseResult.option.value == 123397)
      }

      it("should parse start date") {
        val parseResult: ParseResult[String] =
          FileHeader
            .VersionLine
            .startDate
            .parseOnly(records)

        assert(parseResult.option.value == "19830705")
      }

      it("should parse end date") {
        val parseResult: ParseResult[String] =
          FileHeader
            .VersionLine
            .endDate
            .parseOnly(records)

        assert(parseResult.option.value == "20181211")
      }

      it("should parse utc") {
        val parseResult: ParseResult[String] =
          FileHeader
            .VersionLine
            .UTCoffset
            .parseOnly(records)

        assert(parseResult.option.value == "+0100")
      }
    }
    describe("SummaryLine") {

      it("should initParse") {
        val parseResult: ParseResult[List[(String, String, Int, String)]] =
          FileHeader
            .SummaryLine
            .initParse
            .parseOnly(records)

        assert(parseResult.option.value == List(
          ("ripencc","ipv4",71111,"summary1"),
          ("ripencc","asn",33984,"summary2"),
          ("ripencc","ipv6",18302,"summary3"))
        )
      }

      it("should parse first summary line") {
        val headerVersionLine: Parser[((Int, String, Int, Int, String, String, String), Char)] =
          VersionLine.initParse ~ char('\n')

        val summaryLineParser: Parser[(String, String, Int, String)] = FileHeader
          .SummaryLine
          .next


        val parseResult: ParseResult[(String, String, Int, String)] =
          (headerVersionLine ~> summaryLineParser)
            .parseOnly(records)

        assert(parseResult.option.value == ("ripencc","ipv4",71111,"summary1"))
      }

      it("should parse second summary line") {
        val headerVersionLine: Parser[((Int, String, Int, Int, String, String, String), Char)] =
          VersionLine.initParse ~ char('\n')

        val summaryLineParser: Parser[(String, String, Int, String)] = FileHeader
          .SummaryLine
          .next

        val parseResult: ParseResult[(String, String, Int, String)] =
          ((headerVersionLine ~ summaryLineParser ~ Util.eof) ~> summaryLineParser)
            .parseOnly(records)

        assert(parseResult.option.value == ("ripencc","asn",33984,"summary2"))
      }

      it("should parse third summary line") {
        val headerVersionLine: Parser[((Int, String, Int, Int, String, String, String), Char)] =
          VersionLine.initParse ~ char('\n')

        val summaryLineParser: Parser[(String, String, Int, String)] = FileHeader
          .SummaryLine
          .next

        val parseResult: ParseResult[(String, String, Int, String)] =
          ((headerVersionLine ~ summaryLineParser ~ Util.eof ~ summaryLineParser ~ Util.eof) ~> summaryLineParser)
            .parseOnly(records)

        assert(parseResult.option.value == ("ripencc","ipv6",18302,"summary3"))
      }

      it("should parse registry in first summary line") {
        val parseResult: ParseResult[String] =
          FileHeader
            .SummaryLine
            .initRegistry
            .parseOnly(records)
        assert(parseResult.option.value == "ripencc")
      }

      it("should parse registry in second summary line") {
        val parseResult: ParseResult[String] =
          ((FileHeader
            .SummaryLine
            .initRegistry ~ Util.eof) ~>
            FileHeader.SummaryLine.nextRegistry // second line
            )
            .parseOnly(records)
        assert(parseResult.option.value == "ripencc")
      }

      it("should parse registry in third summary line") {
        val parseResult: ParseResult[String] =
          ((FileHeader
            .SummaryLine
            .initRegistry ~ Util.eof ~ FileHeader.SummaryLine.nextRegistry ~ Util.eof) ~>
            FileHeader.SummaryLine.nextRegistry // third line
            )
            .parseOnly(records)
        assert(parseResult.option.value == "ripencc")
      }

      it("should parse type in first summary line") {
        val parseResult: ParseResult[String] =
          FileHeader
            .SummaryLine
            .initType
            .parseOnly(records)
        
        assert(parseResult.option.value == "ipv4")
      }

      it("should parse type in second summary line") {
        val parseResult: ParseResult[String] =
          ((FileHeader
            .SummaryLine
            .initType ~ Util.eof) ~> FileHeader.SummaryLine.nextType)
            .parseOnly(records)
        
        assert(parseResult.option.value == "asn")
      }

      it("should parse type in third summary line") {
        val parseResult: ParseResult[String] =
          ((FileHeader
            .SummaryLine
            .initType ~ Util.eof ~ FileHeader.SummaryLine.nextType ~ Util.eof) ~> FileHeader.SummaryLine.nextType)
            .parseOnly(records)
        
        assert(parseResult.option.value == "ipv6")
      }

      it("should parse count in first summary line") {
        val parseResult: ParseResult[Int] =
          FileHeader
            .SummaryLine
            .initCount
            .parseOnly(records)
        
        assert(parseResult.option.value == 71111)
      }

      it("should parse count in second summary line") {
        val parseResult: ParseResult[Int] =
          ((FileHeader
            .SummaryLine
            .initCount ~ Util.eof) ~> FileHeader.SummaryLine.nextCount)
            .parseOnly(records)
        
        assert(parseResult.option.value == 33984)
      }

      it("should parse count in third summary line") {
        val parseResult: ParseResult[Int] =
          ((FileHeader
            .SummaryLine
            .initCount ~ Util.eof ~ FileHeader.SummaryLine.nextCount ~ Util.eof) ~> FileHeader.SummaryLine.nextCount)
            .parseOnly(records)

        assert(parseResult.option.value == 18302)
      }

      it("should parse summary in first summary line") {
        val parseResult: ParseResult[String] =
          FileHeader
            .SummaryLine
            .initSummary
            .parseOnly(records)

        assert(parseResult.option.value == "summary1")
      }

      it("should parse summary in second summary line") {
        val parseResult: ParseResult[String] =
          ((FileHeader
            .SummaryLine
            .initSummary ~ Util.eof) ~> FileHeader.SummaryLine.nextSummary)
            .parseOnly(records)

        assert(parseResult.option.value == "summary2")
      }

      it("should parse summary in third summary line") {
        val parseResult: ParseResult[String] =
          ((FileHeader
            .SummaryLine
            .initSummary ~ Util.eof ~ FileHeader.SummaryLine.nextSummary ~ Util.eof) ~> FileHeader.SummaryLine.nextSummary)
            .parseOnly(records)

        assert(parseResult.option.value == "summary3")
      }

    }
  }
}
