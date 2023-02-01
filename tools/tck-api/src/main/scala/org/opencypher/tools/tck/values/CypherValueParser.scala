/*
 * Copyright (c) 2015-2023 "Neo Technology,"
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
      case _ => throw new RuntimeException("Unexpected parser output")
    }
  }

  // Entry-level parsers:

  private def cypherValueFromEntireInput[X: P]: P[CypherValue] =
    Start ~ cypherValue ~ End

  private def cypherValue[X: P]: P[CypherValue] =
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

  private def node[X: P]: P[CypherNode] =
    P("(" ~~/ label.rep ~/ map.? ~/ ")").map { case (labels, properties) =>
      CypherNode(labels.toSet, properties)
    }

  private def relationship[X: P]: P[CypherRelationship] =
    P("[" ~~ label ~/ map.? ~/ "]").map { case (relType, props) =>
      CypherRelationship(relType, props)
    }

  private def path[X: P]: P[CypherPath] =
    P("<" ~~/ node ~~/ pathLink.rep ~~/ ">").map { case (node, links) =>
      CypherPath(node, links.toList)
    }

  private def list[X: P]: P[CypherList] =
    P("[" ~~ cypherValue.rep(sep = ",") ~~/ "]").map { seq =>
      if (orderedLists) {
        CypherOrderedList(seq.toList)
      }
      else {
        CypherUnorderedList(seq.toList)
      }
    }

  private def map[X: P]: P[CypherPropertyMap] =
    P("{" ~~/ keyValuePair.rep(sep = ",") ~~/ "}").map { keyValuePairs =>
      CypherPropertyMap(keyValuePairs.toMap)
    }

  private def string[X: P]: P[CypherString] =
    P("'" ~/ (stringChunk | backslash | escape).rep.!.map { s =>
      val escaped = s.replace("\\'", "'").replace("\\\\", "\\")
      CypherString(escaped)
    } ~ "'")

  private def float[X: P]: P[CypherFloat] =
    P("-".? ~ floatRepr).!.map { s =>
      CypherFloat(s.toDouble)
    }

  private def integer[X: P]: P[CypherInteger] =
    P("-".? ~~ digits).!.map { s =>
      CypherInteger(s.toLong)
    }

  private def boolean[X: P]: P[CypherBoolean] =
    P("true" | "false").!.map { s =>
      CypherBoolean(s.toBoolean)
    }

  private def nullValue[X: P]: P[CypherNull.type] =
    P("null").!.map { _ =>
      CypherNull
    }

  private def nanValue[X: P]: P[CypherNaN.type] =
    P("NaN").!.map { _ =>
      CypherNaN
    }

  // Sub-parsers:

  private def label[X: P]: P[String] = ":" ~~ symbolicName.!

  private def keyValuePair[X: P]: P[(String, CypherValue)] = symbolicName ~~ ":" ~ cypherValue

  private def pathLink[X: P]: P[Connection] = (forwardRel ~~ node).map(forward) | (backRel ~~ node).map(backward)
  private def forwardRel[X: P]: P[CypherRelationship] = "-" ~~ relationship ~~ "->"
  private def backRel[X: P]: P[CypherRelationship] = "<-" ~~ relationship ~~ "-"

  private def symbolicName[X: P]: P[String] = CharsWhileIn("a-zA-Z0-9$_").!

  /**
    * A 'simple' string chunk; without apostrophes or backslash (escape sequence)
    */
  private def stringChunk[X: P]: P[Unit] = {
    CharsWhile(c => c != '\'' && c != '\\')
  }
  /**
    * We escape apostrophes inside strings using a backslash
    */
  private def escape[X: P]: P[Unit] = {
    P("\\") ~/ P("'")
  }
  /**
    * Since backslash is used
    */
  private def backslash[X: P]: P[Unit] = {
    P("\\\\")
  }

  private def digits[X: P]: P[Unit] = CharsWhileIn("0-9")
  private def floatRepr[X: P]: P[Unit] =
    (digits ~~ "." ~~ digits ~~ exponent.?) |
      ("." ~~ digits ~~ exponent.?) |
      (digits ~~ exponent)
  private def exponent[X: P]: P[Unit] = IgnoreCase("e") ~~ "-".? ~~ digits

  private def newline[X: P]: P[Unit] = "\n" | "\r\n" | "\r" | "\f"
  private def invisible[X: P]: P[Unit] = " " | "\t" | newline

  implicit val whitespace: P[_] => P[Unit] = { implicit ctx: ParsingRun[_] => invisible.rep }
}
