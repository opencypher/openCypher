#
# Copyright (c) 2015-2018 "Neo Technology,"
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

Feature: Function2 - Negative tests

  Scenario Outline: `toBoolean()` on invalid types
    Given any graph
    When executing query:
      """
      WITH [true, <invalid>] AS list
      RETURN toBoolean(list[1]) AS b
      """
    Then a TypeError should be raised at runtime: InvalidArgumentValue

    Examples:
      | invalid |
      | []      |
      | {}      |
      | 1       |
      | 1.0     |

  Scenario Outline: `toInteger()` failing on invalid arguments
    Given an empty graph
    And having executed:
      """
      CREATE ()-[:T]->()
      """
    When executing query:
      """
      MATCH p = (n)-[r:T]->()
      RETURN [x IN [1, <invalid>] | toInteger(x) ] AS list
      """
    Then a TypeError should be raised at runtime: InvalidArgumentValue

    Examples:
      | invalid |
      | true    |
      | []      |
      | {}      |
      | n       |
      | r       |
      | p       |

  Scenario Outline: `toFloat()` failing on invalid arguments
    Given an empty graph
    And having executed:
      """
      CREATE ()-[:T]->()
      """
    When executing query:
      """
      MATCH p = (n)-[r:T]->()
      RETURN [x IN [1.0, <invalid>] | toFloat(x) ] AS list
      """
    Then a TypeError should be raised at runtime: InvalidArgumentValue

    Examples:
      | invalid |
      | true    |
      | []      |
      | {}      |
      | n       |
      | r       |
      | p       |

  Scenario Outline: `toString()` failing on invalid arguments
    Given an empty graph
    And having executed:
      """
      CREATE ()-[:T]->()
      """
    When executing query:
      """
      MATCH p = (n)-[r:T]->()
      RETURN [x IN [1, '', <invalid>] | toString(x) ] AS list
      """
    Then a TypeError should be raised at runtime: InvalidArgumentValue

    Examples:
      | invalid |
      | []      |
      | {}      |
      | n       |
      | r       |
      | p       |
