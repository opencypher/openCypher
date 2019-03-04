/*
 * Copyright (c) 2015-2019 "Neo Technology,"
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

import java.time._

import org.opencypher.tools.tck.values._
import org.scalatest.{FunSuite, Matchers}

class CypherValueParserTest extends FunSuite with Matchers {

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
  }

  test("floats in exponent form") {
    CypherValue(".4e10") should equal(CypherFloat(.4e10))
    CypherValue(".4e-10") should equal(CypherFloat(.4e-10))
    CypherValue("-.4e-10") should equal(CypherFloat(-.4e-10))
    CypherValue("-1e-10") should equal(CypherFloat(-1e-10))
    CypherValue("8e10") should equal(CypherFloat(8e10))
    CypherValue("8.12312e2") should equal(CypherFloat(8.12312e2))
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
  }

  test("date") {
    CypherValue("2018-12-23") should equal(CypherDate(LocalDate.of(2018, 12, 23)))
    CypherValue("0002-01-21") should equal(CypherDate(LocalDate.of(2, 1, 21)))
  }

  test("localtime") {
    CypherValue("12:31:14.645876123") should equal(CypherLocalTime(LocalTime.of(12, 31, 14, 645876123)))
    CypherValue("12:31:14.0") should equal(CypherLocalTime(LocalTime.of(12, 31, 14, 0)))
    CypherValue("12:31:14") should equal(CypherLocalTime(LocalTime.of(12, 31, 14)))
    CypherValue("09:00:59") should equal(CypherLocalTime(LocalTime.of(9, 0, 59)))
  }

  test("localdatetime") {
    CypherValue("1984-10-11T12:31:14.645876123") should equal(CypherLocalDateTime(LocalDateTime.of(1984, 10, 11, 12, 31, 14, 645876123)))
    CypherValue("1984-10-11T12:31:14") should equal(CypherLocalDateTime(LocalDateTime.of(1984, 10, 11, 12, 31, 14)))
    CypherValue("0001-12-11T00:31:14.645876123") should equal(CypherLocalDateTime(LocalDateTime.of(1, 12, 11, 0, 31, 14, 645876123)))
  }

  test("time") {
    CypherValue("12:31:14.645876123+01:00") should equal(CypherTime(OffsetTime.of(12, 31, 14, 645876123, ZoneOffset.ofHours(1))))
    CypherValue("00:00:00+00:00") should equal(CypherTime(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC)))
    CypherValue("00:00:00-08:30") should equal(CypherTime(OffsetTime.of(0, 0, 0, 0, ZoneOffset.ofHoursMinutes(-8, -30))))
    CypherValue("23:59:59Z") should equal(CypherTime(OffsetTime.of(23, 59, 59, 0, ZoneOffset.UTC)))
  }

  test("datetime") {
    CypherValue("1984-10-11T12:31:14.645876123+01:00") should equal(CypherDateTime(OffsetDateTime.of(1984, 10, 11, 12, 31, 14, 645876123, ZoneOffset.ofHours(1))))
    CypherValue("0000-01-31T12:31:14-02:00") should equal(CypherDateTime(OffsetDateTime.of(0, 1, 31, 12, 31, 14, 0, ZoneOffset.ofHours(-2))))
    CypherValue("1234-12-12T00:00:00Z") should equal(CypherDateTime(OffsetDateTime.of(1234, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC)))
  }

}
