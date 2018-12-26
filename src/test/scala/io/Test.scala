package io


import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class Test extends  FlatSpec with Matchers {

  val input =
    """|2|ripencc|1543791599|123180|19830705|20181202|+0100
       |ripencc|*|ipv4|*|70977|summary
       |ripencc|*|asn|*|33966|summary
       |#lltyy
       |ripencc|*|ipv6|*|18237|summary
       |ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
       |ripencc|EU|ipv4|2.16.0.0|524288|20100910|allocated
       |ripencc|GB|ipv4|2.24.0.0|524288|20100921|allocated
    """.stripMargin

  "jaja" should "jjjjj" in {
    println {

      //(many(digit), many(digit)).mapN(_ ++ _).parse("aaabcd").done
      //(letter, digit).mapN(_ + _).parse("12cd").done
      //digit.parse("aaabcd").done

      //VersionLine.toTuple.parseOnly("2|ripencc|1543791599|123180|19830705|20181202|+0100")


//      (Base.versionParser ~ many(choice(Util.pipe,letter,digit))).parseOnly("2|ripencc|1543791599|123180|19830705|20181202|+0100")


//      (Base.versionParser <~ many(choice(Util.pipe,letter,digit)), Base.UTCoffsetParser)
//        .mapN(Tuple2.apply).parseOnly("2|ripencc|1543791599|123180|19830705|20181202|+0100")

      // many(letter).parseOnly("Hello").done

//      {
//        VersionLine.version ~ many(letter) ~ VersionLine.UTCoffset
//      }.parseOnly("2|ripencc|1543791599|123180|19830705|20181202|+0100")


//      (sepBy(VersionLine.toTuple ~ letter, char('\n'))).parseOnly(
//        """2|ripencc|1543791599|123180|19830705|20181202|+0100
//          |ripencc|*|ipv4|*|70977|summary""".stripMargin).done

//      (SummaryLine.extract).parseOnly(
//        """2|ripencc|1543791599|123180|19830705|20181202|+0100
//          |ripencc|*|ipv4|*|70977|summary""".stripMargin).done

      //(VersionLine.parse ~ char('\n') ~ many((SummaryLine.parse) ~ char('\n'))).parseOnly(input)
      //(SummaryLine.startByExtracting).parseOnly(input)

      val string: String = Source.fromFile("/Users/daderemi/Documents/play/parser/src/test/scala/test").mkString


      //(CommentLines.comment).parseOnly(input)

//      many(anyCharExcept("-", "\n")).parseOnly(
//        """1a
//          |
//          |w-2
//          |
//          |3""".stripMargin)

      //SummaryLine.registry.parseOnly("ripencc|*|ipv4|*|70977|summary")

    }
  }

}
