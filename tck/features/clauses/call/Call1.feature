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

Feature: Call1 - Procedures without arguments

  Scenario: Standalone call to VOID procedure that takes no arguments
    Given an empty graph
    And there exists a procedure test.doNothing() :: VOID:
      |
    When executing query:
      """
      CALL test.doNothing()
      """
    Then the result should be empty
    And no side effects

  Scenario: Standalone call to VOID procedure that takes no arguments, called with implicit arguments
    Given an empty graph
    And there exists a procedure test.doNothing() :: VOID:
      |
    When executing query:
      """
      CALL test.doNothing
      """
    Then the result should be empty
    And no side effects

  Scenario: In-query call to VOID procedure that takes no arguments
    Given an empty graph
    And there exists a procedure test.doNothing() :: VOID:
      |
    When executing query:
      """
      MATCH (n)
      CALL test.doNothing()
      RETURN n
      """
    Then the result should be, in any order:
      | n |
    And no side effects

  Scenario: In-query call to VOID procedure does not consume rows
    Given an empty graph
    And there exists a procedure test.doNothing() :: VOID:
      |
    And having executed:
      """
      CREATE (:A {name: 'a'})
      CREATE (:B {name: 'b'})
      CREATE (:C {name: 'c'})
      """
    When executing query:
      """
      MATCH (n)
      CALL test.doNothing()
      RETURN n.name AS `name`
      """
    Then the result should be, in any order:
      | name |
      | 'a'  |
      | 'b'  |
      | 'c'  |
    And no side effects

  Scenario: Standalone call to procedure that takes no arguments and yields no results
    Given an empty graph
    And there exists a procedure test.doNothing() :: ():
      |
    When executing query:
      """
      CALL test.doNothing()
      """
    Then the result should be empty
    And no side effects

  Scenario: Standalone call to procedure that takes no arguments and yields no results, called with implicit arguments
    Given an empty graph
    And there exists a procedure test.doNothing() :: ():
      |
    When executing query:
      """
      CALL test.doNothing
      """
    Then the result should be empty
    And no side effects

  Scenario: In-query call to procedure that takes no arguments and yields no results
    Given an empty graph
    And there exists a procedure test.doNothing() :: ():
      |
    When executing query:
      """
      CALL test.doNothing() YIELD - RETURN 1
      """
    Then the result should be, in any order:
      | 1 |
    And no side effects


  Scenario: Standalone call to STRING procedure that takes no arguments
    Given an empty graph
    And there exists a procedure test.labels() :: (label :: STRING?):
      | label |
      | 'A'   |
      | 'B'   |
      | 'C'   |
    When executing query:
      """
      CALL test.labels()
      """
    Then the result should be, in order:
      | label |
      | 'A'   |
      | 'B'   |
      | 'C'   |
    And no side effects

  Scenario: In-query call to STRING procedure that takes no arguments
    Given an empty graph
    And there exists a procedure test.labels() :: (label :: STRING?):
      | label |
      | 'A'   |
      | 'B'   |
      | 'C'   |
    When executing query:
      """
      CALL test.labels() YIELD label
      RETURN label
      """
    Then the result should be, in order:
      | label |
      | 'A'   |
      | 'B'   |
      | 'C'   |
    And no side effects
