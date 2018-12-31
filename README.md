## RISTEX

Ristex is a parser combinator library for parsing the **RI**R **St**atistics **Ex**change files. 
In theory it should work with the files published by all regional internet registry, in practice, it has only been used with files published by 
RIPE NCC.

A description of the RIR statistics exchange format, published by RIPE NCC can be found [here](https://ftp.ripe.net/pub/stats/ripencc/RIR-Statistics-Exchange-Format.txt). 

Ristex uses [Atto](http://tpolecat.github.io/atto/), which is a compact, pure-functional, incremental text parsing library for Scala.

Ristex essentially extends Atto, and makes no attempt to abstract it away, hence all of Atto's API is available for 
use once you import `io.geekabyte.parsers.RirstexApi._`

### Getting started

TODO

### Overview

Always start by importing everything from `RirstexApi` via

```
import io.geekabyte.parsers.RirstexApi._`
```

Ristex exposes three kinds of parsing functionality:

1) Line Parsers: Ability to parse a value per line

These allows parsing one single value from a line. After parsing a value, the parsing moves to the next line. These 
parsers are suffixed with `lines`. They include `CommentLines`, `HeaderLines` and `RecordLines`. and they are categorised based on the sections found in the RIR exchange statistics files.
There are two kinds of these parsers. The ones that is used to start the parsing. These are prefixed with `init`. The
 other ones are without the prefix are to be used after the prefixed ones. 
 
2) Value Parsers: Ability to parse multiple values within a line.

These allow parsing multiple values from a single line. After parsing a value from a line, it is still possible to 
parse other values from the same line. These parsers can be found in `io.geekabyte.parsers.Base`.

3. Utility Parsers: These are utility parsers and can be found in `io.geekabyte.parsers.Util`.

## Examples

The code snippets below will use the following sample data as input for parsing

```
val input = 
"""|2|ripencc|1544569199|123397|19830705|20181211|+0100
 |ripencc|*|ipv4|*|71111|summary1
 |#this is a comment
 |ripencc|*|asn|*|33984|summary2
 |ripencc|*|ipv6|*|18302|summary3
 |#this another comment
 |ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated
 |ripencc|EU|ipv6|2001:600::|32|19990826|allocated
 |ripencc|GB|asn|210331|1|20180821|assigned
""".stripMargin
```

Also the examples assumes the following import has already being done

```
import io.geekabyte.parsers.RirstexApi._`
```

##### Line Parser examples

###### Comment parsers:

```
val initComment = CommentLines.initComment.parseOnly(input)

println(initComment.parseOnly(input).option.get) // prints "this is a comment"

val allComment = CommentLines.all.parseOnly(input)

println(allComment.parseOnly(input).option.get) // prints "List(this is a comment, this another comment)" 
``` 
 
###### Header parsers:

Start by parsing all the version line

```
// prints "(2.0,ripencc,1544569199,123397,19830705,20181211,+0100)"

println {
   HeaderLines.VersionLine.initAll.parseOnly(input).option.get
}
```

Start by parsing the registry line

```
// prints "ripencc"
println {
 HeaderLines.VersionLine.initRegistry.parseOnly(input).option.get
}
```

Start by parsing all the comments

```
// prints List((ripencc,ipv4,71111,summary1), (ripencc,asn,33984,summary2), (ripencc,ipv6,18302,summary3))

println {
 HeaderLines.SummaryLine.initAll.parseOnly(input).option.get
}
```

Starts by parsing the first registry, skip the comments, then parse the next summary line

```
// prints (ripencc,asn,33984,summary2)"

print {

((HeaderLines.SummaryLine.initRegistry ~ Util.lb ~ CommentLines.comment ~ Util.lb) 
      ~> HeaderLines.SummaryLine.next)
      .parseOnly(input)

}
``` 
 
###### Record parsers: 

Parse all records

```
// print List((ripencc,FR,ipv4,2.0.0.0,1048576,20100712,allocated), (ripencc,EU,ipv6,2001:600::,32,19990826,allocated), (ripencc,GB,asn,210331,1,20180821,assigned))
println {
    RecordLines.Standard.initAll.parseOnly(input).option.get
}
``` 

`RecordLines.Extended.initAll` behaves exactly like `RecordLines.Standard.initAll` except for extended version of 
the RIR exchange statistic files
 
 
#### TODO Before Release

- Add some Scaladoc
- Add installation instructions and 
- Figure how to use sbt to publish to maven central 