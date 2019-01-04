package io.geekabyte.parsers

import atto.Atto._
import atto.{ParseResult, Parser}
import io.geekabyte.ristex.parsers.{RecordLines, Util}
import org.scalatest.{FunSpec, OptionValues}

import scala.io.Source

class ExtendedRecordLinesTest extends FunSpec with OptionValues {

  private val extendedRecords: String =
    Source
      .fromResource("sample_extended_records")
      .getLines()
      .toList.mkString("\n")


  private val extendedAllRecords: String =
    Source
      .fromResource("sample_extended_all_records")
      .getLines()
      .toList.mkString("\n")

  describe("Records") {

    describe("Extended") {
      it("should parse registry in record line") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .firstRegistry
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "ripencc")
      }

      it("should parse registry in second record line") {
        val initRegistryParser: Parser[String] =
          RecordLines
            .Extended
            .firstRegistry

        val nextRegistryParser: Parser[String] =
          RecordLines
            .Extended
            .nextRegistry

        assert((initRegistryParser ~> nextRegistryParser).parseOnly(extendedRecords).option.value=="ripencc")
      }

      it("should init parse country code") {

        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .firstCountryCode
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "FR")
      }

      it("should parse first country code after init") {

        val initParse: Parser[String] =
          RecordLines
            .Extended
            .firstCountryCode

        val parseResult: ParseResult[String] = (initParse ~> RecordLines.Extended.nextCountryCode)
          .parseOnly(extendedRecords)

        assert(parseResult.option.value == "EU")
      }

      it("should parse second country code after init") {

        val initParse: Parser[String] =
          RecordLines
            .Extended
            .firstCountryCode

        val parseResult: ParseResult[String] = (
          initParse ~ RecordLines.Extended.nextCountryCode ~> RecordLines.Extended.nextCountryCode)
          .parseOnly(extendedRecords)

        assert(parseResult.option.value == "GB")
      }

      it("should init parse type") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .firstIPType
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "ipv4")
      }

      it("should parse the type after the init parse") {
        val initParse: Parser[String] =
          RecordLines
            .Extended
            .firstIPType

        val parseResult: ParseResult[String] = (initParse ~> RecordLines.Extended.nextIPType)
          .parseOnly(extendedRecords)

        assert(parseResult.option.value == "ipv6")
      }

      it("should init parse start") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .firstStartAddress
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "2.0.0.0")
      }

      it("should parse start after init") {
        val initParse: Parser[String] =
          RecordLines
            .Extended
            .firstStartAddress

        val parseResult: ParseResult[String] =  (initParse ~> RecordLines.Extended.nextStartAddress)
          .parseOnly(extendedRecords)

        assert(parseResult.option.value == "2001:600::")
      }

      it("should parse second start after init") {
        val initParse: Parser[String] =
          RecordLines
            .Extended
            .firstStartAddress

        val parseResult: ParseResult[String] =
          (initParse ~ RecordLines.Extended.nextStartAddress ~> RecordLines.Extended.nextStartAddress)
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "210331")
      }

      it("should init parse value") {
        val parseResult: ParseResult[Long] =
          RecordLines
            .Extended
            .firstValue
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == 1048576L)
      }

      it("should parse second value after init") {
        val initParse: Parser[Long] =
          RecordLines
            .Extended
            .firstValue

        val parseResult: ParseResult[Long] =
          (initParse ~> RecordLines.Extended.nextValue)
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == 32L)
      }

      it("should init parse date") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .firstDate
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "20100712")
      }

      it("should parse date after init") {
        val initParse: Parser[String] =
          RecordLines
            .Extended
            .firstDate

        val parseResult: ParseResult[String] = (initParse ~> RecordLines.Extended.nextDate)
          .parseOnly(extendedRecords)

        assert(parseResult.option.value == "20100910")
      }

      it("should init parse status") {
        val parseResult: ParseResult[String] =
          RecordLines
            .Extended
            .firstStatus
            .parseOnly(extendedRecords)

        assert(parseResult.option.value == "allocated")
      }

      it("should parse status after init") {
        val initParse: Parser[String] =
          RecordLines
            .Extended
            .firstStatus

        val parseResult: ParseResult[String] =  (initParse ~> RecordLines.Extended.nextStatus)
          .parseOnly(extendedRecords)

        assert(parseResult.option.value == "allocated")
      }

      it("should init parse all") {
        val initParse: Parser[List[(String, String, String, String, Long, String, String, String)]] =
          RecordLines
            .Extended
            .initParseAll

        val parseResult: ParseResult[List[(String, String, String, String, Long, String, String, String)]] = initParse
          .parseOnly(extendedAllRecords)

        assert {
          parseResult.option.value == List(("ripencc","FR","ipv4","2.0.0.0",1048576L,"20100712","allocated","ddca1da8-afb0-4c30-be7d-ca266853c8a3"),
          ("ripencc","EU","ipv6","2001:600::",32L,"20100910","allocated","194d6708-08df-4642-bdb8-c912d2b8582a"),
          ("ripencc","GB","asn","210331",1L,"20100921","allocated","6ef20ee1-bac9-429a-ba27-cf7b67ebb5ea"))
        }
      }

      it("should init parse first line") {
        val initParse: Parser[(String, String, String, String, Long, String, String, String)] =
          RecordLines
            .Extended
            .initParseFirst

        val parseResult: ParseResult[(String, String, String, String, Long, String, String, String)] = initParse
          .parseOnly(extendedAllRecords)

        assert {
          parseResult.option.value == ("ripencc","FR","ipv4","2.0.0.0",1048576L,"20100712","allocated","ddca1da8-afb0-4c30-be7d-ca266853c8a3")
        }
      }

    }
  }

}
