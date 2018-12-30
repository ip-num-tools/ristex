package io.geekabyte.parsers

import atto.Atto._
import atto.{ParseResult, Parser}
import org.scalatest.{FunSpec, OptionValues}

import scala.io.Source

class ExtendedRecordLinesTest extends FunSpec with OptionValues {

  private val extendedRecords: String =
    Source
      .fromResource("sample_extended_records")
      .getLines()
      .toList.mkString("\n")

  describe("Records") {

    describe("Extended") {
      it("should parse registry in record line") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .initRegistry
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "ripencc")
      }

      it("should parse registry in second record line") {
        val initRegistryParser: Parser[String] =
          RecordLines
            .Extended
            .initRegistry

        val nextRegistryParser: Parser[String] =
          RecordLines
            .Extended
            .nextRegistry

        assert((initRegistryParser ~ Util.eol ~> nextRegistryParser).parseOnly(extendedRecords).option.value=="ripencc")
      }

      it("should init parse country code") {

        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .initCountryCode
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "FR")
      }

      it("should parse first country code after init") {

        val initParse: Parser[String] =
          RecordLines
            .Extended
            .initCountryCode

        val parseResult: ParseResult[String] = (initParse ~ Util.eol ~> RecordLines.Extended.nextCountryCode)
          .parseOnly(extendedRecords)

        assert(parseResult.option.value == "EU")
      }

      it("should parse second country code after init") {

        val initParse: Parser[String] =
          RecordLines
            .Extended
            .initCountryCode

        val parseResult: ParseResult[String] = (
          initParse ~ Util.eol ~
            RecordLines.Extended.nextCountryCode ~ Util.eol ~>
            RecordLines.Extended.nextCountryCode
          ).parseOnly(extendedRecords)

        assert(parseResult.option.value == "GB")
      }

      it("should init parse type") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .initType
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "ipv4")
      }

      it("should parse the type after the init parse") {
        val initParse: Parser[String] =
          RecordLines
            .Extended
            .initType

        val parseResult: ParseResult[String] = (initParse ~ Util.eol ~> RecordLines.Extended.nextType)
          .parseOnly(extendedRecords)

        assert(parseResult.option.value == "ipv6")
      }

      it("should init parse start") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .initStart
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "2.0.0.0")
      }

      it("should parse start after init") {
        val initParse: Parser[String] =
          RecordLines
            .Extended
            .initStart

        val parseResult: ParseResult[String] =  (initParse ~ Util.eol ~> RecordLines.Extended.nextStart)
          .parseOnly(extendedRecords)

        assert(parseResult.option.value == "2001:600::")
      }

      it("should parse second start after init") {
        val initParse: Parser[String] =
          RecordLines
            .Extended
            .initStart

        val parseResult: ParseResult[String] =
          (initParse ~ Util.eol ~ RecordLines.Extended.nextStart ~ Util.eol ~> RecordLines.Extended.nextStart)
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "210331")
      }

      it("should init parse value") {
        val parseResult: ParseResult[Long] =
          RecordLines
            .Extended
            .initValue
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == 1048576L)
      }

      it("should parse second value after init") {
        val initParse: Parser[Long] =
          RecordLines
            .Extended
            .initValue

        val parseResult: ParseResult[Long] =
          (initParse ~ Util.eol ~> RecordLines.Extended.nextValue)
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == 32L)
      }

      it("should init parse date") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .initDate
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "20100712")
      }

      it("should parse date after init") {
        val initParse: Parser[String] =
          RecordLines
            .Extended
            .initDate

        val parseResult: ParseResult[String] = (initParse ~ Util.eol ~> RecordLines.Extended.nextDate)
          .parseOnly(extendedRecords)

        assert(parseResult.option.value == "20100910")
      }

      it("should init parse status") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .initStatus
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "allocated")
      }

      it("should parse status after init") {
        val initParse: Parser[String] =
          RecordLines
            .Extended
            .initStatus

        val parseResult: ParseResult[String] =  (initParse ~ Util.eol ~> RecordLines.Extended.nextStatus)
          .parseOnly(extendedRecords)

        assert(parseResult.option.value == "allocated")
      }

    }
  }

}
