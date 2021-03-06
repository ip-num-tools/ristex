package io.geekabyte.ristex.parsers

import atto.Atto.{sepBy, _}
import atto._
import cats.implicits._
import Base._
import HeaderLines.SummaryLine
import Util.{lb, pipe}

object RecordLines {

  private val parseUpUntilRecords: Parser[List[(String, String, Int, String)]] =
    SummaryLine.initParseAll

  private def regParser(statusParser: Parser[String]): Parser[String] = registryParser <~ {
    pipe ~ countryCodeParse ~
      pipe ~ { ipTypeAndStartValueParser } ~
      pipe ~ valueParser ~
      pipe ~ recordDate ~
      pipe ~ statusParser
  }

  private def ccParser(statusParser: Parser[String]): Parser[String] = {
    val cc: Parser[String] = (registryParser ~ pipe) ~> countryCodeParse
    cc <~ pipe ~ { ipTypeAndStartValueParser } ~
      pipe ~ valueParser ~
      pipe ~ recordDate ~
      pipe ~ statusParser
  }

  private def typeParser(statusParser: Parser[String]): Parser[String] = {
    val ipType: Parser[(String, String)] = (registryParser ~ pipe ~ countryCodeParse ~ pipe) ~> ipTypeAndStartValueParser
    (ipType <~ pipe ~ valueParser ~
      pipe ~ recordDate ~
      pipe ~ statusParser).map(_._1)
  }

  private def startParser(statusParser: Parser[String]): Parser[String] = {
    val ipType: Parser[(String, String)] = (registryParser ~ pipe ~ countryCodeParse ~ pipe) ~> ipTypeAndStartValueParser
    (ipType <~ pipe ~ valueParser ~
      pipe ~ recordDate ~
      pipe ~ statusParser).map(_._2)
  }

  private def startValueParser(statusParser: Parser[String]): Parser[Long] = {
    val value: Parser[Long] = registryParser ~ pipe ~ countryCodeParse ~ pipe ~ ipTypeAndStartValueParser ~ pipe ~> valueParser
    value <~ pipe ~ recordDate ~
      pipe ~ statusParser
  }

  private def dateParser(statusParser: Parser[String]): Parser[String] = {
    val date: Parser[String] = registryParser ~ pipe ~ countryCodeParse ~ pipe ~ ipTypeAndStartValueParser ~ pipe ~ valueParser ~ pipe ~> recordDate
    date <~ pipe ~ statusParser
  }

  private def statusParser(statusParser: Parser[String]): Parser[String] = {
    registryParser ~ pipe ~ countryCodeParse ~ pipe ~ ipTypeAndStartValueParser ~ pipe ~
      valueParser ~ pipe ~> recordDate ~ pipe ~> statusParser
  }

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
    *
    * * See https://ftp.ripe.net/pub/stats/ripencc/RIR-Statistics-Exchange-Format.txt for more information on the
    * * format of a RIR statistic exchange file
    */
  object Standard {

    private val lineParser: Parser[(String, String, String, String, Long, String, String)] =
      (registryParser <~ pipe,
        countryCodeParse <~ pipe,
        ipTypeAndStartValueParser <~ pipe, valueParser <~ pipe,
        recordDate <~ pipe,
        standardStatusParser)
      .mapN((reg, cc, iptypeandval, value, date, status) => (reg, cc, iptypeandval._1, iptypeandval._2, value, date, status))

    val initParseAll: Parser[List[(String, String, String, String, Long, String, String)]] = {
      val skippingComments = lineParser <~ (skipMany(lb.map(_.toString)) ~ skipMany(CommentLines.comment))
      parseUpUntilRecords ~> many(skippingComments)
    }

    val initParseFirst: Parser[(String, String, String, String, Long, String, String)] = {
      parseUpUntilRecords ~> lineParser <~ lb
    }

    val firstRegistry: Parser[String] = {
      parseUpUntilRecords ~>  {
        nextRegistry
      }
    }

    val nextRegistry = {
      regParser(standardStatusParser) <~ lb
    }

    val firstCountryCode: Parser[String] = {
      parseUpUntilRecords ~> {
        nextCountryCode
      }
    }

    val nextCountryCode: Parser[String] = {
      ccParser(standardStatusParser) <~ lb
    }

    val firstIPType: Parser[String] = {
      parseUpUntilRecords ~> {
        nextIPType
      }
    }

    val nextIPType = {
      typeParser(standardStatusParser) <~ lb
    }

    val firstStartAddress: Parser[String] = {
      parseUpUntilRecords ~> {
        nextStartAddress
      }
    }

    val nextStartAddress: Parser[String] = {
      startParser(standardStatusParser) <~ lb
    }


    val firstValue: Parser[Long] = {
      parseUpUntilRecords ~> {
        nextValue
      }
    }

