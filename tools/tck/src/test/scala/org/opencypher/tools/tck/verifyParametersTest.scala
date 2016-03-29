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

class verifyParametersTest extends TckTestSupport {

  test("should accept valid parameters") {
    verifyParameters(tableOf(Seq("key1", "1"), Seq("key2", "2"))) shouldBe None
  }

  test("should not accept an invalid parameter") {
    val table = tableOf(Seq("key1", "'foo'"), Seq("key2", "bar"))
    verifyParameters(table) shouldBe Some("1 parameters had invalid format: bar")
  }

  test("should report all invalid parameters") {
    val table = tableOf(Seq("key1", "'foo"), Seq("key2", "bar"), Seq("key3", "[}"))
    verifyParameters(table) shouldBe Some("3 parameters had invalid format: 'foo, bar, [}")
  }

  // Tests for single param values

  test("integer should be ok") {
    verifyParameters("1") shouldBe true
  }

  test("float should be ok") {
    verifyParameters("1.0") shouldBe true
  }

  test("boolean should be ok") {
    verifyParameters("false") shouldBe true
  }

  test("string should be ok") {
    verifyParameters("'string'") shouldBe true
  }

  test("null should be ok") {
    verifyParameters("null") shouldBe true
  }

  test("list should be ok") {
    verifyParameters("[1, 2, 3]") shouldBe true
  }

  test("map should be ok") {
    verifyParameters("{k: 1, k2: true}") shouldBe true
  }

  test("node should not be ok") {
    verifyParameters("()") shouldBe false
  }

  test("relationship should not be ok") {
    verifyParameters("[:T]") shouldBe false
  }

  test("path should not be ok") {
    verifyParameters("<()>") shouldBe false
  }

}
