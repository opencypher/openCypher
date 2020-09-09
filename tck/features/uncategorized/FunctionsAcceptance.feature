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

Feature: FunctionsAcceptance

  # Consider splitting into one scenario per one of four function:nodes, relationships, types, and size.
  Scenario: Functions should return null if they get path containing unbound
    Given any graph
    When executing query:
      """
      WITH null AS a
      OPTIONAL MATCH p = (a)-[r]->()
      RETURN size(nodes(p)), type(r), nodes(p), relationships(p)
      """
    Then the result should be, in any order:
      | size(nodes(p)) | type(r) | nodes(p) | relationships(p) |
      | null           | null    | null     | null             |
    And no side effects

  Scenario: `properties()` on a map
    Given any graph
    When executing query:
      """
      RETURN properties({name: 'Popeye', level: 9001}) AS m
      """
    Then the result should be, in any order:
      | m                             |
      | {name: 'Popeye', level: 9001} |
    And no side effects

  Scenario: `properties()` failing on an integer literal
    Given any graph
    When executing query:
      """
      RETURN properties(1)
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

  Scenario: `properties()` failing on a string literal
    Given any graph
    When executing query:
      """
      RETURN properties('Cypher')
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

  Scenario: `properties()` failing on a list of booleans
    Given any graph
    When executing query:
      """
      RETURN properties([true, false])
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

  Scenario: `properties()` on null
    Given any graph
    When executing query:
      """
      RETURN properties(null)
      """
    Then the result should be, in any order:
      | properties(null) |
      | null             |
    And no side effects

  Scenario Outline: IS NOT NULL with literal maps
    Given any graph
    When executing query:
      """
      WITH <map> AS map
      RETURN map.name IS NOT NULL
      """
    Then the result should be, in any order:
      | map.name IS NOT NULL |
      | <result>             |
    And no side effects

    Examples:
      | map                             | result |
      | {name: 'Mats', name2: 'Pontus'} | true   |
      | {name: null}                    | false  |
      | {notName: 0, notName2: null}    | false  |

  Scenario Outline: `percentileDisc()`
    Given an empty graph
    And having executed:
      """
      CREATE ({price: 10.0}),
             ({price: 20.0}),
             ({price: 30.0})
      """
    And parameters are:
      | percentile | <p> |
    When executing query:
      """
      MATCH (n)
      RETURN percentileDisc(n.price, $percentile) AS p
      """
    Then the result should be, in any order:
      | p        |
      | <result> |
    And no side effects

    Examples:
      | p   | result |
      | 0.0 | 10.0   |
      | 0.5 | 20.0   |
      | 1.0 | 30.0   |

  Scenario Outline: `percentileCont()`
    Given an empty graph
    And having executed:
      """
      CREATE ({price: 10.0}),
             ({price: 20.0}),
             ({price: 30.0})
      """
    And parameters are:
      | percentile | <p> |
    When executing query:
      """
      MATCH (n)
      RETURN percentileCont(n.price, $percentile) AS p
      """
    Then the result should be, in any order:
      | p        |
      | <result> |
    And no side effects

    Examples:
      | p   | result |
      | 0.0 | 10.0   |
      | 0.5 | 20.0   |
      | 1.0 | 30.0   |

  Scenario Outline: `percentileCont()` failing on bad arguments
    Given an empty graph
    And having executed:
      """
      CREATE ({price: 10.0})
      """
    And parameters are:
      | param | <percentile> |
    When executing query:
      """
      MATCH (n)
      RETURN percentileCont(n.price, $param)
      """
    Then a ArgumentError should be raised at runtime: NumberOutOfRange

    Examples:
      | percentile |
      | 1000       |
      | -1         |
      | 1.1        |

  Scenario Outline: `percentileDisc()` failing on bad arguments
    Given an empty graph
    And having executed:
      """
      CREATE ({price: 10.0})
      """
    And parameters are:
      | param | <percentile> |
    When executing query:
      """
      MATCH (n)
      RETURN percentileDisc(n.price, $param)
      """
    Then a ArgumentError should be raised at runtime: NumberOutOfRange

    Examples:
      | percentile |
      | 1000       |
      | -1         |
      | 1.1        |

  Scenario: `percentileDisc()` failing in more involved query
    Given an empty graph
    And having executed:
      """
      UNWIND range(0, 10) AS i
      CREATE (s:S)
      WITH s, i
      UNWIND range(0, i) AS j
      CREATE (s)-[:REL]->()
      """
    When executing query:
      """
      MATCH (n:S)
      WITH n, size([(n)-->() | 1]) AS deg
      WHERE deg > 2
      WITH deg
      LIMIT 100
      RETURN percentileDisc(0.90, deg), deg
      """
    Then a ArgumentError should be raised at runtime: NumberOutOfRange

  Scenario: `labels()` should accept type Any
    Given an empty graph
    And having executed:
      """
      CREATE (:Foo), (:Foo:Bar)
      """
    When executing query:
      """
      MATCH (a)
      WITH [a, 1] AS list
      RETURN labels(list[0]) AS l
      """
    Then the result should be (ignoring element order for lists):
      | l              |
      | ['Foo']        |
      | ['Foo', 'Bar'] |
    And no side effects

  Scenario: `labels()` failing on a path
    Given an empty graph
    And having executed:
      """
      CREATE (:Foo), (:Foo:Bar)
      """
    When executing query:
      """
      MATCH p = (a)
      RETURN labels(p) AS l
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

  Scenario: `labels()` failing on invalid arguments
    Given an empty graph
    And having executed:
      """
      CREATE (:Foo), (:Foo:Bar)
      """
    When executing query:
      """
      MATCH (a)
      WITH [a, 1] AS list
      RETURN labels(list[1]) AS l
      """
    Then a TypeError should be raised at runtime: InvalidArgumentValue
