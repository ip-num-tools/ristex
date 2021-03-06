package io.geekabyte.ristex.parsers

import atto.Atto._
import atto._
import cats.implicits._
import Base.{UTCoffsetParser, endDateParser, ipTypeParser, recordCountParser, registryParser, serialNumberParser, startDateParser, summaryParser, versionParser}
import Util.{lb, pipe, summaryPipe}

/**
  * The file header consists of
  * the version line
  *   and
  * the summary line
  *
  */
object HeaderLines {

  /**
    * Format:
    *
    *
    * version|registry|serial|records|startdate|enddate|UTCoffset
    *
    * version    = format version number of this file, currently 2;
    * registry   = as for records and filename (see below);
    * serial     = serial number of this file (within the creating RIR series);
    * records    = number of records in file, excluding blank lines,
    * summary lines, the version line and comments;
    * startdate  = start date of time period, in yyyymmdd format;
    * enddate    = end date of period, in yyyymmdd format;
    * UTCoffset  = offset from UTC of local RIR producing file,
    * in +/- HHMM format.
    *
    *
    * Note that all methods on VersionLine are suffixed with init, since the version line is the start of all RIR
    * statistics exchange files
    *
    * * See https://ftp.ripe.net/pub/stats/ripencc/RIR-Statistics-Exchange-Format.txt for more information on the
    * * format of a RIR statistic exchange file
    */
  object VersionLine {

    val initParse: Parser[(Double, String, Int, Int, String, String, String)] = {
      (versionParser <~ pipe,
        registryParser <~ pipe,
        serialNumberParser <~ pipe,
        recordCountParser <~ pipe,
        startDateParser <~ pipe,
        endDateParser <~ pipe,
        UTCoffsetParser).mapN(Tuple7.apply)
    }

    val initVersion: Parser[Double] = {
      for {
        version <- versionParser <~ {
          pipe ~ registryParser ~
            pipe ~ serialNumberParser ~
            pipe ~ recordCountParser ~
            pipe ~ startDateParser ~
            pipe ~ endDateParser ~
            pipe ~ UTCoffsetParser ~ manyN(0, lb)
        }

      } yield version
    }

    val initRegistry: Parser[String] = {
      val reg: Parser[String] = (versionParser ~ pipe) ~> registryParser
      reg <~ {
          pipe ~ serialNumberParser ~
          pipe ~ recordCountParser ~
          pipe ~ startDateParser ~
          pipe ~ endDateParser ~
          pipe ~ UTCoffsetParser ~ manyN(0, lb)
      }
    }

    val initSerial: Parser[Int] = {
      val serial: Parser[Int] = {versionParser ~ pipe ~ registryParser ~ pipe} ~> serialNumberParser
      serial <~ {
          pipe ~ recordCountParser ~
          pipe ~ startDateParser ~
          pipe ~ endDateParser ~
          pipe ~ UTCoffsetParser ~ manyN(0, lb)
      }
    }

    val initRecords: Parser[Int] = {
      val record: Parser[Int] = {
        versionParser ~ pipe ~ registryParser ~ pipe ~ serialNumberParser ~ pipe
      } ~> recordCountParser

      record <~ {
          pipe ~ startDateParser ~
          pipe ~ endDateParser ~
          pipe ~ UTCoffsetParser ~ manyN(0, lb)
      }
    }

    val initStartDate: Parser[String] = {
      val startdate: Parser[String] = {
        versionParser ~ pipe ~ registryParser ~ pipe ~ serialNumberParser ~ pipe ~ recordCountParser ~ pipe
      } ~> startDateParser

      startdate <~ {
          pipe ~ endDateParser ~
          pipe ~ UTCoffsetParser ~ manyN(0, lb)
      }
    }

    val initEndDate: Parser[String] = {
      val enddate: Parser[String] = {
        versionParser ~ pipe ~ registryParser ~ pipe ~ serialNumberParser ~ pipe ~ recordCountParser ~
          pipe ~ startDateParser ~ pipe
      } ~> endDateParser

      enddate <~ {
        pipe ~ UTCoffsetParser ~ manyN(0, lb)
      }
    }

