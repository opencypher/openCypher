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
package org.opencypher.tools.tck.constants

object TCKStepDefinitions {

  // for Background
  val BACKGROUND = "^$"
  val backgroundR = BACKGROUND.r

  // for Given
  val ANY_GRAPH = "^any graph$"
  val anyGraphR = ANY_GRAPH.r

  val EMPTY_GRAPH = "^an empty graph$"
  val emptyGraphR = EMPTY_GRAPH.r

  val NAMED_GRAPH = "^the (.*) graph$"
  val namedGraphR = NAMED_GRAPH.r

  // for And
  val INIT_QUERY = "^having executed:$"
  val initQueryR = INIT_QUERY.r

  val PARAMETERS = "^parameters are:$"
  val parametersR = PARAMETERS.r

  val SIDE_EFFECTS = "^the side effects should be:$"
  val sideEffectsR = SIDE_EFFECTS.r

  val NO_SIDE_EFFECTS = "^no side effects$"
  val noSideEffectsR = NO_SIDE_EFFECTS.r

  val INSTALLED_PROCEDURE = """^there exists a procedure (.+):$"""
  val installedProcedureR = INSTALLED_PROCEDURE.r

  val CSV_FILE = "^there exists a CSV file with URL as \\$(.*), with rows:$"
  val csvFileR = CSV_FILE.r

  // for When
  val EXECUTING_QUERY = "^executing query:$"
  val executingQueryR = EXECUTING_QUERY.r

  val EXECUTING_CONTROL_QUERY = "^executing control query:$"
  val executingControlQueryR = EXECUTING_CONTROL_QUERY.r

  // for Then
  val EXPECT_RESULT = "^the result should be, in any order:$"
  val expectResultR = EXPECT_RESULT.r

  val EXPECT_SORTED_RESULT = "^the result should be, in order:$"
  val expectSortedResultR = EXPECT_SORTED_RESULT.r

  val EXPECT_RESULT_UNORDERED_LISTS = "^the result should be \\(ignoring element order for lists\\):$"
  val expectResultUnorderedListsR = EXPECT_RESULT_UNORDERED_LISTS.r

  val EXPECT_SORTED_RESULT_UNORDERED_LISTS = "^the result should be, in order \\(ignoring element order for lists\\):$"
  val expectSortedResultUnorderedListsR = EXPECT_SORTED_RESULT_UNORDERED_LISTS.r

  val EXPECT_EMPTY_RESULT = "^the result should be empty$"
  val expectEmptyResultR = EXPECT_EMPTY_RESULT.r

  val EXPECT_ERROR = "^an? (.+) should be raised at (.+): (.+)$"
  val expectErrorR = EXPECT_ERROR.r

}
