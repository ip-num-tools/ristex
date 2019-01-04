## RISTEX

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.geekabyte.ristex/ristex/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.geekabyte.ristex/ristex)


Ristex is a parser combinator library for parsing the **RI**R **St**atistics **Ex**change files. 
In theory it should work with the files published by all regional internet registry, in practice, it has only been used with files published by 
RIPE NCC.

A description of the RIR statistics exchange format, published by RIPE NCC can be found [here](https://ftp.ripe.net/pub/stats/ripencc/RIR-Statistics-Exchange-Format.txt). 

Ristex uses [Atto](http://tpolecat.github.io/atto/), which is a compact, pure-functional, incremental text parsing library for Scala.

Ristex essentially extends Atto, and makes no attempt to abstract it away, hence all of Atto's API is available for 
use once you import `io.geekabyte.ristex.parsers.AttoApi._`

### Getting started

Add via sbt as follows:

```
libraryDependencies += "io.geekabyte.ristex" %% "ristex" % "${version}"
```

### Overview

Always start by importing everything from `AttoApi` via

```
import io.geekabyte.ristex.parsers.AttoApi._
```

Ristex exposes three kinds of parsing functionality:

#### 1. Line Parsers: Parses a value per line

These allows parsing one single value from a line. After parsing a value, the parsing moves to the next line. These 
parsers are suffixed with `lines`. They include `CommentLines`, `HeaderLines` and `RecordLines`. and they are 
categorised based on the sections found in the RIR exchange statistics files which is described below.

The RIR exchange statistic files is divided into the following sections:

1. File Header: Contains meta information about the file. It is made up of:
   - Version line: Contains information like version, serial number of file etc
   - Summary line: Contains a summary of the record lines in the file 
2. Records: Contains the record entries.
   
Apart from these, the files can contain comments. Note also that the record lines differs depending on whether the 
file being viewed is an extended file format or not. More information about the format can be found [here](https://ftp.ripe.net/pub/stats/ripencc/RIR-Statistics-Exchange-Format.txt) 

The above structure of the RIR exchange statistic files is reflected in Ristex:

| RIR exchange statistic     | Ristex                   |
| -------------------------  | ------------------------ |
| File Header/Version line   | HeaderLines.VersionLine  |
| File Header/Summary line   | HeaderLines.SummaryLine  |
| Records (standard format)  | RecordLines.Standard     |
| Records (extended format)  | RecordLines.Extended     |
| Comments                   | CommentLines              |


There are two kinds of line parsers. The ones that is used to start the parsing. These are prefixed with `init`. 
Example is `HeaderLines.SummaryLine.initRegistry` which starts the parsing from the first registry value in the 
Summary section.

The other ones are without the `init` prefix and are to be used after the prefixed ones. 
 
#### 2. Value Parsers: Parses multiple values within a line.

These allow parsing multiple values from a single line. After parsing a value from a line, it is still possible to 
parse other values from the same line. These parsers can be found in `io.geekabyte.ristex.parsers.Base`.

#### 3. Utility Parsers

These are utility parsers and can be found in `io.geekabyte.ristex.parsers.Util`.

## Examples

The code snippets below will use the following sample data as input for parsing

```
val input = 
"""|2|ripencc|1544569199|123397|19830705|20181211|+0100
 |ripencc|*|ipv4|*|71111|summary
 |ripencc|*|asn|*|33984|summary
 |ripencc|*|ipv6|*|18302|summary
 |#this is a comment
 |#this another comment
 |ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
 |ripencc|EU|ipv6|2001:600::|32|19990826|allocated
 |ripencc|GB|asn|210331|1|20180821|assigned
""".stripMargin
```

Also the examples assumes the following imports has already being made

```
scala> import io.geekabyte.ristex.parsers._
import io.geekabyte.ristex.parsers._

scala> import io.geekabyte.ristex.parsers.AttoApi._
import io.geekabyte.ristex.parsers.AttoApi._
```

Finally the output of the examples is in the Atto format where the value parsed and value remaining after parsing is 
seen. For example:

```
scala> Parser.parseOnly(input)

res3: atto.ParseResult[String] =
Done(
remaining text after parsing
,parsed text)
```

### Line Parser examples

#### Comment lines parsing (via CommentLines):

Parse the first comment:

```
CommentLines.firstComment.parseOnly(input)
 
res3: atto.ParseResult[String] =
Done(#this another comment
 ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
 ripencc|EU|ipv6|2001:600::|32|19990826|allocated
 ripencc|GB|asn|210331|1|20180821|assigned
       ,this is a comment)
``` 

Parse all the comments: 

```
scala> CommentLines.all.parseOnly(input)

res4: atto.ParseResult[List[String]] =
Done(ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
ripencc|EU|ipv6|2001:600::|32|19990826|allocated
ripencc|GB|asn|210331|1|20180821|assigned
      ,List(this is a comment, this another comment))
```
 
#### Header section parsing (via HeaderLines.VersionLine):

Initiate the parsing by parsing the version line

```
scala> HeaderLines.VersionLine.initParse.parseOnly(input)

res0: atto.ParseResult[(Double, String, Int, Int, String, String, String)] =
Done(
ripencc|*|ipv4|*|71111|summary
ripencc|*|asn|*|33984|summary
ripencc|*|ipv6|*|18302|summary
#this is a comment
#this another comment
ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
ripencc|EU|ipv6|2001:600::|32|19990826|allocated
ripencc|GB|asn|210331|1|20180821|assigned
      ,(2.0,ripencc,1544569199,123397,19830705,20181211,+0100))
```

Initiate the parsing by parsing the version in the version line

```
scala> HeaderLines.VersionLine.initVersion.parseOnly(input)

res1: atto.ParseResult[Double] =
Done(
ripencc|*|ipv4|*|71111|summary
ripencc|*|asn|*|33984|summary
ripencc|*|ipv6|*|18302|summary
#this is a comment
#this another comment
ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
ripencc|EU|ipv6|2001:600::|32|19990826|allocated
ripencc|GB|asn|210331|1|20180821|assigned
      ,2.0)
```


Initiate the parsing by parsing the UTC Offset in the version line

```
scala> HeaderLines.VersionLine.initUTCoffset.parseOnly(input)

res2: atto.ParseResult[String] =
Done(
ripencc|*|ipv4|*|71111|summary
ripencc|*|asn|*|33984|summary
ripencc|*|ipv6|*|18302|summary
#this is a comment
#this another comment
ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
ripencc|EU|ipv6|2001:600::|32|19990826|allocated
ripencc|GB|asn|210331|1|20180821|assigned
      ,+0100)
```

#### Header section parsing (via HeaderLines.SummaryLine):

Initiate the parsing by parsing all the summary lines

```
scala> HeaderLines.SummaryLine.initParseAll.parseOnly(input)

res0: atto.ParseResult[List[(String, String, Int, String)]] =
Done(#this another comment
ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
ripencc|EU|ipv6|2001:600::|32|19990826|allocated
ripencc|GB|asn|210331|1|20180821|assigned
      ,List((ripencc,ipv4,71111,summary), (ripencc,asn,33984,summary), (ripencc,ipv6,18302,summary)))
```

Initiate the parsing by parsing the first summary line

```
scala> HeaderLines.SummaryLine.initParseFirst.parseOnly(input)

res1: atto.ParseResult[(String, String, Int, String)] =
Done(ripencc|*|asn|*|33984|summary
ripencc|*|ipv6|*|18302|summary
#this is a comment
#this another comment
ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
ripencc|EU|ipv6|2001:600::|32|19990826|allocated
ripencc|GB|asn|210331|1|20180821|assigned
      ,(ripencc,ipv4,71111,summary))
``` 

Initiate the parsing by parsing the first summary line 
and then parse the second line

```
scala> (HeaderLines.SummaryLine.initParseFirst ~> HeaderLines.SummaryLine.nextLine).parseOnly(input)

res2: atto.ParseResult[(String, String, Int, String)] =
Done(#this another comment
ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
ripencc|EU|ipv6|2001:600::|32|19990826|allocated
ripencc|GB|asn|210331|1|20180821|assigned
      ,(ripencc,asn,33984,summary))
```

Initiate the parsing by parsing the first summary line 
and then parse the resource type from second line

```
scala> (HeaderLines.SummaryLine.initParseFirst ~> HeaderLines.SummaryLine.nextType).parseOnly(input)

res3: atto.ParseResult[String] =
Done(
ripencc|*|ipv6|*|18302|summary
#this is a comment
#this another comment
ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
ripencc|EU|ipv6|2001:600::|32|19990826|allocated
ripencc|GB|asn|210331|1|20180821|assigned
      ,asn)
```

Initiate the parsing by parsing the first resource type

```
scala> (HeaderLines.SummaryLine.firstIPType).parseOnly(input)

res4: atto.ParseResult[String] =
Done(
ripencc|*|asn|*|33984|summary
ripencc|*|ipv6|*|18302|summary
#this is a comment
#this another comment
ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
ripencc|EU|ipv6|2001:600::|32|19990826|allocated
ripencc|GB|asn|210331|1|20180821|assigned
      ,ipv4)

```
 
#### Record entry parsing (via RecordLines.Standard and RecordLines.Extended): 

> Note that `RecordLines.Extended.*` behaves exactly like `RecordLines.Standard.*` except that extended version of 
  the RIR exchange statistic files has `RecordLines.Extended.firstOpaqueId` and `RecordLines.Extended.nextOpaqueId`

Initiate the parse by parsing all records

```
scala> (RecordLines.Standard.initParseAll).parseOnly(input)

res1: atto.ParseResult[List[(String, String, String, String, Long, String, String)]] = Done(
,List(
(ripencc,FR,ipv4,2.0.0.0,1048576,20100712,allocated), 
(ripencc,EU,ipv6,2001:600::,32,19990826,allocated), 
(ripencc,GB,asn,210331,1,20180821,assigned))
)
``` 

Initiate the parse by parsing the first record line
```
scala> (RecordLines.Standard.initParseFirst).parseOnly(input)

res1: atto.ParseResult[(String, String, String, String, Long, String, String)] =
Done(ripencc|EU|ipv6|2001:600::|32|19990826|allocated
ripencc|GB|asn|210331|1|20180821|assigned,(ripencc,FR,ipv4,2.0.0.0,1048576,20100712,allocated))
```

Initiate the parse by parsing the first country code

```
scala> (RecordLines.Standard.firstCountryCode).parseOnly(input)

res2: atto.ParseResult[String] =
Done(ripencc|EU|ipv6|2001:600::|32|19990826|allocated
ripencc|GB|asn|210331|1|20180821|assigned
      ,FR)
```

Initiate the parse by parsing the first country code, then parse the second country code

```
scala> (RecordLines.Standard.firstCountryCode ~> RecordLines.Standard.nextCountryCode).parseOnly(input)

res0: atto.ParseResult[String] = Done(ripencc|GB|asn|210331|1|20180821|assigned,EU)
```

 