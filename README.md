## RISTEX

Ristex is a parser combinator library for parsing the RIr STatistics EXchange files. In theory it should work with 
the files published by all regional internet registry, in practice, it has only been used with files published by 
RIPE NCC

#### TODO Before Release

- Spruce up things a little bit
- Add one or more utility functionality:
    - Ability to parse a line by country code
    - Ability to parse a line by IP value (eq, lt, gt)
- Add some Scaladoc
- Add installation instructions and examples of usage to README
- Figure how to use sbt to publish to maven central 