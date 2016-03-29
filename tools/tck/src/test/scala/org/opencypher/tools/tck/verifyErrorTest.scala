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

class verifyErrorTest extends TckTestSupport {

  test("should accept valid error specification") {
    verifyError("TypeError", "runtime", "InvalidElementAccess") shouldBe None
  }

  test("should report invalid error specification") {
    verifyError("InvalidType", "unknown", "_-^&*") shouldBe Some(
      """Invalid error type: InvalidType
        |Invalid error phase: unknown
        |Invalid error detail: _-^&*""".stripMargin)
  }

}
