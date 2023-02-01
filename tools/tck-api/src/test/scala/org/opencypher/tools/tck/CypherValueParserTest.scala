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

import org.opencypher.tools.tck.values._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CypherValueParserTest extends AnyFunSuite with Matchers {

  test("unlabelled node") {
    val string = "()"
    val parsed = CypherValue(string)
    val expected = CypherNode(Set.empty[String], CypherPropertyMap(Map.empty))
    parsed should equal(expected)
  }

  test("node") {
    val string = "(:A {name: 'Hans'})"
    val parsed = CypherValue(string)
    val expected = CypherNode(Set("A"), CypherPropertyMap(Map("name" -> CypherString("Hans"))))
    parsed should equal(expected)
  }

  test("relationship") {
    val string = "[:A {since: 1920}]"
    val parsed = CypherValue(string)
    val expected = CypherRelationship("A", CypherPropertyMap(Map("since" -> CypherInteger(1920))))
    parsed should equal(expected)
  }

  test("scalars") {
    CypherValue("true") should equal(CypherBoolean(true))
    CypherValue("false") should equal(CypherBoolean(false))
    CypherValue("-1") should equal(CypherInteger(-1))
    CypherValue("-1.0") should equal(CypherFloat(-1.0))
    CypherValue("'true'") should equal(CypherString("true"))
    CypherValue("''") should equal(CypherString(""))
    CypherValue("'-1'") should equal(CypherString("-1"))
    CypherValue("null") should equal(CypherNull)
    CypherValue("NaN") should equal(CypherNaN)
  }

  test("string escaping") {
    CypherValue("'The Devil\\'s Advocate'") should equal(CypherString("The Devil's Advocate"))
    CypherValue("'\\\\'") should equal(CypherString("\\"))
    CypherValue("'\\''") should equal(CypherString("'"))
    CypherValue("'\\'\\''") should equal(CypherString("''"))
  }

  test("incorrect escaping") {
    (the[CypherValueParseException] thrownBy CypherValue("'\\'")).expected should equal("\"'\"")
    (the[CypherValueParseException] thrownBy CypherValue("'''")).expected should equal("end-of-input")
  }

  test("floats in exponent form") {
    CypherValue(".4e10") should equal(CypherFloat(.4e10))
    CypherValue(".4e-10") should equal(CypherFloat(.4e-10))
    CypherValue("-.4e-10") should equal(CypherFloat(-.4e-10))
    CypherValue("-1e-10") should equal(CypherFloat(-1e-10))
    CypherValue("8e10") should equal(CypherFloat(8e10))
    CypherValue("8.12312e2") should equal(CypherFloat(8.12312e2))
    CypherValue("-1.0E-9") should equal(CypherFloat(-1e-9))
    CypherValue("-1E-9") should equal(CypherFloat(-1e-9))
  }

  test("path with a single node") {
    val string = "<()>"
    val parsed = CypherValue(string)
    val expected = CypherPath(CypherNode())
    parsed should equal(expected)
  }

  test("complex path") {
    val string = "<({a: true})-[:R]->(:A)<-[:T {b: 'true'}]-()>"
    val parsed = CypherValue(string)
    val expected = CypherPath(
      CypherNode(properties = CypherPropertyMap(Map("a" -> CypherBoolean(true)))),
      List(
        Forward(
          CypherRelationship("R"),
          CypherNode(Set("A"))),
        Backward(
          CypherRelationship("T", CypherPropertyMap(Map("b" -> CypherString("true")))),
          CypherNode())
      )
    )
    parsed should equal(expected)
  }

  test("map") {
    CypherValue("{}") should equal(CypherPropertyMap())
    CypherValue("{name: 'Hello', foo: true}") should equal(
      CypherPropertyMap(Map("name" -> CypherString("Hello"), "foo" -> CypherBoolean(true))))
  }

  test("list") {
    CypherValue("[]") should equal(CypherOrderedList())
    CypherValue("[1, null, 2]") should equal(
      CypherOrderedList(List(CypherInteger(1), CypherNull, CypherInteger(2))))
    CypherValue("[1, 2, null]") should equal(
      CypherOrderedList(List(CypherInteger(1), CypherInteger(2), CypherNull)))
    CypherValue("[]", orderedLists = false) should equal(CypherUnorderedList())
    CypherValue("[2, 1]", orderedLists = false) should equal(CypherUnorderedList(List(CypherInteger(2), CypherInteger(1)).sorted(CypherValue.ordering)))
    CypherValue("[1, 2, null]", orderedLists = false) should equal(
      CypherUnorderedList(List(CypherInteger(1), CypherInteger(2), CypherNull).sorted(CypherValue.ordering)))
    CypherValue("['address', 'name', 'age']", orderedLists = false) should equal(
      CypherOrderedList(List(CypherString("name"), CypherString("age"), CypherString("address")))
    )
  }

}
