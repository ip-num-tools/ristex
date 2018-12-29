package io.geekabyte.parsers

import atto.Atto._
import atto._
import io.geekabyte.parsers.Base._
import io.geekabyte.parsers.HeaderLines.SummaryLine
import Util.pipe

object RecordLines {

  /**
    * Format:
    *
    * registry|cc|type|start|value|date|status
    *
    * registry  = One value from the set of defined strings:
    *
    * {apnic,arin,iana,lacnic,ripencc};
    *
    * cc        = ISO 3166 2-letter country code, and the enumerated
    * variances of
    *
    * {AP,EU,UK}
    *
    * These values are not defined in ISO 3166 but are widely used.
    *
    * The cc value identifies the country. However, it is not specified
    * if this is the country where the addresses are used.
    * There are no rules defined for this value.
    * It therefore cannot be used in any reliable way to map IP addresses
    * to countries
    *
    * type      = Type of Internet number resource represented in this record,
    * One value from the set of defined strings:
    *
    * {asn,ipv4,ipv6}
    *
    * start     = In the case of records of type 'ipv4' or 'ipv6'
    * this is the IPv4 or IPv6 'first address' of the range.
    *
    * In the case of an 16 bit AS number  the format is
    * the integer value in the range 0 to 65535, in the
    * case of a 32 bit ASN the value is in the range 0
    * to 4294967296. No distinction is drawn between 16
    * and 32 bit ASN values in the range 0 to 65535.
    *
    * value     = In the case of IPv4 address the count of hosts for
    * this range. This count does not have to represent a
    * CIDR range.
    *
    * In the case of an IPv6 address the value will be
    * the CIDR prefix length from the 'first address'
    * value of <start>.
    * In the case of records of type 'asn' the number is
    * the count of AS from this start value.
    *
    * date      = Date on this allocation/assignment was made by the
    * RIR in the format YYYYMMDD;
    *
    * Where the allocation or assignment has been
    * transferred from another registry, this date
    * represents the date of first assignment or allocation
    * as received in from the original RIR.
    *
    * It is noted that where records do not show a date of
    * first assignment, this can take the 00000000 value.
    *
    * status    = Type of allocation from the set:
    *
    * {allocated, assigned}
    *
    * This is the allocation or assignment made by the
    * registry producing the file and not any sub-assignment
    * by other agencies.
    */
  object Standard {

    private val parseUpUntilRecords: Parser[(List[(String, String, Int, String)], Char)] = SummaryLine.initAll ~ Util.eof

    val initRegistry: Parser[String] = {
      parseUpUntilRecords ~>  {
        nextRegistry
      }
    }

    val nextRegistry = {
      registryParser <~ {
        pipe ~ countryCodeParse ~
          pipe ~ { ipTypeAndStartValueParser } ~
          pipe ~ valueParser ~
          pipe ~ recordDate ~
          pipe ~ standardStatusParser
      }
    }

    val initCountryCode: Parser[String] = {
      parseUpUntilRecords ~> {
        nextCountryCode
      }
    }

    val nextCountryCode: Parser[String] = {
      val cc: Parser[String] = (registryParser ~ pipe) ~> countryCodeParse
      cc <~ pipe ~ { ipTypeAndStartValueParser } ~
        pipe ~ valueParser ~
        pipe ~ recordDate ~
        pipe ~ standardStatusParser
    }

    val initType: Parser[String] = {
      parseUpUntilRecords ~> {
        nextType
      }
    }

    val nextType = {
      val ipType: Parser[(String, String)] = (registryParser ~ pipe ~ countryCodeParse ~ pipe) ~> ipTypeAndStartValueParser
      (ipType <~ pipe ~ valueParser ~
        pipe ~ recordDate ~
        pipe ~ standardStatusParser).map(_._1)
    }

    val initStart: Parser[String] = {
      parseUpUntilRecords ~> {
        nextStart
      }
    }

    val nextStart: Parser[String] = {
      val ipType: Parser[(String, String)] = (registryParser ~ pipe ~ countryCodeParse ~ pipe) ~> ipTypeAndStartValueParser
      (ipType <~ pipe ~ valueParser ~
        pipe ~ recordDate ~
        pipe ~ standardStatusParser).map(_._2)
    }


    val initValue: Parser[Long] = {
      parseUpUntilRecords ~> {
        nextValue
      }
    }

    val nextValue: Parser[Long] = {
      val value: Parser[Long] = registryParser ~ pipe ~ countryCodeParse ~ pipe ~ ipTypeAndStartValueParser ~ pipe ~> valueParser
      value <~ pipe ~ recordDate ~
        pipe ~ standardStatusParser
    }


    val initDate: Parser[String] = {
      parseUpUntilRecords ~> {
        nextDate
      }
    }

    val nextDate: Parser[String] = {
      val date: Parser[String] = registryParser ~ pipe ~ countryCodeParse ~ pipe ~ ipTypeAndStartValueParser ~ pipe ~ valueParser ~ pipe ~> recordDate
      date <~ pipe ~ standardStatusParser
    }

    val initStatus: Parser[String] = {
      parseUpUntilRecords ~> {
        nextStatus
      }
    }

    val nextStatus: Parser[String] = {
      registryParser ~ pipe ~ countryCodeParse ~ pipe ~ ipTypeAndStartValueParser ~ pipe ~
        valueParser ~ pipe ~> recordDate ~ pipe ~> standardStatusParser
    }

  }

  object Extended {

  }

}
