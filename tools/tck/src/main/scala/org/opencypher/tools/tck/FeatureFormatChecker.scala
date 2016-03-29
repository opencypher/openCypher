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

import cucumber.api.DataTable
import cucumber.api.scala.{EN, ScalaDsl}
import org.opencypher.tools.tck.TCKStepDefinitions._

class FeatureFormatChecker extends ScalaDsl with EN {

  private var lastSeenQuery = ""
  private val orderBy = "(?i).*ORDER BY.*"

  (new Step("Background")) (BACKGROUND) {}

  Given(NAMED_GRAPH) { (name: String) =>
    verifyNamedGraph(name).map(msg => throw new InvalidFeatureFormatException(msg))
  }

  Given(ANY) {}

  Given(EMPTY) {}

  And(INIT_QUERY) { (query: String) =>
    verifyCodeStyle(query).map(msg => throw new InvalidFeatureFormatException(msg))
  }

  And(PARAMETERS) { (table: DataTable) =>
    verifyParameters(table).map(msg => throw new InvalidFeatureFormatException(msg))
  }

  When(EXECUTING_QUERY) { (query: String) =>
    verifyCodeStyle(query).map(msg => throw new InvalidFeatureFormatException(msg))
    lastSeenQuery = query
  }

  Then(EXPECT_RESULT) { (table: DataTable) =>
    verifyResults(table).map(msg => throw new InvalidFeatureFormatException(msg))
//    if (lastSeenQuery.matches(orderBy))
//      throw new InvalidFeatureFormatException(
//        "Queries with `ORDER BY` needs ordered expectations. Please see the readme.")
  }

  Then(EXPECT_ERROR) { (status: String, phase: String, detail: String) =>
    verifyError(status, phase, detail).map(msg => throw new InvalidFeatureFormatException(msg))
  }

  Then(EXPECT_SORTED_RESULT) { (table: DataTable) =>
    verifyResults(table).map(msg => throw new InvalidFeatureFormatException(msg))
//    if (!lastSeenQuery.matches(orderBy))
//      throw new InvalidFeatureFormatException(
//        "Queries with ordered expectations should have `ORDER BY` in them. Please see the readme.")
  }

  Then(EXPECT_EMPTY_RESULT) {}

  And(SIDE_EFFECTS) { (table: DataTable) =>
    verifySideEffects(table).map(msg => throw new InvalidFeatureFormatException(msg))
  }

  class InvalidFeatureFormatException(message: String) extends RuntimeException(message)

}

