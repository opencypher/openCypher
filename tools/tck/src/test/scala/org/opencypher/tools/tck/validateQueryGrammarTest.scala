/*
 * Copyright (c) 2015-2018 "Neo Technology,"
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

import org.opencypher.tools.grammar.Antlr4TestUtils

class validateQueryGrammarTest extends TckTestSupport {

  validateQueryGrammar.f = new java.util.function.Consumer[String] {
    override def accept(query: String): Unit = Antlr4TestUtils.parse(query)
  }

  test("parsing should work") {
    validateQueryGrammar("MATCH (a) RETURN a")
  }

  test("bad query should fail parsing") {
    an [AssertionError] shouldBe thrownBy {
      validateQueryGrammar("not a query")
    }
  }
}
