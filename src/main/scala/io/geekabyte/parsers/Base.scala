package io.geekabyte.parsers

import atto.Atto._
import atto._
import cats.implicits._
import Util.{anyStringOrDigit, fixed, range}

object Base {

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

  def versionParser : Parser[Double] = double

  val registryParser: Parser[String] = (
      string("afrinic")   |
        string("apnic")   |
        string("arin")    |
        string("iana")    |
        string("lacnic")  |
        string("ripencc")
      ).map(identity)


  val ipTypeParser: Parser[String] =
    (string("asn") | string("ipv4") | string("ipv6")).map(identity)


  val serialNumberParser: Parser[Int] = int


  val recordCountParser: Parser[Int] = int


  val startDateParser: Parser[String] = yearMonthDateParser
  val endDateParser: Parser[String] = yearMonthDateParser
  val recordDate: Parser[String] = yearMonthDateParser


  val summaryParser: Parser[String] = anyStringOrDigit


  val UTCoffsetParser: Parser[String] = {
    val prefix: Parser[Char] = char('+') | char('-')
    val hours: Parser[List[Char]] = count(4, digit)
    val value: Parser[(Char, List[Char])] = prefix ~ hours
    value.map { case (a, b) => (a +: b).mkString("") }
  }

  val countryCodeParse: Parser[String] = {
    manyN(2, letter).map(_.mkString)
  }

  val asnParser: Parser[String] = {
    long.filter(asn => {
      asn >= 0 && asn <= "4294967296".toLong
    }).map(_.toString())
  }

  val ipv4Parser: Parser[String] = {

    val octetParser: Parser[Int] =
      int
        .filter(n => n >= 0 && n <= 255)

    val dotParser: Parser[Char] = char('.')

    (octetParser <~ dotParser,
      octetParser <~ dotParser,
      octetParser <~ dotParser, octetParser).mapN((a, b, c, d) => s"$a.$b.$c.$d")
  }

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

  val valueParser: Parser[Long] = {
    long.filter(value => {
      value >= 0 && value <= Math.pow(2, 32).toLong
    })
  }

  val statusParser: Parser[String] = {
    string("available")   |
    string("allocated")   |
    string("assigned")    |
    string("reserved")
  }

  val standardStatusParser: Parser[String] = {
    string("allocated") | string("assigned")
  }

  val extendedStatusParser: Parser[String] = statusParser

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
