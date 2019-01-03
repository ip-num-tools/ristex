package io.geekabyte.ristex.parsers

import atto.Atto._
import atto._
import cats.implicits._
import Util.{fixed, range}

object Base {

  /**
    * parses a date string in the ddmmyyyy format
    */
  val yearMonthDateParser: Parser[String] = {

    def padWithZero(in: String): String = {
      if (in.length == 1) s"0$in" else in
    }

    val yearParser: Parser[String] = {
      count(4, digit).map(_.mkString)
    }

    val monthParser: Parser[String] = {
      fixed(2).flatMap((month: Int) => {
        range(month, 1, 12, s"Month $month, cannot be less than 1 or greater than 12")
      }).map((month: Int) => padWithZero(month.toString))
    }

    val dateParser: Parser[String] = {
      int.flatMap((date: Int) => {
        range(date, 1, 31, s"Date $date cannot be less than 1 or greater than 31")
      }).map((date: Int) => padWithZero(date.toString))
    }

    (yearParser, monthParser, dateParser).mapN(_ ++ _ ++ _).map(_.mkString(""))
  }

  /**
    * parses the version of the RIR statistic exchange files as a double
    */
  val versionParser : Parser[Double] = double

  /**
    * parses the rir. valid values include:
    * afrinic, apnic, arin, iana, lacnic, ripencc
    */
  val registryParser: Parser[String] = (
      string("afrinic")   |
        string("apnic")   |
        string("arin")    |
        string("iana")    |
        string("lacnic")  |
        string("ripencc")
      ).map(identity)


  /**
    * parses the type of ip resource. valid values include:
    * asn, ipv4, ipv6
    */
  val ipTypeParser: Parser[String] =
    (string("asn") | string("ipv4") | string("ipv6")).map(identity)


  /**
    * parses the serial number of the  RIR statistic exchange file
    */
  val serialNumberParser: Parser[Int] = int


  /**
    * parses the number of records in file, excluding blank lines,
    * summary lines, the version line and comments;
    */
  val recordCountParser: Parser[Int] = int

  /**
    * start date of time period, in yyyymmdd format
    */
  val startDateParser: Parser[String] = yearMonthDateParser
  /**
    * end date of period, in yyyymmdd format
    */
  val endDateParser: Parser[String] = yearMonthDateParser

  /**
    * parses the Date on this allocation/assignment was made by the
    * RIR in the format YYYYMMDD;
    *
    * Where the allocation or assignment has been
    * transferred from another registry, this date
    * represents the date of first assignment or allocation
    * as received in from the original RIR.
    *
    * It is noted that where records do not show a date of
    * first assignment, this can take the 00000000 value.
    */
  val recordDate: Parser[String] = yearMonthDateParser | string("00000000")


  /**
    * parses the ASCII string 'summary' (to distinguish the record line)
    */
  val summaryParser: Parser[String] = string("summary")


  /**
    * parse the offset from UTC of local RIR producing file,
    * in +/- HHMM format
    */
  val UTCoffsetParser: Parser[String] = {
    val prefix: Parser[Char] = char('+') | char('-')
    val hours: Parser[List[Char]] = count(4, digit)
    val value: Parser[(Char, List[Char])] = prefix ~ hours
    value.map { case (a, b) => (a +: b).mkString("") }
  }

  /**
    * parses the ISO 3166 2-letter country code, and the enumerated
    * variances of
    *
    * {AP,EU,UK}
    *
    * These values are not defined in ISO 3166 but are widely used.
    */
  val countryCodeParse: Parser[String] = {
    manyN(2, letter).map(_.mkString)
  }

  /**
    * parses an asn value
    */
  val asnParser: Parser[String] = {
    long.filter(asn => {
      asn >= 0 && asn <= "4294967296".toLong
    }).map(_.toString())
  }

  /**
    * parses an ipv4 value
    */
  val ipv4Parser: Parser[String] = {

    val octetParser: Parser[Int] =
      int
        .filter(n => n >= 0 && n <= 255)

    val dotParser: Parser[Char] = char('.')

    (octetParser <~ dotParser,
      octetParser <~ dotParser,
      octetParser <~ dotParser, octetParser).mapN((a, b, c, d) => s"$a.$b.$c.$d")
  }

  /**
    * parses an ipv6 value
    */
  val ipv6Parser: Parser[String] = {
    val hexParser: Parser[String] = {
      manyN(4, hexDigit).map(_.mkString) |
      manyN(3, hexDigit).map(_.mkString) |
      manyN(2, hexDigit).map(_.mkString) |
      manyN(1, hexDigit).map(_.mkString)
    }
    val colonParser: Parser[String] = string(":")
    val doubleColonParser: Parser[String] = string("::")

    def recursive: Parser[String] = {
      for {
        first <- doubleColonParser | hexParser | colonParser
        rest <- {
          if (":".equals(first) || "::".equals(first)) {
            hexParser
          } else {
            recursive
          }
        }
      } yield first + rest
    }

    (many(recursive).map(_.mkString), many(string("::")).map(_.mkString)).mapN((a,b) => a + b)
  }

  /**
    * parses some value quantity associated with either an asn, ipv4 or ipv6
    *
    * In the case of IPv4 address the count of hosts for
    * this range is the value. This count does not have to represent a
    * CIDR range.
    *
    * In the case of an IPv6 address the value will be
    * the CIDR prefix length from the 'first address'
    * value of <start>.
    * In the case of records of type 'asn' the number is
    * the count of AS from this start value.
    */
  val valueParser: Parser[Long] = {
    long.filter(value => {
      value >= 0 && value <= Math.pow(2, 32).toLong
    })
  }

  /**
    * parses all the possible status available for a resource
    */
  val statusParser: Parser[String] = {
    string("available")   |
    string("allocated")   |
    string("assigned")    |
    string("reserved")
  }

  /**
    * parses the type of allocation from the set:
    *
    * {allocated, assigned}
    *
    * This is the allocation or assignment made by the
    * registry producing the file and not any sub-assignment
    * by other agencies.
    */
  val standardStatusParser: Parser[String] = {
    string("allocated") | string("assigned")
  }

  /**
    * Type of allocation from the set:
    *
    * {available, allocated, assigned, reserved}
    *
    * This is the allocation or assignment made by the
    * registry producing the extended file and not any sub-assignment
    * by other agencies.
    */
  val extendedStatusParser: Parser[String] = statusParser

  /**
    * parses the resource type (asn, ipv4, ipv6) and the start value as a tuple
    *
    * For the start value, In the case of records of type 'ipv4' or 'ipv6'
    * this is the IPv4 or IPv6 'first address' of the range.
    *
    * In the case of an 16 bit AS number  the format is
    * the integer value in the range 0 to 65535, in the
    * case of a 32 bit ASN the value is in the range 0
    * to 4294967296. No distinction is drawn between 16
    * and 32 bit ASN values in the range 0 to 65535.
    */
  val ipTypeAndStartValueParser: Parser[(String, String)] = for {
    ipType <- ipTypeParser <~ char('|')
    startValue <- if ("asn".equalsIgnoreCase(ipType)) {
      asnParser
    } else if ("ipv4".equalsIgnoreCase(ipType)) {
      ipv4Parser
    } else {
      ipv6Parser
    }
  } yield (ipType, startValue)

  val opaqueIdParser: Parser[String] = {
    many(char('-') | letterOrDigit).map(_.mkString)
  }

}
