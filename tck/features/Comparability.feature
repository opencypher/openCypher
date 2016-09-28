#
# Copyright 2016 "Neo Technology",
# Network Engine for Objects in Lund AB (http://neotechnology.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

Feature: Comparability

  Scenario: Fail when comparing nodes to parameters
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    And parameters are:
      | param | 'foo' |
    When executing query:
      """
      MATCH (b)
      WHERE b = $param
      RETURN b
      """
    Then a TypeError should be raised at compile time: IncomparableValues

  Scenario: Fail when comparing parameters to nodes
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    And parameters are:
      | param | 'foo' |
    When executing query:
      """
      MATCH (b)
      WHERE $param = b
      RETURN b
      """
    Then a TypeError should be raised at compile time: IncomparableValues

  Scenario: Comparing nodes to properties
    Given an empty graph
    And having executed:
      """
      CREATE ({val: 17})
      """
    When executing query:
      """
      MATCH (a)
      WHERE a = a.val
      RETURN count(a)
      """
    Then a TypeError should be raised at compile time: IncomparableValues

  Scenario: Fail when comparing nodes to relationships
    Given an empty graph
    And having executed:
      """
      CREATE ()-[:T]->()
      """
    When executing query:
      """
      MATCH (a)-[b]->()
      RETURN a = b
      """
    Then a TypeError should be raised at compile time: IncomparableValues

  Scenario: Fail when comparing relationships to nodes
    Given an empty graph
    And having executed:
      """
      CREATE ()-[:T]->()
      """
    When executing query:
      """
      MATCH (a)-[b]->()
      RETURN b = a
      """
    Then a TypeError should be raised at compile time: IncomparableValues
