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

Feature: Call5 - Results projection

  Scenario: In-query call to procedure with explicit arguments that drops all result fields
    Given an empty graph
    And there exists a procedure test.my.proc(name :: STRING?, id :: INTEGER?) :: (city :: STRING?, country_code :: INTEGER?):
      | name     | id | city      | country_code |
      | 'Andres' | 1  | 'Malmö'   | 46           |
      | 'Tobias' | 1  | 'Malmö'   | 46           |
      | 'Mats'   | 1  | 'Malmö'   | 46           |
      | 'Stefan' | 1  | 'Berlin'  | 49           |
      | 'Stefan' | 2  | 'München' | 49           |
      | 'Petra'  | 1  | 'London'  | 44           |
    When executing query:
      """
      WITH 'Stefan' AS name, 1 AS id
      CALL test.my.proc(name, id) YIELD -
      RETURN name, id, count(*) AS count
      """
    Then the result should be, in order:
      | name     | id | count |
      | 'Stefan' | 1  | 1     |
    And no side effects