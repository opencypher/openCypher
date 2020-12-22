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

Feature: Merge9 - Merge clause interoperation with other clauses

  Scenario: [1] Unwind combined with merge
    Given an empty graph
    When executing query:
      """
      UNWIND [1, 2, 3, 4] AS int
      MERGE (n {id: int})
      RETURN count(*)
      """
    Then the result should be, in any order:
      | count(*) |
      | 4        |
    And the side effects should be:
      | +nodes      | 4 |
      | +properties | 4 |

  Scenario: [2] Mixing MERGE with CREATE
    Given an empty graph
    When executing query:
      """
      CREATE (a:A), (b:B)
      MERGE (a)-[:KNOWS]->(b)
      CREATE (b)-[:KNOWS]->(c:C)
      RETURN count(*)
      """
    Then the result should be, in any order:
      | count(*) |
      | 1        |
    And the side effects should be:
      | +nodes         | 3 |
      | +relationships | 2 |
      | +labels        | 3 |
