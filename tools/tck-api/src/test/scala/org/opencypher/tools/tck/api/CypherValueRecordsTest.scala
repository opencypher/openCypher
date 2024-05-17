/*
 * Copyright (c) 2015-2024 "Neo Technology,"
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
package org.opencypher.tools.tck.api

import org.opencypher.tools.tck.values.CypherString
import org.opencypher.tools.tck.values.CypherValue
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CypherValueRecordsTest extends AnyFunSuite with Matchers {

  test("compare rows with equal hash code") {
    val rows = List(Map("a" -> CypherString("Aa")), Map("a" -> CypherString("BB")))
    val a = CypherValueRecords(List("a", "b"), rows)
    val b = CypherValueRecords(List("a", "b"), rows.reverse)

    rows.head.hashCode() shouldBe rows(1).hashCode() // If this fails, find another hash collision

    a.equalsUnordered(b) shouldBe true
  }

  test("compare rows with equal hash code 2") {
    val a = CypherValueRecords(List("a", "b"), List(Map("a" -> CypherString("Aa")), Map("a" -> CypherString("BB"))))
    val b = CypherValueRecords(List("a", "b"), List(Map("a" -> CypherString("BB")), Map("a" -> CypherString("BB"))))

    a.equalsUnordered(b) shouldBe false
  }

  test("compare unordered lists") {
    val a = records(List(
      Map("foo" -> CypherValue("['Ada', 'Danielle']", orderedLists = false)),
      Map("foo" -> CypherValue("['Carl']", orderedLists = false)),
      Map("foo" -> CypherValue("['Danielle']", orderedLists = false)),
      Map("foo" -> CypherValue("[]", orderedLists = false)),
      Map("foo" -> CypherValue("['Bob', 'Carl']", orderedLists = false))
    ))
    val b = records(List(
      Map("foo" -> CypherValue("['Carl', 'Bob']")),
      Map("foo" -> CypherValue("['Ada', 'Danielle']")),
      Map("foo" -> CypherValue("['Danielle']")),
      Map("foo" -> CypherValue("['Carl']")),
      Map("foo" -> CypherValue("[]"))
    ))

    a.equalsUnordered(b) shouldBe true
    b.equalsUnordered(a) shouldBe true
    a should not equal(b)
    b should not equal(a)
  }

  test("compare unordered lists 2") {
    val a = records(List(
      Map("foo" -> CypherValue("['Bob', 'Carl']", orderedLists = false)),
      Map("foo" -> CypherValue("['Ada', 'Danielle']", orderedLists = false)),
      Map("foo" -> CypherValue("['Danielle']", orderedLists = false)),
      Map("foo" -> CypherValue("['Carl']", orderedLists = false)),
      Map("foo" -> CypherValue("[]", orderedLists = false))
    ))
    val b = records(List(
      Map("foo" -> CypherValue("['Carl', 'Bob']")),
      Map("foo" -> CypherValue("['Ada', 'Danielle']")),
      Map("foo" -> CypherValue("['Danielle']")),
      Map("foo" -> CypherValue("['Carl']")),
      Map("foo" -> CypherValue("[]"))
    ))

    a.equalsUnordered(b) shouldBe true
    b.equalsUnordered(a) shouldBe true
    a shouldBe (b)
    b shouldBe (a)
  }

  test("compare lists of nodes with properties") {
    val a = records(List(
      Map(
        "x" -> "(:Person {city: 'Paris', name: 'Chris', age: 30})",
        "a" -> "[(:Person {city: 'Paris', name: 'Chris', age: 30})]",
        "r" -> "[[:K {weight: 2, id: 7}]]",
        "b" -> "[(:Person {city: 'Paris', name: 'Diana', age: 35})]",
        "y" -> "(:Person {city: 'Paris', name: 'Diana', age: 35})"
      ),
      Map(
        "x" -> "(:Person {city: 'Paris', name: 'Chris', age: 30})",
        "a" -> "[(:Person {city: 'Paris', name: 'Chris', age: 30})]",
        "r" -> "[[:E {weight: 3, id: 3}]]",
        "b" -> "[(:Person {city: 'Paris', name: 'Diana', age: 35})]",
        "y" -> "(:Person {city: 'Paris', name: 'Diana', age: 35})"
      ),
      Map(
        "x" -> "(:Person {city: 'Paris', name: 'Chris', age: 30})",
        "a" -> "[(:Person {city: 'Paris', name: 'Chris', age: 30}), (:Person {city: 'Paris', name: 'Diana', age: 35})]",
        "r" -> "[[:E {weight: 3, id: 3}], [:E {weight: 1, id: 4}]]",
        "b" -> "[(:Person {city: 'Paris', name: 'Diana', age: 35}), (:Person {city: 'London', name: 'Sue', age: 32})]",
        "y" -> "(:Person {city: 'London', name: 'Sue', age: 32})"
      ),
      Map(
        "x" -> "(:Person {city: 'Paris', name: 'Chris', age: 30})",
        "a" -> "[(:Person {city: 'Paris', name: 'Chris', age: 30}), (:Person {city: 'Paris', name: 'Diana', age: 35})]",
        "r" -> "[[:E {weight: 3, id: 3}], [:E {weight: 7, id: 5}]]",
        "b" -> "[(:Person {city: 'Paris', name: 'Diana', age: 35}), (:Person {city: 'Oslo', name: 'Tony', age: 40})]",
        "y" -> "(:Person {city: 'Oslo', name: 'Tony', age: 40})"
      ),
      Map(
        "x" -> "(:Person {city: 'Paris', name: 'Chris', age: 30})",
        "a" -> "[(:Person {city: 'Paris', name: 'Chris', age: 30}), (:Person {city: 'Paris', name: 'Diana', age: 35})]",
        "r" -> "[[:K {weight: 2, id: 7}], [:E {weight: 1, id: 4}]]",
        "b" -> "[(:Person {city: 'Paris', name: 'Diana', age: 35}), (:Person {city: 'London', name: 'Sue', age: 32})]",
        "y" -> "(:Person {city: 'London', name: 'Sue', age: 32})"
      ),
      Map(
        "x" -> "(:Person {city: 'Paris', name: 'Chris', age: 30})",
        "a" -> "[(:Person {city: 'Paris', name: 'Chris', age: 30}), (:Person {city: 'Paris', name: 'Diana', age: 35})]",
        "r" -> "[[:K {weight: 2, id: 7}], [:E {weight: 7, id: 5}]]",
        "b" -> "[(:Person {city: 'Paris', name: 'Diana', age: 35}), (:Person {city: 'Oslo', name: 'Tony', age: 40})]",
        "y" -> "(:Person {city: 'Oslo', name: 'Tony', age: 40})"
      )
    ).map(_.map { case (k, v) => k -> CypherValue(v)}))
    
    val b = records(List(
      Map(
        "x" -> "(:Person {name: 'Chris', city: 'Paris', age: 30})",
        "a" -> "[(:Person {name: 'Chris', city: 'Paris', age: 30})]",
        "r" -> "[[:K {weight: 2, id: 7}]]",
        "b" -> "[(:Person {name: 'Diana', city: 'Paris', age: 35})]",
        "y" -> "(:Person {name: 'Diana', city: 'Paris', age: 35})"
      ),
      Map(
        "x" -> "(:Person {name: 'Chris', city: 'Paris', age: 30})",
        "a" -> "[(:Person {name: 'Chris', city: 'Paris', age: 30})]",
        "r" -> "[[:E {weight: 3, id: 3}]]",
        "b" -> "[(:Person {name: 'Diana', city: 'Paris', age: 35})]",
        "y" -> "(:Person {name: 'Diana', city: 'Paris', age: 35})"
      ),
      Map(
        "x" -> "(:Person {name: 'Chris', city: 'Paris', age: 30})",
        "a" -> "[(:Person {name: 'Chris', city: 'Paris', age: 30}), (:Person {name: 'Diana', city: 'Paris', age: 35})]",
        "r" -> "[[:K {weight: 2, id: 7}], [:E {weight: 1, id: 4}]]",
        "b" -> "[(:Person {name: 'Diana', city: 'Paris', age: 35}), (:Person {name: 'Sue', city: 'London', age: 32})]",
        "y" -> "(:Person {name: 'Sue', city: 'London', age: 32})"
      ),
      Map(
        "x" -> "(:Person {name: 'Chris', city: 'Paris', age: 30})",
        "a" -> "[(:Person {name: 'Chris', city: 'Paris', age: 30}), (:Person {name: 'Diana', city: 'Paris', age: 35})]",
        "r" -> "[[:K {id: 7, weight: 2}], [:E {weight: 7, id: 5}]]",
        "b" -> "[(:Person {name: 'Diana', city: 'Paris', age: 35}), (:Person {name: 'Tony', city: 'Oslo', age: 40})]",
        "y" -> "(:Person {name: 'Tony', city: 'Oslo', age: 40})"
      ),
      Map(
        "x" -> "(:Person {name: 'Chris', city: 'Paris', age: 30})",
        "a" -> "[(:Person {name: 'Chris', city: 'Paris', age: 30}), (:Person {name: 'Diana', city: 'Paris', age: 35})]",
        "r" -> "[[:E {weight: 3, id: 3}], [:E {weight: 1, id: 4}]]",
        "b" -> "[(:Person {name: 'Diana', city: 'Paris', age: 35}), (:Person {name: 'Sue', city: 'London', age: 32})]",
        "y" -> "(:Person {name: 'Sue', city: 'London', age: 32})"
      ),
      Map(
        "x" -> "(:Person {name: 'Chris', city: 'Paris', age: 30})",
        "a" -> "[(:Person {name: 'Chris', city: 'Paris', age: 30}), (:Person {name: 'Diana', city: 'Paris', age: 35})]",
        "r" -> "[[:E {weight: 3, id: 3}], [:E {id: 5, weight: 7}]]",
        "b" -> "[(:Person {name: 'Diana', city: 'Paris', age: 35}), (:Person {name: 'Tony', city: 'Oslo', age: 40})]",
        "y" -> "(:Person {name: 'Tony', city: 'Oslo', age: 40})"
      )
    ).map(_.map { case (k, v) => k -> CypherValue(v)}))

    a.equalsUnordered(b) shouldBe true
    b.equalsUnordered(a) shouldBe true
  }

  private def records(columns: List[Map[String, CypherValue]]): CypherValueRecords = {
    val header = columns.flatMap(_.keySet).distinct
    CypherValueRecords(header, columns)
  }
}
