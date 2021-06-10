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

Feature: Map1 - Static value access
# Static value access refers to the dot-operator – <expression resulting in a map>.<identify> – which does not allow any dynamic computation of the map key – i.e. <identify>.

  Scenario: [1] Statically access a field of a map resulting from an expression
    Given any graph
    When executing query:
      """
      WITH [{num: 0}, 1] AS list
      RETURN (list[0]).num
      """
    Then the result should be, in any order:
      | (list[0]).num |
      | 0             |
    And no side effects

  Scenario: [2] Statically access a field on null results in null
    Given any graph
    When executing query:
      """
      RETURN null.num
      """
    Then the result should be, in any order:
      | null.num |
      | null     |
    And no side effects

  Scenario: [3] Statically access a field with null results in null
    Given any graph
    When executing query:
      """
      WITH {num: 0} AS map
      RETURN map.null
      """
    Then the result should be, in any order:
      | null.num |
      | null     |
    And no side effects

  Scenario Outline: [4] Statically access a field is case-sensitive
    Given any graph
    When executing query:
      """
      WITH <map> AS map
      RETURN map.<key> AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | map                            | key  | result   |
      | {name: 'Mats', nome: 'Pontus'} | name | 'Mats'   |
      | {name: 'Mats', Name: 'Pontus'} | name | 'Mats'   |
      | {name: 'Mats', Name: 'Pontus'} | Name | 'Pontus' |
      | {name: 'Mats', Name: 'Pontus'} | nAMe | null     |

  Scenario Outline: [5] Statically access a field with a delimited identifier
    Given any graph
    When executing query:
      """
      WITH <map> AS map
      RETURN map.<key> AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | map                            | key    | result   |
      | {name: 'Mats', nome: 'Pontus'} | `name` | 'Mats'   |
      | {name: 'Mats', nome: 'Pontus'} | `nome` | 'Pontus' |
      | {name: 'Mats', nome: 'Pontus'} | `Mats` | null     |
      | {name: 'Mats', nome: 'Pontus'} | `null` | null     |
      | {null: 'Mats', NULL: 'Pontus'} | `null` | 'Mats'   |
      | {null: 'Mats', NULL: 'Pontus'} | `NULL` | 'Pontus' |

  Scenario: [6] Fail when performing property access on a non-map
    Given any graph
    When executing query:
      """
      WITH [{num: 0}, 1] AS list
      RETURN (list[1]).num
      """
    Then a TypeError should be raised at runtime: PropertyAccessOnNonMap
