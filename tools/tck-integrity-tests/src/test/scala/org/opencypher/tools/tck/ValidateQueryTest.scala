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

import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

class ValidateQueryTest extends AnyFunSpecLike with Matchers {

  it("should accept keyword labels") {
    assertCorrect("CREATE (s1:Start {id: 1})")
  }

  it("should throw on bad styling") {
    assertIncorrect("match (n) return n", "MATCH (n) RETURN n")
  }

  it("should accept good styling") {
    assertCorrect("MATCH (n) RETURN n")
  }

  it("should request space after colon") {
    assertIncorrect("MATCH (n {name:'test'})", "MATCH (n {name: 'test'})")
  }

  it("should not request space after colon if it's a label") {
    assertCorrect("MATCH (n:Label)")
  }

  it("should not request space after colon if it's a relationship type") {
    assertCorrect("MATCH ()-[:T]-()")
  }

  it("should not request space after colon if it's a time") {
    assertCorrect("MATCH ({t: '12:00'})-[:T]-()")
  }

  it("should request space after comma") {
    assertIncorrect("WITH [1,2,3] AS list RETURN list,list", "WITH [1, 2, 3] AS list RETURN list, list")
  }

  it("should accept space after comma when present") {
    assertCorrect("WITH [1, 2, 3] AS list RETURN list, list")
  }

  it("should not request space after comma when line breaks") {
    assertCorrect("""MATCH (a),
                    |(b)
                    |RETURN 1
                    """.stripMargin)
  }

  it("should not request space after comma when inside a string") {
    assertCorrect("WITH ',' AS string RETURN string")
  }

  ignore("should request space after comma also when a string is present in the query") {
    assertIncorrect("WITH '' AS string RETURN string,string", "WITH '' AS string RETURN string, string")
  }

  it("should request single quotes for literal strings") {
    assertIncorrect("WITH \"string\" AS string", "WITH 'string' AS string")
  }

  it("should not request single quotes for literal strings that contain single quotes") {
    assertCorrect("WITH \"string that has ' within it\" AS string")
  }

  it("should get correct casing for null") {
    assertIncorrect("WITH Null AS n", "WITH null AS n")
    assertCorrect("WITH null AS n")
  }

  it("should not accept lower case IS NULL") {
    assertIncorrect("MATCH (n) WHERE n.prop is null RETURN n", "MATCH (n) WHERE n.prop IS NULL RETURN n")
    assertCorrect("MATCH (n) WHERE n.prop IS NULL RETURN n")
    assertIncorrect("MATCH (n) WHERE n.prop is not null RETURN n", "MATCH (n) WHERE n.prop IS NOT NULL RETURN n")
    assertCorrect("MATCH (n) WHERE n.prop IS NOT NULL RETURN n")
  }

  it("no illegal words accepted") {
    assertIncorrect("LOAD CSV", " ")
    assertIncorrect("CREATE CONSTRAINT ON", "CREATE  ON")
  }

  private def assertCorrect(query: String) = {
    query shouldBe NormalizeQueryStyle(query)
  }

  private def assertIncorrect(query: String, normalized: String) = {
    query should not be NormalizeQueryStyle(query)
    NormalizeQueryStyle(query) shouldBe normalized
  }
}
