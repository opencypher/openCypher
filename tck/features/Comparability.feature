# Copyright 2017 "Neo Technology",
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

  Scenario: Comparing strings and integers using > in an AND'd predicate
    Given an empty graph
    And having executed:
      """
      CREATE (root:Root)-[:T]->(:Child {id: 0}),
             (root)-[:T]->(:Child {id: 'xx'}),
             (root)-[:T]->(:Child)
      """
    When executing query:
      """
      MATCH (:Root)-->(i:Child)
      WHERE exists(i.id) AND i.id > 'x'
      RETURN i.id
      """
    Then the result should be:
      | i.id |
      | 'xx' |
    And no side effects

  Scenario: Comparing strings and integers using > in a OR'd predicate
    Given an empty graph
    And having executed:
      """
      CREATE (root:Root)-[:T]->(:Child {id: 0}),
             (root)-[:T]->(:Child {id: 'xx'}),
             (root)-[:T]->(:Child)
      """
    When executing query:
      """
      MATCH (:Root)-->(i:Child)
      WHERE NOT exists(i.id) OR i.id > 'x'
      RETURN i.id
      """
    Then the result should be:
      | i.id |
      | 'xx' |
      | null |
    And no side effects
