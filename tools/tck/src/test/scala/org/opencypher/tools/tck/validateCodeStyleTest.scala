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

class validateCodeStyleTest extends TckTestSupport {

  test("should throw on bad styling") {
    validateCodeStyle("match (n) return n") shouldBe
      Some("""A query did not follow style requirements:
             |match (n) return n
             |
             |Prettified version:
             |MATCH (n) RETURN n""".stripMargin)
  }

  test("should accept good styling") {
    validateCodeStyle("MATCH (n) RETURN n") shouldBe None
  }

  test("should request space after colon") {
    validateCodeStyle("MATCH (n {name:'test'})") shouldBe
      Some("""A query did not follow style requirements:
             |MATCH (n {name:'test'})
             |
             |Prettified version:
             |MATCH (n {name: 'test'})""".stripMargin)
  }

  test("should not request space after colon if it's a label") {
    validateCodeStyle("MATCH (n:Label)") shouldBe None
  }

  test("should not request space after colon if it's a relationship type") {
    validateCodeStyle("MATCH ()-[:T]-()") shouldBe None
  }

}
