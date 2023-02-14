#
# Copyright (c) 2015-2023 "Neo Technology,"
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

Feature: Foo

  Scenario: Return literal
    Given an empty graph
    When executing query:
      """
      RETURN 1
      """
    Then the result should be, in any order:
      | 1 |
      | 1 |
    And no side effects

  Scenario: Reading a CSV
    Given an empty graph
    And there exists a CSV file with URL as $param, with rows:
      | i | txt |
      | 1 | 'a' |
      | 2 | 'b' |
    And parameters are:
      | list | [] |

    When executing query:
      """
      LOAD CSV WITH HEADERS from '$param' AS row
      RETURN row.txt AS res
      """
    Then the result should be, in any order:
      | res   |
      | 'a'   |
      | 'b'   |
    And no side effects

  Scenario: Fail
    Given an empty graph
    When executing query:
      """
      RETURN foo()
      """
    Then a SyntaxError should be raised at compile time: UnknownFunction

  Scenario: Fail with any type
    Given an empty graph
    When executing query:
      """
      RETURN foo()
      """
    Then an Error should be raised at compile time: UnknownFunction

  Scenario: Fail at any time
    Given an empty graph
    When executing query:
      """
      RETURN foo()
      """
    Then a SyntaxError should be raised at any time: UnknownFunction

  Scenario: Fail with any detail
    Given an empty graph
    When executing query:
      """
      RETURN foo()
      """
    Then a SyntaxError should be raised at compile time: *

  Scenario: Fail with any type and any detail at any type
    Given an empty graph
    When executing query:
      """
      RETURN foo()
      """
    Then an Error should be raised at any time: *

  @ignore
  Scenario: Ignored
    Given an unsupported step
    When executing query:
      """
      not really a query
      """

  Scenario: Do not fail init queries silently
    Given an empty graph
    And having executed:
      """
      FAIL
      """
    When executing query:
      """
      RETURN 1
      """
    Then the result should be, in any order:
      | 1 |
      | 1 |
    And no side effects