    val initUTCoffset: Parser[String] = {
      val enddate: Parser[String] = {
        versionParser ~ pipe ~ registryParser ~ pipe ~ serialNumberParser ~ pipe ~ recordCountParser ~ pipe ~
          startDateParser ~ pipe ~ endDateParser ~ pipe
      } ~> UTCoffsetParser

      enddate <~ {
        manyN(0, lb)
      }
    }

  }

  /**
    *
    *
    * Format:
    *
    * registry|*|type|*|count|summary
    *
    * registry   = as for records (see below);
    * *          = an ASCII '*' (unused field, retained for spreadsheet purposes);
    * type       = Type of Internet number resource represented in this record,
    *               One value from the set of defined strings:
    *               {asn,ipv4,ipv6}
    * count      = the number of record lines of this type in the file;
    * summary    = the ASCII string 'summary' (to distinguish the record line).
    *
    * * See https://ftp.ripe.net/pub/stats/ripencc/RIR-Statistics-Exchange-Format.txt for more information on the
    * * format of a RIR statistic exchange file
    */
  object SummaryLine {
    private val headerVersionLine: Parser[((Double, String, Int, Int, String, String, String), Char)] =
      VersionLine.initParse ~ lb

    val initParseAll: Parser[List[(String, String, Int, String)]] = {
      val parser: Parser[(String, String, Int, String)] = (
        registryParser <~ summaryPipe,
        ipTypeParser <~ summaryPipe,
        recordCountParser <~ pipe,
        summaryParser
      ).mapN(Tuple4.apply)

      val skippingComments: Parser[(String, String, Int, String)] = parser <~ (skipMany(lb.map(_.toString)) ~ skipMany(CommentLines.comment))
      headerVersionLine ~> many(skippingComments)
    }

    val initParseFirst: Parser[(String, String, Int, String)] = {
      headerVersionLine ~> (
        registryParser <~ summaryPipe,
        ipTypeParser <~ summaryPipe,
        recordCountParser <~ pipe,
        summaryParser <~ lb
      ).mapN(Tuple4.apply)
    }

    val all: Parser[List[(String, String, Int, String)]] = {
      val parser: Parser[(String, String, Int, String)] = (
        registryParser <~ summaryPipe,
        ipTypeParser <~ summaryPipe,
        recordCountParser <~ pipe,
        summaryParser
      ).mapN(Tuple4.apply)

      val skippingComments = parser <~ many(lb ~ CommentLines.firstComment)

      sepBy(skippingComments, lb)
    }

    val nextLine: Parser[(String, String, Int, String)] = {
      val parser: Parser[(String, String, Int, String)] = (registryParser <~ summaryPipe,
        ipTypeParser <~ summaryPipe,
        recordCountParser <~ pipe,
        summaryParser).mapN(Tuple4.apply)

      parser <~ many(lb ~ CommentLines.firstComment) ~ lb
    }

    val firstRegistry: Parser[String] = {
      headerVersionLine ~> {
        nextRegistry
      }
    }

    val nextRegistry: Parser[String] = {
      registryParser <~ {
        summaryPipe ~ ipTypeParser ~
          summaryPipe ~ recordCountParser ~
          pipe ~ summaryParser
      } <~ lb
    }

    val firstIPType: Parser[String] = {
      headerVersionLine ~> {
        nextType
      }
    }

    val nextType = {
      val ipType: Parser[String] = (registryParser ~ summaryPipe) ~> ipTypeParser

      ipType <~ summaryPipe ~ recordCountParser ~ pipe ~ summaryParser ~ lb
    }

    val firstCount: Parser[Int] = {
      headerVersionLine ~> {
        nextCount
      }
    }

    val nextCount = {
      val recordCount: Parser[Int] = (registryParser ~ summaryPipe ~ ipTypeParser ~ summaryPipe) ~> recordCountParser
      recordCount <~ pipe ~ summaryParser ~ lb
    }

    val firstSummaryTag: Parser[String] = {
      headerVersionLine ~> {
        nextSummaryTag
      }
    }

    val nextSummaryTag: Parser[String] = {
      val summary: Parser[String] =
        (registryParser ~ summaryPipe ~ ipTypeParser ~ summaryPipe ~ recordCountParser ~ pipe) ~> summaryParser
      summary <~ lb
    }

  }

}
