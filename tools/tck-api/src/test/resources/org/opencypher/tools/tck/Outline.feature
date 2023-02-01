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

Feature: Outline

  @numbered
  Scenario Outline: [1] Outline Test
    Given an empty graph
    When executing query:
      """
      RETURN <sample>
      """
    Then the result should be, in any order:
      | <column> |
      | <result> |
    And no side effects

    Examples:
      | sample | column | result |
      | 1      | 1      | 1      |
      | 2      | 2      | 2      |
      | 3      | 3      | 3      |

  @numbered
  Scenario Outline: [2] Outline Test with some and overlapping example name #Example: <exampleName>
    Given an empty graph
    When executing query:
      """
      RETURN <sample>
      """
    Then the result should be, in any order:
      | <column> |
      | <result> |
    And no side effects

    Examples:
      | sample | column | result | exampleName |
      | 1      | 1      | 1      | one         |
      | 2      | 2      | 2      |             |
      | 3      | 3      | 3      | threeFour   |
      | 4      | 4      | 4      | threeFour   |

  @numbered @fullyNamed
  Scenario Outline: [3] Outline Test with all example name #Example: <exampleName>
    Given an empty graph
    When executing query:
      """
      RETURN <sample>
      """
    Then the result should be, in any order:
      | <column> |
      | <result> |
    And no side effects

    Examples:
      | sample | column | result | exampleName |
      | 1      | 1      | 1      | one         |
      | 2      | 2      | 2      | two         |
      | 3      | 3      | 3      | three       |

  @numbered @fullyNamed
  Scenario Outline: [4] Outline Test with a single example #Example: <exampleName>
    Given an empty graph
    When executing query:
      """
      RETURN <sample>
      """
    Then the result should be, in any order:
      | <column> |
      | <result> |
    And no side effects

    Examples:
      | sample | column | result | exampleName |
      | 1      | 1      | 1      | one         |
