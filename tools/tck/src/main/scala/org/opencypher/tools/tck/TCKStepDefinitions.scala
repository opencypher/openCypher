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

object TCKStepDefinitions {

  // for Background
  val BACKGROUND = "^$"

  // for Given
  val ANY = "^any graph$"
  val EMPTY = "^an empty graph$"
  val NAMED_GRAPH = "^the (.*) graph$"

  // for And
  val INIT_QUERY = "^having executed: (.*)$"
  val PARAMETERS = "^parameters are:$"
  val SIDE_EFFECTS = "^the side effects should be:$"

  // for When
  val EXECUTING_QUERY = "^executing query: (.*)$"

  // for Then
  val EXPECT_RESULT = "^the result should be:$"
  val EXPECT_SORTED_RESULT = "^the result should be, in order:$"
  val EXPECT_EMPTY_RESULT = "^the result should be empty$"
  val EXPECT_ERROR = "^a (.+) should be raised at (.+): (.+)$"
}
