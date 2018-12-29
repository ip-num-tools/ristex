package io.geekabyte.parsers

import atto.Atto._
import atto.{ParseResult, Parser}
import org.scalatest.{FunSpec, OptionValues}

import scala.io.Source

class RecordLinesTest extends FunSpec with OptionValues {
  private val records: String =
    Source
      .fromResource("records_short")
      .getLines()
      .toList.mkString("\n")

  describe("Records") {

    describe("Standard") {
      it("should parse registry in record line") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .initRegistry
            .parseOnly(records)

        assert(parseResult.option.value == "ripencc")
      }

      it("should parse registry in second record line") {
        val initRegistryParser: Parser[String] =
          RecordLines
            .Standard
            .initRegistry

        val nextRegistryParser: Parser[String] =
          RecordLines
            .Standard
            .nextRegistry

        assert((initRegistryParser ~ Util.eof ~> nextRegistryParser).parseOnly(records).option.value == "ripencc")
      }

      it("should init parse country code") {

        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .initCountryCode
            .parseOnly(records)

        assert(parseResult.option.value == "FR")
      }

      it("should parse first country code after init") {

        val initParse: Parser[String] =
          RecordLines
            .Standard
            .initCountryCode

        val parseResult: ParseResult[String] = (initParse ~ Util.eof ~> RecordLines.Standard.nextCountryCode)
            .parseOnly(records)

        assert(parseResult.option.value == "EU")
      }

      it("should parse second country code after init") {

        val initParse: Parser[String] =
          RecordLines
            .Standard
            .initCountryCode

        val parseResult: ParseResult[String] = (
          initParse ~ Util.eof ~
          RecordLines.Standard.nextCountryCode ~ Util.eof ~>
            RecordLines.Standard.nextCountryCode
          ).parseOnly(records)

        assert(parseResult.option.value == "GB")
      }

      it("should init parse type") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .initType
            .parseOnly(records)

        assert(parseResult.option.value == "ipv4")
      }

      it("should parse the type after the init parse") {
        val initParse: Parser[String] =
          RecordLines
            .Standard
            .initType

        val parseResult: ParseResult[String] = (initParse ~ Util.eof ~> RecordLines.Standard.nextType)
          .parseOnly(records)

        assert(parseResult.option.value == "ipv6")
      }

      it("should init parse start") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .initStart
            .parseOnly(records)

        assert(parseResult.option.value == "2.0.0.0")
      }

      it("should parse start after init") {
        val initParse: Parser[String] =
          RecordLines
            .Standard
            .initStart

        val parseResult: ParseResult[String] =  (initParse ~ Util.eof ~> RecordLines.Standard.nextStart)
          .parseOnly(records)

        assert(parseResult.option.value == "2001:600::")
      }

      it("should parse second start after init") {
        val initParse: Parser[String] =
          RecordLines
            .Standard
            .initStart

        val parseResult: ParseResult[String] =
          (initParse ~ Util.eof ~ RecordLines.Standard.nextStart ~ Util.eof ~> RecordLines.Standard.nextStart)
          .parseOnly(records)

        assert(parseResult.option.value == "210331")
      }

      it("should init parse value") {
        val parseResult: ParseResult[Long] =
          RecordLines
            .Standard
            .initValue
            .parseOnly(records)

        assert(parseResult.option.value == 1048576L)
      }

      it("should parse second value after init") {
        val initParse: Parser[Long] =
          RecordLines
            .Standard
            .initValue

        val parseResult: ParseResult[Long] =
          (initParse ~ Util.eof ~> RecordLines.Standard.nextValue)
          .parseOnly(records)

        assert(parseResult.option.value == 32L)
      }

      it("should init parse date") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .initDate
            .parseOnly(records)

        assert(parseResult.option.value == "20100712")
      }

      it("should parse date after init") {
        val initParse: Parser[String] =
          RecordLines
            .Standard
            .initDate

        val parseResult: ParseResult[String] = (initParse ~ Util.eof ~> RecordLines.Standard.nextDate)
          .parseOnly(records)

        assert(parseResult.option.value == "19990826")
      }

      it("should init parse status") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Standard
            .initStatus
            .parseOnly(records)

        assert(parseResult.option.value == "allocated")
      }

      it("should parse status after init") {
        val initParse: Parser[String] =
          RecordLines
            .Standard
            .initStatus


        val parseResult: ParseResult[String] =  (initParse ~ Util.eof ~> RecordLines.Standard.nextStatus)
          .parseOnly(records)

        assert(parseResult.option.value == "assigned")
      }

    }
  }

}
