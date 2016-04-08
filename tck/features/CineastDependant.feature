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

@db:cineast
Feature: CineastDependant

  Background:
    Given the cineast graph

  Scenario: Make query from existing database
    When executing query: MATCH (n) RETURN count(n)
    Then the result should be:
      | count(n) |
      | 63084    |
    And no side effects

  Scenario: Support multiple divisions in aggregate function
    When executing query: MATCH (n) RETURN count(n)/60/60 AS count
    Then the result should be:
      | count |
      | 17    |
    And no side effects

  Scenario: Support column renaming for aggregates as well
    When executing query: MATCH (a) WHERE id(a) = 0 RETURN count(*) AS ColumnName
    Then the result should be:
      | ColumnName |
      | 1          |
    And no side effects

  Scenario: Run coalesce
    When executing query: MATCH (a) WHERE id(a) = 0 RETURN coalesce(a.title, a.name)
    Then the result should be:
      | coalesce(a.title, a.name) |
      | 'Emil Eifrem'             |
    And no side effects

  Scenario: Allow addition
    When executing query: MATCH (a) WHERE id(a) = 61263 RETURN a.version + 5
    Then the result should be:
      | a.version + 5 |
      | 1863          |
    And no side effects

  Scenario: Functions should return null if they get path containing unbound
    When executing query: MATCH (a) WHERE id(a) = 1 OPTIONAL MATCH p=(a)-[r]->() RETURN length(nodes(p)), id(r), type(r), nodes(p), rels(p)
    Then the result should be:
      | length(nodes(p)) | id(r) | type(r) | nodes(p) | rels(p) |
      | null             | null  | null    | null     | null    |
    And no side effects

  Scenario: Aggregates inside normal functions
    When executing query: MATCH (a) RETURN length(collect(a))
    Then the result should be:
      | length(collect(a)) |
      | 63084              |
    And no side effects
