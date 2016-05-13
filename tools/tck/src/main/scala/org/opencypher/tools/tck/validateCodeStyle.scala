/*
 * Copyright (c) 2015-2016 "Neo Technology,"
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
 */
package org.opencypher.tools.tck

/**
  * This function will check that the input query string adheres to the specified code styling rules for Cypher queries.
  * If the code had bad styling, a message will be returned showing the bad query and a prettified version of it,
  * otherwise None will be returned.
  */
object validateCodeStyle extends (String => Option[String]) {

  override def apply(query: String): Option[String] = {
    val prettified1 = upperCased.foldLeft(query) {
      case (q, word) => q.replaceAll(s"(?i)(^|[^a-zA-Z])$word ", s"$$1$word ")
    }
    val lowerCased2 = lowerCased.foldLeft(prettified1) {
      case (q, word) => q.replaceAll(s"(?i)(^|[^a-zA-Z])$word ", s"$$1$word ")
    }

    val onlySingleQuotes = lowerCased2.replaceAll("\"(.+)\"", "'$1'")

    val spaceAfterComma = if (onlySingleQuotes.contains("'")) {
      onlySingleQuotes // it's difficult to find out whether the comma is in a string or not... just avoid this case
    } else onlySingleQuotes.replaceAll(",([^ \\n])", ", $1")

    val spaceAfterColon = spaceAfterComma.replaceAll(":([^A-Z ])", ": $1")

    if (spaceAfterColon != query)
      Some( s"""A query did not follow style requirements:
                |$query
                |
                |Prettified version:
                |$spaceAfterColon""".stripMargin)
    else None
  }

  // TODO: Write a proper style checker, that is able to interpret context and do proper parsing
  // The result will probably be similar to the class Prettifier in the neo4j repository, which
  // we could use, but a dependency on neo4j that way would be very smelly.

  private val lowerCased = Set("true",
                               "false",
                               "null",
                               "exists",
                               "filter",
                               "count",
                               "toInt",
                               "collect",
                               "extract")

  private val upperCased = Set("MATCH",
                             "OPTIONAL",
                             "WHERE",
                             "RETURN",
                             "LOAD",
                             "CSV",
                             "ORDER",
                             "BY",
                             "CREATE",
                             "INDEX",
                             "DROP",
                             "CONSTRAINT",
                             "ON",
                             "PERIODIC",
                             "COMMIT",
                             "DETACH",
                             "DELETE",
                             "START",
                             "WITH",
                             "SKIP",
                             "LIMIT",
                             "ASC",
                             "DESC",
                             "WHEN",
                             "CASE",
                             "THEN",
                             "ELSE",
                             "ASSERT",
                             "SCAN",
                             "CALL",
                             "STARTS",
                             "ENDS",
                             "WITH",
                             "CONTAINS",
                             "DISTINCT",
                             "OR",
                             "XOR",
                             "AND",
                             "IN",
                             "UNIQUE",
                             "AS",
                             "UNION")

}
