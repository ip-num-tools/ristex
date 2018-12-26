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
      })map((date: Int) => padWithZero(date.toString))
    }

    (yearParser, monthParser, dateParser).mapN(_ ++ _ ++ _).map(_.mkString(""))
  }


  def versionParser(version:Int): Parser[Int] = {
    int
      .filter((n: Int) => n == version)
  }

  def versionParser : Parser[Int] = {
    int
      .filter((n: Int) => n == 1 || n == 2)
  }

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


  val summaryParser: Parser[String] = anyStringOrDigit


  val endDateParser: Parser[String] = yearMonthDateParser


  val UTCoffsetParser: Parser[String] = {
    val prefix: Parser[Char] = char('+') | char('-')
    val hours: Parser[List[Char]] = count(4, digit)
    val value: Parser[(Char, List[Char])] = prefix ~ hours
    value.map { case (a, b) => (a +: b).mkString("") }
  }

}
