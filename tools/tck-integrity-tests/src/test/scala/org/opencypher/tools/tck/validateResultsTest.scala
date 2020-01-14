/*
 * Copyright (c) 2015-2020 "Neo Technology,"
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

class validateResultsTest extends TckTestSupport {

  test("should accept valid values") {
    validateResults(tableOf(Seq("column1", "column2"), Seq("1", "2"), Seq("true", "null"), Seq("3.14", "'s'"))) shouldBe None
  }

  test("should not accept an invalid value") {
    val table = tableOf(Seq("column1", "column2"), Seq("'foo'", "bar"))
    validateResults(table) shouldBe Some("1 expected result values had invalid format: bar")
  }

  test("should report all invalid values") {
    val table = tableOf(Seq("column1", "column2"), Seq("'foo", "bar"), Seq("[}", "(::)"))
    validateResults(table) shouldBe Some("4 expected result values had invalid format: 'foo, bar, [}, (::)")
  }

  // Tests for single result values

  test("integer should be ok") {
    validateResults("1") shouldBe true
  }

  test("float should be ok") {
    validateResults("1.0") shouldBe true
  }

  test("boolean should be ok") {
    validateResults("false") shouldBe true
  }

  test("string should be ok") {
    validateResults("'string'") shouldBe true
  }

  test("null should be ok") {
    validateResults("null") shouldBe true
  }

  test("list should be ok") {
    validateResults("[1, 2, 3]") shouldBe true
  }

  test("map should be ok") {
    validateResults("{k: 1, k2: true}") shouldBe true
  }

  test("node should be ok") {
    validateResults("()") shouldBe true
  }

  test("relationship should be ok") {
    validateResults("[:T]") shouldBe true
  }

  test("path should be ok") {
    validateResults("<()>") shouldBe true
  }

}
