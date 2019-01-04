package io.geekabyte.parsers

import atto.Atto._
import atto.{ParseResult, Parser}
import io.geekabyte.ristex.parsers.HeaderLines.SummaryLine
import io.geekabyte.ristex.parsers.{RecordLines, Util}
import org.scalatest.{FunSpec, OptionValues}
import shapeless.PolyDefns.~>

import scala.io.Source

class StandardRecordLinesTest extends FunSpec with OptionValues {
  private val standardRecords: String =
    Source
      .fromResource("sample_standard_records")
      .getLines()
      .toList.mkString("\n")


  private val standardAllRecords: String =
    Source
      .fromResource("sample_standard_all_records")
      .getLines()
      .toList.mkString("\n")

  describe("Records") {

    describe("Standard") {
      it("should parse registry in record line") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .firstRegistry
            .parseOnly(standardRecords)

        assert(parseResult.option.value == "ripencc")
      }

      it("should parse registry in second record line") {
        val initRegistryParser: Parser[String] =
          RecordLines
            .Standard
            .firstRegistry

        val nextRegistryParser: Parser[String] =
          RecordLines
            .Standard
            .nextRegistry

        assert((initRegistryParser ~> nextRegistryParser).parseOnly(standardRecords).option.value == "ripencc")
      }

      it("should init parse country code") {

        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .firstCountryCode
            .parseOnly(standardRecords)

        assert(parseResult.option.value == "FR")
      }

      it("should parse first country code after init") {

        val initParse: Parser[String] =
          RecordLines
            .Standard
            .firstCountryCode

        val parseResult: ParseResult[String] = (initParse ~> RecordLines.Standard.nextCountryCode)
            .parseOnly(standardRecords)

        assert(parseResult.option.value == "EU")
      }

      it("should parse second country code after init") {

        val initParse: Parser[String] =
          RecordLines
            .Standard
            .firstCountryCode

        val parseResult: ParseResult[String] = (
          initParse ~ RecordLines.Standard.nextCountryCode ~>
            RecordLines.Standard.nextCountryCode
          ).parseOnly(standardRecords)

        assert(parseResult.option.value == "GB")
      }

      it("should init parse type") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .firstIPType
            .parseOnly(standardRecords)

        assert(parseResult.option.value == "ipv4")
      }

      it("should parse the type after the init parse") {
        val initParse: Parser[String] =
          RecordLines
            .Standard
            .firstIPType

        val parseResult: ParseResult[String] = (initParse ~> RecordLines.Standard.nextIPType)
          .parseOnly(standardRecords)

        assert(parseResult.option.value == "ipv6")
      }

      it("should init parse start") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .firstStartAddress
            .parseOnly(standardRecords)

        assert(parseResult.option.value == "2.0.0.0")
      }

      it("should parse start after init") {
        val initParse: Parser[String] =
          RecordLines
            .Standard
            .firstStartAddress

        val parseResult: ParseResult[String] =  (initParse ~> RecordLines.Standard.nextStartAddress)
          .parseOnly(standardRecords)

        assert(parseResult.option.value == "2001:600::")
      }

      it("should parse second start after init") {
        val initParse: Parser[String] =
          RecordLines
            .Standard
            .firstStartAddress

        val parseResult: ParseResult[String] =
          (initParse ~ RecordLines.Standard.nextStartAddress ~> RecordLines.Standard.nextStartAddress)
          .parseOnly(standardRecords)

        assert(parseResult.option.value == "210331")
      }

      it("should init parse value") {
        val parseResult: ParseResult[Long] =
          RecordLines
            .Standard
            .firstValue
            .parseOnly(standardRecords)

        assert(parseResult.option.value == 1048576L)
      }

      it("should parse second value after init") {
        val initParse: Parser[Long] =
          RecordLines
            .Standard
            .firstValue

        val parseResult: ParseResult[Long] =
          (initParse ~> RecordLines.Standard.nextValue)
          .parseOnly(standardRecords)

        assert(parseResult.option.value == 32L)
      }

      it("should init parse date") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .firstDate
            .parseOnly(standardRecords)

        assert(parseResult.option.value == "20100712")
      }

      it("should parse date after init") {
        val initParse: Parser[String] =
          RecordLines
            .Standard
            .firstDate

        val parseResult: ParseResult[String] = (initParse ~> RecordLines.Standard.nextDate)
          .parseOnly(standardRecords)

        assert(parseResult.option.value == "19990826")
      }

      it("should init parse status") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .firstStatus
            .parseOnly(standardRecords)

        assert(parseResult.option.value == "allocated")
      }

      it("should parse status after first status") {
        val initParse: Parser[String] =
          RecordLines
            .Standard
            .firstStatus

        val parseResult: ParseResult[String] =  (initParse ~> RecordLines.Standard.nextStatus)
          .parseOnly(standardRecords)

        assert(parseResult.option.value == "allocated")
      }

      it("should init parse all") {
        val initParse: Parser[List[(String, String, String, String, Long, String, String)]] =
          RecordLines
            .Standard
            .initParseAll

        val parseResult: ParseResult[List[(String, String, String, String, Long, String, String)]] =  initParse
          .parseOnly(standardAllRecords)

        assert {
          parseResult.option.value == List(
            ("ripencc","FR","ipv4","2.0.0.0",1048576l,"20100712","allocated"),
            ("ripencc","EU","ipv6","2001:600::",32l,"19990826", "allocated"),
            ("ripencc","GB","asn","210331",1l,"20180821","assigned")
          )
        }
      }

      it("should init parse first line") {
        val initParse: Parser[(String, String, String, String, Long, String, String)] =
          RecordLines
            .Standard
            .initParseFirst

        val parseResult: ParseResult[(String, String, String, String, Long, String, String)] =  initParse
          .parseOnly(standardAllRecords)

        assert {
          parseResult.option.value == ("ripencc","FR","ipv4","2.0.0.0",1048576l,"20100712","allocated")
        }
      }
    }
  }

}
