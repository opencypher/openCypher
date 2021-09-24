/*
 * Copyright (c) 2015-2021 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.tools.tck.values
import fastparse.Parsed.{Failure, Success}
import fastparse._
import org.opencypher.tools.tck.values.Connection.{backward, forward}

case class CypherValueParseException(msg: String, expected: String) extends Exception(msg)

class CypherValueParser(val orderedLists: Boolean) {
  def parse(s: String): CypherValue = {
    fastparse.parse(s, cypherValueFromEntireInput(_), verboseFailures = true) match {
      case Success(value, _) => value
      case Failure(expected, index, extra) =>
        val before = index - math.max(index - 20, 0)
        val after = math.min(index + 20, extra.input.length) - index
        val locationPointer =
          s"""|\t${extra.input.slice(index - before, index + after).replace('\n', ' ')}
              |\t${"~" * before + "^" + "~" * after}""".stripMargin
        val msg =
          s"""|Failed at index $index:
              |
              |Expected:\t$expected
              |
              |$locationPointer
              |
              |${extra.trace().msg}""".stripMargin
        throw CypherValueParseException(msg, expected)
    }
  }

  // Entry-level parsers:

  private def cypherValueFromEntireInput[_: P]: P[CypherValue] =
    Start ~ cypherValue ~ End

  private def cypherValue[_: P]: P[CypherValue] =
    node |
      relationship |
      path |
      list |
      map |
      string |
      float |
      integer |
      boolean |
      nullValue |
      nanValue

  private def node[_: P]: P[CypherNode] =
    P("(" ~~/ label.rep ~/ map.? ~/ ")").map { case (labels, properties) =>
      CypherNode(labels.toSet, properties)
    }

  private def relationship[_: P]: P[CypherRelationship] =
    P("[" ~~ label ~/ map.? ~/ "]").map { case (relType, props) =>
      CypherRelationship(relType, props)
    }

  private def path[_: P]: P[CypherPath] =
    P("<" ~~/ node ~~/ pathLink.rep ~~/ ">").map { case (node, links) =>
      CypherPath(node, links.toList)
    }

  private def list[_: P]: P[CypherList] =
    P("[" ~~ cypherValue.rep(sep = ",") ~~/ "]").map { seq =>
      if (orderedLists) {
        CypherOrderedList(seq.toList)
      }
      else {
        CypherUnorderedList(seq.toList)
      }
    }

  private def map[_: P]: P[CypherPropertyMap] =
    P("{" ~~/ keyValuePair.rep(sep = ",") ~~/ "}").map { keyValuePairs =>
      CypherPropertyMap(keyValuePairs.toMap)
    }

  private def string[_: P]: P[CypherString] =
    P("'" ~/ (stringChunk | backslash | escape).rep.!.map { s =>
      val escaped = s.replaceAllLiterally("\\'", "'").replaceAllLiterally("\\\\", "\\")
      CypherString(escaped)
    } ~ "'")

  private def float[_: P]: P[CypherFloat] =
    P("-".? ~ floatRepr).!.map { s =>
      CypherFloat(s.toDouble)
    }

  private def integer[_: P]: P[CypherInteger] =
    P("-".? ~~ digits).!.map { s =>
      CypherInteger(s.toLong)
    }

  private def boolean[_: P]: P[CypherBoolean] =
    P("true" | "false").!.map { s =>
      CypherBoolean(s.toBoolean)
    }

  private def nullValue[_: P]: P[CypherNull.type] =
    P("null").!.map { _ =>
      CypherNull
    }

  private def nanValue[_: P]: P[CypherNaN.type] =
    P("NaN").!.map { _ =>
      CypherNaN
    }

  // Sub-parsers:

  private def label[_: P]: P[String] = ":" ~~ symbolicName.!

  private def keyValuePair[_: P]: P[(String, CypherValue)] = symbolicName ~~ ":" ~ cypherValue

  private def pathLink[_: P]: P[Connection] = (forwardRel ~~ node).map(forward) | (backRel ~~ node).map(backward)
  private def forwardRel[_: P]: P[CypherRelationship] = "-" ~~ relationship ~~ "->"
  private def backRel[_: P]: P[CypherRelationship] = "<-" ~~ relationship ~~ "-"

  private def symbolicName[_: P]: P[String] = CharsWhileIn("a-zA-Z0-9$_").!

  /**
    * A 'simple' string chunk; without apostrophes or backslash (escape sequence)
    */
  private def stringChunk[_: P]: P[Unit] = {
    CharsWhile(c => c != '\'' && c != '\\')
  }
  /**
    * We escape apostrophes inside strings using a backslash
    */
  private def escape[_: P]: P[Unit] = {
    P("\\") ~/ P("'")
  }
  /**
    * Since backslash is used
    */
  private def backslash[_: P]: P[Unit] = {
    P("\\\\")
  }

  private def digits[_: P]: P[Unit] = CharsWhileIn("0-9")
  private def floatRepr[_: P]: P[Unit] =
    (digits ~~ "." ~~ digits ~~ exponent.?) |
      ("." ~~ digits ~~ exponent.?) |
      (digits ~~ exponent)
  private def exponent[_: P]: P[Unit] = IgnoreCase("e") ~~ "-".? ~~ digits

  private def newline[_: P]: P[Unit] = "\n" | "\r\n" | "\r" | "\f"
  private def invisible[_: P]: P[Unit] = " " | "\t" | newline

  implicit val whitespace: P[_] => P[Unit] = { implicit ctx: ParsingRun[_] => invisible.rep }
}
