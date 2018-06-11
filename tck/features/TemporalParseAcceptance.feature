#
# Copyright (c) 2015-2018 "Neo Technology,"
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

Feature: TemporalParseAcceptance

  Scenario: Should parse date from string
    Given any graph
    When executing query:
      """
      UNWIND [date('2015-07-21'),
              date('20150721'),
              date('2015-07'),
              date('201507'),
              date('2015-W30-2'),
              date('2015W302'),
              date('2015-W30'),
              date('2015W30'),
              date('2015-202'),
              date('2015202'),
              date('2015')] AS d
      RETURN d
      """
    Then the result should be, in order:
      | d            |
      | '2015-07-21' |
      | '2015-07-21' |
      | '2015-07-01' |
      | '2015-07-01' |
      | '2015-07-21' |
      | '2015-07-21' |
      | '2015-07-20' |
      | '2015-07-20' |
      | '2015-07-21' |
      | '2015-07-21' |
      | '2015-01-01' |
    And no side effects

  Scenario: Should parse local time from string
    Given any graph
    When executing query:
      """
      UNWIND [localtime('21:40:32.142'),
              localtime('214032.142'),
              localtime('21:40:32'),
              localtime('214032'),
              localtime('21:40'),
              localtime('2140'),
              localtime('21')] AS d
      RETURN d
      """
    Then the result should be, in order:
      | d              |
      | '21:40:32.142' |
      | '21:40:32.142' |
      | '21:40:32'     |
      | '21:40:32'     |
      | '21:40'        |
      | '21:40'        |
      | '21:00'        |
    And no side effects

  Scenario: Should parse time from string
    Given any graph
    When executing query:
      """
      UNWIND [time('21:40:32.142+0100'),
              time('214032.142Z'),
              time('21:40:32+01:00'),
              time('214032-0100'),
              time('21:40-01:30'),
              time('2140-00:00'),
              time('2140-02'),
              time('22+18:00')] AS d
      RETURN d
      """
    Then the result should be, in order:
      | d                    |
      | '21:40:32.142+01:00' |
      | '21:40:32.142Z'      |
      | '21:40:32+01:00'     |
      | '21:40:32-01:00'     |
      | '21:40-01:30'        |
      | '21:40Z'             |
      | '21:40-02:00'        |
      | '22:00+18:00'        |
    And no side effects

  Scenario: Should parse local date time from string
    Given any graph
    When executing query:
      """
      UNWIND [localdatetime('2015-07-21T21:40:32.142'),
              localdatetime('2015-W30-2T214032.142'),
              localdatetime('2015-202T21:40:32'),
              localdatetime('2015T214032'),
              localdatetime('20150721T21:40'),
              localdatetime('2015-W30T2140'),
              localdatetime('2015202T21')] AS d
      RETURN d
      """
    Then the result should be, in order:
      | d                         |
      | '2015-07-21T21:40:32.142' |
      | '2015-07-21T21:40:32.142' |
      | '2015-07-21T21:40:32'     |
      | '2015-01-01T21:40:32'     |
      | '2015-07-21T21:40'        |
      | '2015-07-20T21:40'        |
      | '2015-07-21T21:00'        |
    And no side effects

  Scenario: Should parse date time from string
    Given any graph
    When executing query:
      """
      UNWIND [datetime('2015-07-21T21:40:32.142+0100'),
              datetime('2015-W30-2T214032.142Z'),
              datetime('2015-202T21:40:32+01:00'),
              datetime('2015T214032-0100'),
              datetime('20150721T21:40-01:30'),
              datetime('2015-W30T2140-00:00'),
              datetime('2015-W30T2140-02'),
              datetime('2015202T21+18:00')] AS d
      RETURN d
      """
    Then the result should be, in order:
      | d                               |
      | '2015-07-21T21:40:32.142+01:00' |
      | '2015-07-21T21:40:32.142Z'      |
      | '2015-07-21T21:40:32+01:00'     |
      | '2015-01-01T21:40:32-01:00'     |
      | '2015-07-21T21:40-01:30'        |
      | '2015-07-20T21:40Z'             |
      | '2015-07-20T21:40-02:00'        |
      | '2015-07-21T21:00+18:00'        |
    And no side effects

  Scenario: Should parse date time with named time zone from string
    Given any graph
    When executing query:
      """
      UNWIND [datetime('2015-07-21T21:40:32.142+02:00[Europe/Stockholm]'),
              datetime('2015-07-21T21:40:32.142+0845[Australia/Eucla]'),
              datetime('2015-07-21T21:40:32.142-04[America/New_York]'),
              datetime('2015-07-21T21:40:32.142[Europe/London]'),
              datetime('1818-07-21T21:40:32.142[Europe/Stockholm]')
             ] AS d
      RETURN d
      """
    Then the result should be, in order:
      | d                                                 |
      | '2015-07-21T21:40:32.142+02:00[Europe/Stockholm]'    |
      | '2015-07-21T21:40:32.142+08:45[Australia/Eucla]'     |
      | '2015-07-21T21:40:32.142-04:00[America/New_York]'    |
      | '2015-07-21T21:40:32.142+01:00[Europe/London]'       |
      | '1818-07-21T21:40:32.142+01:12:12[Europe/Stockholm]' |
    And no side effects

  Scenario: Should parse duration from string
    Given any graph
    When executing query:
      """
      UNWIND [duration("P14DT16H12M"),
              duration("P5M1.5D"),
              duration("P0.75M"),
              duration("PT0.75M"),
              duration("P2.5W"),
              duration("P12Y5M14DT16H12M70S"),
              duration("P2012-02-02T14:37:21.545")] AS d
      RETURN d
      """
    Then the result should be, in order:
      | d                          |
      | 'P14DT16H12M'              |
      | 'P5M1DT12H'                |
      | 'P22DT19H51M49.5S'         |
      | 'PT45S'                    |
      | 'P17DT12H'                 |
      | 'P12Y5M14DT16H13M10S'      |
      | 'P2012Y2M2DT14H37M21.545S' |
    And no side effects
