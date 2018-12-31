package io.geekabyte.parsers

import atto.{parser, syntax}

/**
  * Provides a mechanism to import Atto's API
  */
object RistexApi extends parser.Parsers with syntax.Syntaxes
