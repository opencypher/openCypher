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
package org.opencypher.tools.tck

import org.opencypher.tools.grammar.Antlr4TestUtils
import org.opencypher.tools.tck.api.ControlQuery
import org.opencypher.tools.tck.api.ExecQuery
import org.opencypher.tools.tck.api.Execute
import org.opencypher.tools.tck.api.InitQuery
import org.opencypher.tools.tck.api.SideEffectQuery
import org.opencypher.tools.tck.constants.TCKTags
import org.scalatest.AppendedClues
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait ValidateQuery extends AppendedClues with Matchers with DescribeStepHelper {
  private val parsedCache = new scala.collection.mutable.HashMap[String, Try[Unit]]()
  private val normalizedCached = new scala.collection.mutable.HashMap[String, Boolean]()

  def validateQuery(execute: Execute, tags: Set[String] = Set.empty[String]): Assertion = {
    val query = execute.query
    val normalized = normalizedCached.getOrElseUpdate(query, normalize(query) == query)
    val parsed = parsedCache.getOrElseUpdate(query, parse(query))
    execute.qt match {
      case ExecQuery =>
        withClue(s"the query of ${execute.description} has either a syntax conforming to the grammar or the scenario has the ${TCKTags.SKIP_GRAMMAR_CHECK} tag") {
          val hasSkipGrammarCheckTag = tags contains TCKTags.SKIP_GRAMMAR_CHECK
          (parsed, hasSkipGrammarCheckTag) should matchPattern {
            case (Failure(_), true) =>
            case (Success(_), false) =>
          }
        }

        withClue(s"the query of ${execute.description} has either a syntax conforming to the style requirements or the scenario has the ${TCKTags.SKIP_STYLE_CHECK} tag; expected style: $normalized") {
          (normalized, tags contains TCKTags.SKIP_STYLE_CHECK) should matchPattern {
            case (true, false) =>
            case (_, true) =>
          }
        }
      case InitQuery | ControlQuery =>
        withClue(s"the query of ${execute.description} has a syntax conforming to the grammar") {
          parsed should matchPattern {
            case Success(_) =>
          }
        }

        withClue(s"the query of ${execute.description} has a syntax conforming to the style requirements; expected style: $normalized") {
          normalized shouldBe true
        }
      case SideEffectQuery => succeed
    }
  }

  private def normalize(query: String): String = NormalizeQueryStyle(query)

  private def parse(query: String): Try[Unit] = Try(Antlr4TestUtils.parse(query))
}