    val nextValue: Parser[Long] = {
      startValueParser(standardStatusParser) <~ lb
    }

    val firstDate: Parser[String] = {
      parseUpUntilRecords ~> {
        nextDate
      }
    }

    val nextDate: Parser[String] = {
      dateParser(standardStatusParser) <~ lb
    }

    val firstStatus: Parser[String] = {
      parseUpUntilRecords ~> nextStatus
    }

    val nextStatus: Parser[String] = {
      statusParser(standardStatusParser) <~ lb
    }

  }

  /**
    * Format:
    *
    * registry|cc|type|start|value|date|status|opaque-id[|extensions...]
    *
    * Almost same as standard. Difference includes:
    *
    * status     Type of record from the set:
    *
    * {available, allocated, assigned, reserved}
    *
    * available    The resource has not been allocated
    * or assigned to any entity.
    *
    * allocated    An allocation made by the registry
    * producing the file.
    *
    * assigned     An assignment made by the registry
    * producing the file.
    *
    * reserved     The resource has not been allocated
    * or assigned to any entity, and is
    * not available for allocation or
    * assignment.
    *
    * opaque-id  This is an in-series identifier which uniquely
    * identifies a single organisation, an Internet
    * number resource holder.
    *
    * All records in the file with the same opaque-id
    * are registered to the same resource holder.
    *
    * The opaque-id is not guaranteed to be constant
    * between versions of the file.
    *
    * If the records are collated by type, opaque-id and
    * date, records of the same type for the same opaque-id
    * for the same date can be held to be a single
    * assignment or allocation
    *
    * extensions In future, this may include extra data that
    * is yet to be defined.
    *
    * See https://ftp.ripe.net/pub/stats/ripencc/RIR-Statistics-Exchange-Format.txt for more information on the
    * format of a RIR statistic exchange file
    */
  object Extended {

    private val lineParser: Parser[(String, String, String, String, Long, String, String, String)] = (registryParser <~ pipe,
      countryCodeParse <~ pipe,
      ipTypeAndStartValueParser <~ pipe,
      valueParser <~ pipe,
      recordDate <~ pipe,
      extendedStatusParser  <~ pipe,
      opaqueIdParser <~ manyN(0, lb))
      .mapN((reg, cc, iptypeandval, value, date, status, opaqueId) => (reg, cc, iptypeandval._1, iptypeandval._2, value, date, status, opaqueId))

    val initParseAll: Parser[List[(String, String, String, String, Long, String, String, String)]] = {
      val skippingComments = lineParser <~ many(lb ~ CommentLines.comment)
      parseUpUntilRecords ~> sepBy(skippingComments, lb)
    }

    val initParseFirst = {
      parseUpUntilRecords ~> lineParser <~ lb
    }

    val firstRegistry: Parser[String] = {
      parseUpUntilRecords ~> nextRegistry
    }

    val nextRegistry = {
      regParser(extendedStatusParser) <~ (pipe ~ opaqueIdParser ~ lb)
    }

    val firstCountryCode: Parser[String] = {
      parseUpUntilRecords ~> {
        nextCountryCode
      }
    }

    val nextCountryCode: Parser[String] = {
      ccParser(extendedStatusParser) <~ (pipe ~ opaqueIdParser ~ lb)
    }

    val firstIPType: Parser[String] = {
      parseUpUntilRecords ~> {
        nextIPType
      }
    }

    val nextIPType = {
      typeParser(extendedStatusParser) <~ (pipe ~ opaqueIdParser ~ lb)
    }

    val firstStartAddress: Parser[String] = {
      parseUpUntilRecords ~> {
        nextStartAddress
      }
    }

    val nextStartAddress: Parser[String] = {
      startParser(extendedStatusParser) <~ (pipe ~ opaqueIdParser ~ lb)
    }


    val firstValue: Parser[Long] = {
      parseUpUntilRecords ~> {
        nextValue
      }
    }

    val nextValue: Parser[Long] = {
      startValueParser(extendedStatusParser) <~ (pipe ~ opaqueIdParser ~ lb)
    }

    val firstDate: Parser[String] = {
      parseUpUntilRecords ~> {
        nextDate
      }
    }

    val nextDate: Parser[String] = {
      dateParser(extendedStatusParser) <~ (pipe ~ opaqueIdParser ~ lb)
    }

    val firstStatus: Parser[String] = {
      parseUpUntilRecords ~> {
        nextStatus
      }
    }

    val nextStatus: Parser[String] = {
      statusParser(extendedStatusParser) <~ (pipe ~ opaqueIdParser ~ lb)
    }

    val firstOpaqueId: Parser[String] = {
      parseUpUntilRecords ~> {
        nextOpaqueId
      }
    }

    val nextOpaqueId: Parser[String] = {
      statusParser(extendedStatusParser)
    }
  }

}
