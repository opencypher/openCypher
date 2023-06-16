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
package org.opencypher.tools.tck.values

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CypherValueTest extends AnyFunSuite with Matchers {

  test("list comparisons") {
    val oList1 = CypherOrderedList(List(CypherInteger(1), CypherInteger(2)))
    val oList2 = CypherOrderedList(List(CypherInteger(2), CypherInteger(1)))
    val uList1 = CypherUnorderedList(List(CypherInteger(2), CypherInteger(1)).sorted(CypherValue.ordering))
    val uList2 = CypherUnorderedList(List(CypherInteger(1), CypherInteger(2)).sorted(CypherValue.ordering))

    oList1 should not equal oList2
    oList2 should not equal oList1

    assertReallyEqual(uList1, oList1)
    assertReallyEqual(uList1, oList2)
    assertReallyEqual(uList1, uList2)
    assertReallyEqual(uList2, oList1)
    assertReallyEqual(uList2, oList2)
    assertReallyEqual(uList2, uList1)
  }

  test("list comparisons with strings") {
    val oList1 = CypherOrderedList(List(CypherString("age"), CypherString("name")))
    val oList2 = CypherOrderedList(List(CypherString("name"), CypherString("age")))
    val uList1 = CypherUnorderedList(List(CypherString("name"), CypherString("age")).sorted(CypherValue.ordering))
    val uList2 = CypherUnorderedList(List(CypherString("age"), CypherString("name")).sorted(CypherValue.ordering))

    oList1 should not equal oList2
    oList2 should not equal oList1

    assertReallyEqual(uList1, oList1)
    assertReallyEqual(uList1, oList2)
    assertReallyEqual(uList1, uList2)
    assertReallyEqual(uList2, oList1)
    assertReallyEqual(uList2, oList2)
  }

  test("lists that are equal should have the same hashCode") {
    val oList = CypherOrderedList(List(CypherString("Foo")))
    val uList = CypherUnorderedList(List(CypherString("Foo")))

    assertReallyEqual(oList, uList)
  }

  test("lists that are equal should really have the same hashCode") {
    val a = CypherValue("['Carl', 'Bob']", orderedLists = false)
    val b = CypherValue("['Bob', 'Carl']")
    assertReallyEqual(a, b)
  }

  test("list comparisons simple example") {
    val orderedItems1 = List(CypherString("name"), CypherString("age"), CypherString("address"))
    val orderedItems2 = List(CypherString("age"), CypherString("name"), CypherString("address"))
    val l1 = CypherUnorderedList(orderedItems1.sorted(CypherValue.ordering))
    val l2 = CypherOrderedList(orderedItems1)
    assertReallyEqual(l1, l2)
  }

  test("node comparison with labelled nodes") {
    assertReallyEqual(CypherNode(Set("A", "B")), CypherNode(Set("B", "A")))
    CypherNode(Set("A", "C")) should not equal(CypherNode(Set("A", "B")))
  }

  test("list comparison with labelled nodes") {
    val nodeList1 = List(
      CypherNode(scala.collection.immutable.SortedSet("A", "D")),
      CypherNode(scala.collection.immutable.SortedSet("B", "C"))
    )
    val nodeList2 = List(
      CypherNode(scala.collection.immutable.SortedSet("D", "A")(Ordering.String.reverse)),
      CypherNode(scala.collection.immutable.SortedSet("C", "B")(Ordering.String.reverse))
    )

    assertReallyEqual(CypherOrderedList(nodeList1), CypherOrderedList(nodeList2))
    assertReallyEqual(CypherOrderedList(nodeList1), CypherUnorderedList(nodeList2))
    assertReallyEqual(CypherOrderedList(nodeList1.reverse), CypherUnorderedList(nodeList2))
    assertReallyEqual(CypherOrderedList(nodeList1), CypherUnorderedList(nodeList2.reverse))
    assertReallyEqual(CypherOrderedList(nodeList1.reverse), CypherUnorderedList(nodeList1))
    assertReallyEqual(CypherOrderedList(nodeList1), CypherUnorderedList(nodeList1.reverse))

    CypherOrderedList(nodeList1.reverse) should not equal(CypherOrderedList(nodeList2))
    CypherOrderedList(nodeList1) should not equal(CypherOrderedList(nodeList2.reverse))
    CypherOrderedList(nodeList1.reverse) should not equal(CypherOrderedList(nodeList1))
    CypherOrderedList(nodeList1) should not equal(CypherOrderedList(nodeList1.reverse))

    assertReallyEqual(CypherUnorderedList(nodeList1), CypherUnorderedList(nodeList2))
    assertReallyEqual(CypherUnorderedList(nodeList1), CypherOrderedList(nodeList2))
    assertReallyEqual(CypherUnorderedList(nodeList2), CypherOrderedList(nodeList1))
    assertReallyEqual(CypherUnorderedList(nodeList1.reverse), CypherUnorderedList(nodeList2))
    assertReallyEqual(CypherUnorderedList(nodeList1), CypherUnorderedList(nodeList2.reverse))
    assertReallyEqual(CypherUnorderedList(nodeList1.reverse), CypherOrderedList(nodeList2))
    assertReallyEqual(CypherUnorderedList(nodeList1), CypherOrderedList(nodeList2.reverse))
    assertReallyEqual(CypherUnorderedList(nodeList1.reverse), CypherUnorderedList(nodeList1))
    assertReallyEqual(CypherUnorderedList(nodeList1), CypherUnorderedList(nodeList1.reverse))
    assertReallyEqual(CypherUnorderedList(nodeList1.reverse), CypherOrderedList(nodeList1))
    assertReallyEqual(CypherUnorderedList(nodeList1), CypherOrderedList(nodeList1.reverse))
  }

  test("list of lists comparison with labelled nodes") {
    assertReallyEqual(
      CypherValue("[[(:A:D), (:B:C)], [(:AA:DD), (:BB:CC)]]", orderedLists = false),
      CypherValue("[[(:D:A), (:C:B)], [(:DD:AA), (:CC:BB)]]", orderedLists = false)
    )
    assertReallyEqual(
      CypherValue("[[(:A:D), (:B:C)], [(:AA:DD), (:BB:CC)]]", orderedLists = true),
      CypherValue("[[(:D:A), (:C:B)], [(:DD:AA), (:CC:BB)]]", orderedLists = false)
    )
    assertReallyEqual(
      CypherValue("[[(:A:D), (:B:C)], [(:AA:DD), (:BB:CC)]]", orderedLists = true),
      CypherValue("[[(:D:A), (:C:B)], [(:DD:AA), (:CC:BB)]]", orderedLists = true)
    )
    assertReallyEqual(
      CypherValue("[[(:AA:DD), (:BB:CC)], [(:A:D), (:B:C)]]", orderedLists = false),
      CypherValue("[[(:D:A), (:C:B)], [(:DD:AA), (:CC:BB)]]", orderedLists = false)
    )
    assertReallyEqual(
      CypherValue("[[(:AA:DD), (:BB:CC)], [(:A:D), (:B:C)]]", orderedLists = false),
      CypherValue("[[(:D:A), (:C:B)], [(:DD:AA), (:CC:BB)]]", orderedLists = true)
    )
    CypherValue("[[(:AA:DD), (:BB:CC)], [(:A:D), (:B:C)]]", orderedLists = true) should
      not equal(CypherValue("[[(:D:A), (:C:B)], [(:DD:AA), (:CC:BB)]]", orderedLists = true))
  }

  test("nodes with properties") {
    assertReallyEqual(
      CypherValue("[(:A {a: 1, b: 2}), (:A {b: 4, a: 3})]", orderedLists = true),
      CypherValue("[(:A {b: 2, a: 1}), (:A {a: 3, b: 4})]", orderedLists = true),
    )
    assertReallyEqual(
      CypherValue("[(:A {a: 1, b: 2}), (:A {b: 4, a: 3})]", orderedLists = false),
      CypherValue("[(:A {a: 3, b: 4}), (:A {b: 2, a: 1})]", orderedLists = false),
    )
  }

  test("relationships with properties") {
    assertReallyEqual(
      CypherValue("[[:A {a: 1, b: 2}], [:A {b: 4, a: 3}]]", orderedLists = true),
      CypherValue("[[:A {b: 2, a: 1}], [:A {a: 3, b: 4}]]", orderedLists = true),
    )
    assertReallyEqual(
      CypherValue("[[:A {a: 1, b: 2}], [:A {b: 4, a: 3}]]", orderedLists = false),
      CypherValue("[[:A {a: 3, b: 4}], [:A {b: 2, a: 1}]]", orderedLists = false),
    )
  }

  test("maps") {
    assertReallyEqual(
      CypherValue("[{a: 1, b: 2}, {b: 4, a: 3}]", orderedLists = true),
      CypherValue("[{b: 2, a: 1}, {a: 3, b: 4}]", orderedLists = true),
    )
    assertReallyEqual(
      CypherValue("[{a: 1, b: 2}, {b: 4, a: 3}]", orderedLists = false),
      CypherValue("[{a: 3, b: 4}, {b: 2, a: 1}]", orderedLists = false),
    )
  }
  private def assertReallyEqual(a: CypherValue, b: CypherValue): Unit = {
    a shouldBe a
    b shouldBe b
    a shouldBe b
    b shouldBe a
    a.hashCode() shouldBe b.hashCode()
  }
}
