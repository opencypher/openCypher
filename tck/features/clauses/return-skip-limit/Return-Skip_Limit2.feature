#
# Copyright (c) 2015-2020 "Neo Technology,"
# Network Engine for Objects in Lund AB [http://neotechnology.com]
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Attribution Notice under the terms of the Apache License 2.0
#
# This work was created by the collective efforts of the openCypher community.
# Without limiting the terms of Section 6, any Derivative Work that is not
# approved by the public consensus process of the openCypher Implementers Group
# should not be described as “Cypher” (and Cypher® is a registered trademark of
# Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
# proposals for change that have been documented or implemented should only be
# described as "implementation extensions to Cypher" or as "proposed changes to
# Cypher that are not yet approved by the openCypher community".
#

#encoding: utf-8

Feature: Return-Skip-Limit 2 - Limit

  Scenario: Limit to two hits
    Given an empty graph
    When executing query:
      """
      UNWIND [1, 1, 1, 1, 1] AS i
      RETURN i
      LIMIT 2
      """
    Then the result should be, in any order:
      | i |
      | 1 |
      | 1 |
    And no side effects

  Scenario: Limit to two hits with explicit order
    Given an empty graph
    And having executed:
      """
      CREATE ({name: 'A'}),
        ({name: 'B'}),
        ({name: 'C'}),
        ({name: 'D'}),
        ({name: 'E'})
      """
    When executing query:
      """
      MATCH (n)
      RETURN n
      ORDER BY n.name ASC
      LIMIT 2
      """
    Then the result should be, in any order:
      | n             |
      | ({name: 'A'}) |
      | ({name: 'B'}) |
    And no side effects

  Scenario: LIMIT 0 should return an empty result
    Given an empty graph
    And having executed:
      """
      CREATE (), (), ()
      """
    When executing query:
      """
      MATCH (n)
      RETURN n
        LIMIT 0
      """
    Then the result should be, in any order:
      | n |
    And no side effects

  Scenario: Handle ORDER BY with LIMIT 1
    And having executed:
      """
      CREATE (s:Person {name: 'Steven'}),
        (c:Person {name: 'Craig'})
      """
    When executing query:
      """
      MATCH (p:Person)
      RETURN p.name AS name
      ORDER BY p.name
      LIMIT 1
      """
    Then the result should be, in order:
      | name    |
      | 'Craig' |
    And no side effects

  Scenario: ORDER BY with LIMIT 0 should not generate errors
    When executing query:
      """
      MATCH (p:Person)
      RETURN p.name AS name
      ORDER BY p.name
      LIMIT 0
      """
    Then the result should be, in order:
      | name |
    And no side effects

  Scenario: LIMIT with an expression that does not depend on variables
    Given any graph
    And having executed:
      """
      UNWIND range(1, 3) AS i
      CREATE ({nr: i})
      """
    When executing query:
      """
      MATCH (n)
      WITH n LIMIT toInteger(ceil(1.7))
      RETURN count(*) AS count
      """
    Then the result should be, in any order:
      | count |
      | 2     |
    And no side effects

  @NegativeTest
  Scenario: LIMIT with an expression that depends on variables should fail
    Given any graph
    When executing query:
      """
      MATCH (n) RETURN n LIMIT n.count
      """
    Then a SyntaxError should be raised at compile time: NonConstantExpression

  @NegativeTest
  Scenario: Negative parameter for LIMIT should fail
    Given any graph
    And having executed:
      """
      CREATE (s:Person {name: 'Steven'}),
             (c:Person {name: 'Craig'})
      """
    And parameters are:
      | _limit | -1 |
    When executing query:
      """
      MATCH (p:Person)
      RETURN p.name AS name
      LIMIT $_limit
      """
    Then a SyntaxError should be raised at runtime: NegativeIntegerArgument

  @NegativeTest
  Scenario: Negative parameter for LIMIT with ORDER BY should fail
    Given any graph
    And having executed:
      """
      CREATE (s:Person {name: 'Steven'}),
             (c:Person {name: 'Craig'})
      """
    And parameters are:
      | _limit | -1 |
    When executing query:
      """
      MATCH (p:Person)
      RETURN p.name AS name
      ORDER BY name LIMIT $_limit
      """
    Then a SyntaxError should be raised at runtime: NegativeIntegerArgument

  @NegativeTest
  Scenario: Negative LIMIT should fail
    Given any graph
    And having executed:
      """
      CREATE (s:Person {name: 'Steven'}),
             (c:Person {name: 'Craig'})
      """
    When executing query:
      """
      MATCH (p:Person)
      RETURN p.name AS name
      LIMIT -1
      """
    Then a SyntaxError should be raised at compile time: NegativeIntegerArgument

  @NegativeTest
  Scenario: Floating point parameter for LIMIT should fail
    Given any graph
    And having executed:
      """
      CREATE (s:Person {name: 'Steven'}),
             (c:Person {name: 'Craig'})
      """
    And parameters are:
      | _limit | 1.5 |
    When executing query:
      """
      MATCH (p:Person)
      RETURN p.name AS name
      LIMIT $_limit
      """
    Then a SyntaxError should be raised at runtime: InvalidArgumentType

  @NegativeTest
  Scenario: Floating point parameter for LIMIT with ORDER BY should fail
    Given any graph
    And having executed:
      """
      CREATE (s:Person {name: 'Steven'}),
             (c:Person {name: 'Craig'})
      """
    And parameters are:
      | _limit | 1.5 |
    When executing query:
      """
      MATCH (p:Person)
      RETURN p.name AS name
      ORDER BY name LIMIT $_limit
      """
    Then a SyntaxError should be raised at runtime: InvalidArgumentType

  @NegativeTest
  Scenario: Floating point LIMIT should fail
    Given any graph
    And having executed:
      """
      CREATE (s:Person {name: 'Steven'}),
             (c:Person {name: 'Craig'})
      """
    When executing query:
      """
      MATCH (p:Person)
      RETURN p.name AS name
      LIMIT 1.5
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType
