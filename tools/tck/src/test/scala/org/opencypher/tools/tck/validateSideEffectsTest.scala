/*
 * Copyright (c) 2015-2017 "Neo Technology,"
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

class validateSideEffectsTest extends TckTestSupport {

  test("should accept valid side effects") {
    validateSideEffects(tableOf(Seq("+nodes", "1"), Seq("-relationships", "2"))) shouldBe None
  }

  test("should report invalid side effects") {
    val sideEffects = tableOf(Seq("+blargh", "1"), Seq("-relationships", "notanumber"))

    validateSideEffects(sideEffects) shouldBe Some(
        """Invalid side effect keys: +blargh
          |Invalid side effect values: notanumber""".stripMargin)
  }

  test("should allow all elements") {
    val allKeys = Seq("+nodes", "-nodes", "+relationships", "-relationships",
                      "+labels", "-labels", "+properties", "-properties")
    validateSideEffects.checkKeys(allKeys) shouldBe empty
  }

}
