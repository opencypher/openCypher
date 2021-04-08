#
# Copyright (c) 2015-2021 "Neo Technology,"
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

Feature: Pattern2 - Number of Pattern Matches

  Scenario Outline: [1] Any pattern has zero matches on an empty graph
    Given an empty graph
    When executing query:
      """
      RETURN size(<pattern>) AS size
      """
    Then the result should be, in any order:
      | size |
      | 0    |
    And no side effects

    Examples:
      | pattern                            |
      | ()--()                             |
      | ()-->()                            |
      | ()<--()                            |
      | ()-[:REL]-()                       |
      | ()-[:REL]->()                      |
      | ()<-[:REL]-()                      |
      | ()--({})                           |
      | ()-->({})                          |
      | ({})-->({})                        |
      | (:A {})-[:REL]->(:B {num: 6})      |
      | ()-[:REL]->(:C)<-[:REL]-({num: 5}) |

  Scenario Outline: [2] Counting matches of a relationship pattern on an non-empty graph
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:T1]->(:B),
             (:B)-[:T2]->(:A),
             (:B)-[:T2]->(:B),
             (:B)-[:T3]->(:B),
             (:A)-[:T4]->(:A)
      """
    When executing query:
      """
      RETURN size(<pattern>) AS size
      """
    Then the result should be, in any order:
      | size   |
      | <size> |
    And no side effects

    Examples:
      | pattern          | size |
      | ()--()           | 10   |
      | ()-->()          | 5    |
      | ()<--()          | 5    |
      | ()-[:T2]-()      | 4    |
      | ()-[:T2]->()     | 2    |
      | ()<-[:T3]-()     | 1    |
      | (:B)-->()        | 3    |
      | (:B)-->(:B)      | 2    |
      | (:A)-->(:A)      | 1    |
      | (:B)-[:T2]->(:A) | 1    |

  # TODO: Scenario Outline for counting multi-hop patterns

  Scenario Outline: [3] Fail when pattern is a single node pattern
    Given any graph
    When executing query:
      """
      RETURN size(<pattern>)
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

    Examples:
      | pattern       |
      | ()            |
      | (:A)          |
      | ({})          |
      | ({num: 123})  |
      | (:A {})       |
      | (:A {num: 6}) |

  Scenario Outline: [4] Fail when pattern binds a new variable
    Given any graph
    When executing query:
      """
      RETURN size(<pattern>)
      """
    Then a SyntaxError should be raised at compile time: UndefinedVariable

    Examples:
      | pattern                                  |
      | (a)                                      |
      | (a:A)                                    |
      | (a {})                                   |
      | (a {num: 123})                           |
      | (a:A {})                                 |
      | (a:A {num: 6})                           |
      | ()--(a)                                  |
      | (a)-->()                                 |
      | (a)<--(a)                                |
      | (a)-[:REL]-()                            |
      | (a)-[:REL]->(b)                          |
      | (a)-[r:REL]->(b)                         |
      | ()<-[r:REL]-()                           |
      | ()<-[r]-()                               |
      | ()-[:REL]->(c:C)<-[:REL]-({num: 5})      |
      | (a)-[:REL]->(:C)<-[:REL]-(a {num: 5})    |
      | ()-[r:REL]->(:C)<-[s:REL]-({num: 5})     |
      | (a)-[r:REL]->(c:C)<-[s:REL]-(a {num: 5}) |
