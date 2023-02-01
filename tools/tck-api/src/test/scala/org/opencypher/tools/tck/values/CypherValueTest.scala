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

import scala.collection.SortedSet

class CypherValueTest extends AnyFunSuite with Matchers {

  test("list comparisons") {
    val oList1 = CypherOrderedList(List(CypherInteger(1), CypherInteger(2)))
    val oList2 = CypherOrderedList(List(CypherInteger(2), CypherInteger(1)))
    val uList1 = CypherUnorderedList(List(CypherInteger(2), CypherInteger(1)).sorted(CypherValue.ordering))
    val uList2 = CypherUnorderedList(List(CypherInteger(1), CypherInteger(2)).sorted(CypherValue.ordering))

    oList1 should equal(oList1)
    oList2 should equal(oList2)
    uList1 should equal(uList1)
    uList2 should equal(uList2)

    oList1 should not equal oList2
    oList2 should not equal oList1

    uList1 should equal(oList1)
    uList1 should equal(oList2)
    uList1 should equal(uList2)
    uList2 should equal(oList1)
    uList2 should equal(oList2)
    uList2 should equal(uList1)

    oList1 should equal(uList1)
    oList2 should equal(uList1)
    uList2 should equal(uList1)
    oList1 should equal(uList2)
    oList2 should equal(uList2)
    uList1 should equal(uList2)
  }

  test("list comparisons with strings") {
    val oList1 = CypherOrderedList(List(CypherString("age"), CypherString("name")))
    val oList2 = CypherOrderedList(List(CypherString("name"), CypherString("age")))
    val uList1 = CypherUnorderedList(List(CypherString("name"), CypherString("age")).sorted(CypherValue.ordering))
    val uList2 = CypherUnorderedList(List(CypherString("age"), CypherString("name")).sorted(CypherValue.ordering))

    oList1 should equal(oList1)
    oList2 should equal(oList2)
    uList1 should equal(uList1)
    uList2 should equal(uList2)

    oList1 should not equal oList2
    oList2 should not equal oList1

    uList1 should equal(oList1)
    uList1 should equal(oList2)
    uList1 should equal(uList2)
    uList2 should equal(oList1)
    uList2 should equal(oList2)
    uList2 should equal(uList1)

    oList1 should equal(uList1)
    oList2 should equal(uList1)
    uList2 should equal(uList1)
    oList1 should equal(uList2)
    oList2 should equal(uList2)
    uList1 should equal(uList2)
  }

  test("lists that are equal should have the same hashCode") {
    val oList = CypherOrderedList(List(CypherString("Foo")))
    val uList = CypherUnorderedList(List(CypherString("Foo")))

    oList should equal(uList)
    oList.hashCode() should equal(uList.hashCode())
  }

  test("list comparisons simple example") {
    val orderedItems1 = List(CypherString("name"), CypherString("age"), CypherString("address"))
    val orderedItems2 = List(CypherString("age"), CypherString("name"), CypherString("address"))
    val l1 = CypherUnorderedList(orderedItems1.sorted(CypherValue.ordering))
    val l2 = CypherOrderedList(orderedItems1)
    l1 should equal(l2)
    l2 should equal(l1)
  }

  test("node comparison with labelled nodes") {
    CypherNode(Set("A", "B")) should equal(CypherNode(Set("B", "A")))
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

    CypherOrderedList(nodeList1) should equal(CypherOrderedList(nodeList2))
    CypherOrderedList(nodeList2) should equal(CypherOrderedList(nodeList1))
    CypherOrderedList(nodeList1) should equal(CypherUnorderedList(nodeList2))
    CypherOrderedList(nodeList2) should equal(CypherUnorderedList(nodeList1))
    CypherOrderedList(nodeList1.reverse) should equal(CypherUnorderedList(nodeList2))
    CypherOrderedList(nodeList1) should equal(CypherUnorderedList(nodeList2.reverse))
    CypherOrderedList(nodeList1.reverse) should equal(CypherUnorderedList(nodeList1))
    CypherOrderedList(nodeList1) should equal(CypherUnorderedList(nodeList1.reverse))

    CypherOrderedList(nodeList1.reverse) should not equal(CypherOrderedList(nodeList2))
    CypherOrderedList(nodeList1) should not equal(CypherOrderedList(nodeList2.reverse))
    CypherOrderedList(nodeList1.reverse) should not equal(CypherOrderedList(nodeList1))
    CypherOrderedList(nodeList1) should not equal(CypherOrderedList(nodeList1.reverse))

    CypherUnorderedList(nodeList1) should equal(CypherUnorderedList(nodeList2))
    CypherUnorderedList(nodeList2) should equal(CypherUnorderedList(nodeList1))
    CypherUnorderedList(nodeList1) should equal(CypherOrderedList(nodeList2))
    CypherUnorderedList(nodeList2) should equal(CypherOrderedList(nodeList1))
    CypherUnorderedList(nodeList1.reverse) should equal(CypherUnorderedList(nodeList2))
    CypherUnorderedList(nodeList1) should equal(CypherUnorderedList(nodeList2.reverse))
    CypherUnorderedList(nodeList1.reverse) should equal(CypherOrderedList(nodeList2))
    CypherUnorderedList(nodeList1) should equal(CypherOrderedList(nodeList2.reverse))
    CypherUnorderedList(nodeList1.reverse) should equal(CypherUnorderedList(nodeList1))
    CypherUnorderedList(nodeList1) should equal(CypherUnorderedList(nodeList1.reverse))
    CypherUnorderedList(nodeList1.reverse) should equal(CypherOrderedList(nodeList1))
    CypherUnorderedList(nodeList1) should equal(CypherOrderedList(nodeList1.reverse))
  }

  test("list of lists comparison with labelled nodes") {
    CypherValue("[[(:A:D), (:B:C)], [(:AA:DD), (:BB:CC)]]", orderedLists = false) should
      equal(CypherValue("[[(:D:A), (:C:B)], [(:DD:AA), (:CC:BB)]]", orderedLists = false))

    CypherValue("[[(:A:D), (:B:C)], [(:AA:DD), (:BB:CC)]]", orderedLists = true) should
      equal(CypherValue("[[(:D:A), (:C:B)], [(:DD:AA), (:CC:BB)]]", orderedLists = true))

    CypherValue("[[(:AA:DD), (:BB:CC)], [(:A:D), (:B:C)]]", orderedLists = false) should
      equal(CypherValue("[[(:D:A), (:C:B)], [(:DD:AA), (:CC:BB)]]", orderedLists = false))

    CypherValue("[[(:AA:DD), (:BB:CC)], [(:A:D), (:B:C)]]", orderedLists = true) should
      not equal(CypherValue("[[(:D:A), (:C:B)], [(:DD:AA), (:CC:BB)]]", orderedLists = true))
  }
}
