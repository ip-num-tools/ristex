package io.geekabyte.parsers

import atto.Atto._
import atto._
import io.geekabyte.ristex.parsers.HeaderLines.{SummaryLine, VersionLine}
import io.geekabyte.ristex.parsers.{HeaderLines, Util}
import org.scalatest.{FunSpec, OptionValues}
import shapeless.PolyDefns.~>

import scala.io.Source

class HeaderLinesTest extends  FunSpec with OptionValues {

  private val records: String =
    Source
      .fromResource("sample_standard_records")
      .getLines()
      .toList.mkString("\n")

  describe("FileHeader") {
    describe("VersionLine") {

      it("should initParse") {
        val parseResult: ParseResult[(Double, String, Int, Int, String, String, String)] =
          HeaderLines
            .VersionLine
            .initParse
            .parseOnly(records)

        assert(parseResult.option.value == (2,"ripencc",1544569199,123397,"19830705","20181211","+0100"))
      }

      it("should parse version") {
        val parseResult: ParseResult[Double] =
          HeaderLines
            .VersionLine
            .initVersion
            .parseOnly(records)

        assert(parseResult.option.value == 2)
      }

      it("should parse registry") {
        val parseResult: ParseResult[String] =
          HeaderLines
            .VersionLine
            .initRegistry
            .parseOnly(records)

        assert(parseResult.option.value == "ripencc")
      }

      it("should parse serial") {
        val parseResult: ParseResult[Int] =
          HeaderLines
            .VersionLine
            .initSerial
            .parseOnly(records)

        assert(parseResult.option.value == 1544569199)
      }

      it("should parse records") {
        val parseResult: ParseResult[Int] =
          HeaderLines
            .VersionLine
            .initRecords
            .parseOnly(records)

        assert(parseResult.option.value == 123397)
      }

      it("should parse start date") {
        val parseResult: ParseResult[String] =
          HeaderLines
            .VersionLine
            .initStartDate
            .parseOnly(records)

        assert(parseResult.option.value == "19830705")
      }

      it("should parse end date") {
        val parseResult: ParseResult[String] =
          HeaderLines
            .VersionLine
            .initEndDate
            .parseOnly(records)

        assert(parseResult.option.value == "20181211")
      }

      it("should parse utc") {
        val parseResult: ParseResult[String] =
          HeaderLines
            .VersionLine
            .initUTCoffset
            .parseOnly(records)

        assert(parseResult.option.value == "+0100")
      }
    }
    describe("SummaryLine") {

      it("should initParse all summary lines") {
        val parseResult: ParseResult[List[(String, String, Int, String)]] =
          HeaderLines
            .SummaryLine
            .initParseAll
            .parseOnly(records)

        assert(parseResult.option.value == List(
          ("ripencc","ipv4",71111,"summary"),
          ("ripencc","asn",33984,"summary"),
          ("ripencc","ipv6",18302,"summary"))
        )
      }

      it("should init parse with first summary line") {
        val parseResult: ParseResult[(String, String, Int, String)] =
          HeaderLines
            .SummaryLine
            .initParseFirst
            .parseOnly(records)

        assert(parseResult.option.value == ("ripencc","ipv4",71111,"summary"))
      }

      it("should parse all summary lines") {
        val headerVersionLine: Parser[((Double, String, Int, Int, String, String, String), Char)] =
          VersionLine.initParse ~ char('\n')

        val parseResult: ParseResult[List[(String, String, Int, String)]] =
          (headerVersionLine ~> HeaderLines
            .SummaryLine
            .all)
            .parseOnly(records)

        assert(parseResult.option.value == List(
          ("ripencc","ipv4",71111,"summary"),
          ("ripencc","asn",33984,"summary"),
          ("ripencc","ipv6",18302,"summary"))
        )
      }

      it("should parse first summary line") {
        val headerVersionLine: Parser[((Double, String, Int, Int, String, String, String), Char)] =
          VersionLine.initParse ~ char('\n')

        val summaryLineParser: Parser[(String, String, Int, String)] = HeaderLines
          .SummaryLine
          .nextLine


        val parseResult: ParseResult[(String, String, Int, String)] =
          (headerVersionLine ~> summaryLineParser)
            .parseOnly(records)

        assert(parseResult.option.value == ("ripencc","ipv4",71111,"summary"))
      }

      it("should parse second summary line") {
        val headerVersionLine: Parser[((Double, String, Int, Int, String, String, String), Char)] =
          VersionLine.initParse ~ char('\n')

        val summaryLineParser: Parser[(String, String, Int, String)] = HeaderLines
          .SummaryLine
          .nextLine

        val parseResult: ParseResult[(String, String, Int, String)] =
          ((headerVersionLine ~ summaryLineParser) ~> summaryLineParser)
            .parseOnly(records)

        assert(parseResult.option.value == ("ripencc","asn",33984,"summary"))
      }

      it("should parse third summary line") {
        val headerVersionLine: Parser[((Double, String, Int, Int, String, String, String), Char)] =
          VersionLine.initParse ~ char('\n')

        val summaryLineParser: Parser[(String, String, Int, String)] = HeaderLines
          .SummaryLine
          .nextLine

        val parseResult: ParseResult[(String, String, Int, String)] =
          ((headerVersionLine ~ summaryLineParser ~ summaryLineParser) ~> summaryLineParser)
            .parseOnly(records)

        assert(parseResult.option.value == ("ripencc","ipv6",18302,"summary"))
      }

      it("should parse registry in first summary line") {
        val parseResult: ParseResult[String] =
          HeaderLines
            .SummaryLine
            .firstRegistry
            .parseOnly(records)
        assert(parseResult.option.value == "ripencc")
      }

      it("should parse registry in second summary line") {
        val parseResult: ParseResult[String] =
          ((HeaderLines
            .SummaryLine
            .firstRegistry) ~> HeaderLines.SummaryLine.nextRegistry // second line
            )
            .parseOnly(records)

        assert(parseResult.option.value == "ripencc")
      }

      it("should parse registry in third summary line") {
        val parseResult: ParseResult[String] =
          ((HeaderLines
            .SummaryLine
            .firstRegistry ~ HeaderLines.SummaryLine.nextRegistry) ~>
            HeaderLines.SummaryLine.nextRegistry // third line
            ).parseOnly(records)

        assert(parseResult.option.value == "ripencc")
      }

      it("should parse type in first summary line") {
        val parseResult: ParseResult[String] =
          HeaderLines
            .SummaryLine
            .firstIPType
            .parseOnly(records)
        
        assert(parseResult.option.value == "ipv4")
      }

      it("should parse type in second summary line") {
        val parseResult: ParseResult[String] =
          ((HeaderLines
            .SummaryLine
            .firstIPType) ~> HeaderLines.SummaryLine.nextType)
            .parseOnly(records)
        
        assert(parseResult.option.value == "asn")
      }

      it("should parse type in third summary line") {
        val parseResult: ParseResult[String] =
          ((HeaderLines
            .SummaryLine
            .firstIPType ~ HeaderLines.SummaryLine.nextType) ~> HeaderLines.SummaryLine.nextType)
            .parseOnly(records)
        
        assert(parseResult.option.value == "ipv6")
      }

      it("should parse count in first summary line") {
        val parseResult: ParseResult[Int] =
          HeaderLines
            .SummaryLine
            .firstCount
            .parseOnly(records)
        
        assert(parseResult.option.value == 71111)
      }

      it("should parse count in second summary line") {
        val parseResult: ParseResult[Int] =
          ((HeaderLines
            .SummaryLine
            .firstCount) ~> HeaderLines.SummaryLine.nextCount)
            .parseOnly(records)
        
        assert(parseResult.option.value == 33984)
      }

      it("should parse count in third summary line") {
        val parseResult: ParseResult[Int] =
          ((HeaderLines
            .SummaryLine
            .firstCount ~ HeaderLines.SummaryLine.nextCount) ~> HeaderLines.SummaryLine.nextCount)
            .parseOnly(records)

        assert(parseResult.option.value == 18302)
      }

      it("should parse summary in first summary line") {
        val parseResult: ParseResult[String] =
          HeaderLines
            .SummaryLine
            .firstSummaryTag
            .parseOnly(records)

        assert(parseResult.option.value == "summary")
      }

      it("should parse summary in second summary line") {
        val parseResult: ParseResult[String] =
          ((HeaderLines
            .SummaryLine
            .firstSummaryTag) ~> HeaderLines.SummaryLine.nextSummaryTag)
            .parseOnly(records)

        assert(parseResult.option.value == "summary")
      }

      it("should parse summary in third summary line") {
        val parseResult: ParseResult[String] =
          ((HeaderLines
            .SummaryLine
            .firstSummaryTag ~ HeaderLines.SummaryLine.nextSummaryTag) ~> HeaderLines.SummaryLine.nextSummaryTag)
            .parseOnly(records)

        assert(parseResult.option.value == "summary")
      }

    }
  }
}
