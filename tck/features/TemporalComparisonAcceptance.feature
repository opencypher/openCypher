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

Feature: TemporalComparisonAcceptance

  Scenario: Should compare dates
    Given any graph
    When executing query:
      """
      UNWIND [date({year:1980, month:12, day:24}),
              date({year:1984, month:10, day:11})] AS x
      UNWIND [date({year:1984, month:10, day:11}),
              localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:12, minute:31, second:14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, nanosecond: 645876123}),
              datetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, nanosecond: 645876123, timezone: '+01:00'}),
              duration({years: 12, months:5, days: 14, hours:16, minutes: 12, seconds: 70})] AS d
      RETURN x>d, x<d, x>=d, x<=d, x=d
      """
    Then the result should be, in order:
      | x>d   | x<d   | x>=d  | x<=d  | x=d    |
      | false | true  | false | true  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | false | false | true  | true  | true   |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
    And no side effects

  Scenario: Should compare local times
    Given any graph
    When executing query:
      """
      UNWIND [localtime({hour:10, minute:35}),
              localtime({hour:12, minute:31, second:14, nanosecond: 645876123})] AS x
      UNWIND [date({year:1984, month:10, day:11}),
              localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:12, minute:31, second:14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, nanosecond: 645876123}),
              datetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, nanosecond: 645876123, timezone: '+01:00'}),
              duration({years: 12, months:5, days: 14, hours:16, minutes: 12, seconds: 70})] AS d
      RETURN x>d, x<d, x>=d, x<=d, x=d
      """
    Then the result should be, in order:
      | x>d   | x<d   | x>=d  | x<=d  | x=d   |
      | null  | null  | null  | null  | false |
      | false | true  | false | true  | false |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | false | false | true  | true  | true  |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
    And no side effects

  Scenario: Should compare times
    Given any graph
    When executing query:
      """
      UNWIND [time({hour:10, minute:0, timezone: '+01:00'}),
              time({hour:9, minute:35, second:14, nanosecond: 645876123, timezone: '+00:00'})] AS x
      UNWIND [date({year:1984, month:10, day:11}),
              localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:9, minute:35, second:14, nanosecond: 645876123, timezone: '+00:00'}),
              localdatetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, nanosecond: 645876123}),
              datetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, nanosecond: 645876123, timezone: '+01:00'}),
              duration({years: 12, months:5, days: 14, hours:16, minutes: 12, seconds: 70})] AS d
      RETURN x>d, x<d, x>=d, x<=d, x=d
      """
    Then the result should be, in order:
      | x>d   | x<d   | x>=d  | x<=d  | x=d   |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | false | true  | false | true  | false |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | false | false | true  | true  | true  |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
      | null  | null  | null  | null  | false |
    And no side effects

  Scenario: Should compare local date times
    Given any graph
    When executing query:
      """
      UNWIND [localdatetime({year:1980, month:12, day:11, hour:12, minute:31, second:14}),
              localdatetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, nanosecond: 645876123})] AS x
      UNWIND [date({year:1984, month:10, day:11}),
              localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:9, minute:35, second:14, nanosecond: 645876123, timezone: '+00:00'}),
              localdatetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, nanosecond: 645876123}),
              datetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, nanosecond: 645876123, timezone: '+01:00'}),
              duration({years: 12, months:5, days: 14, hours:16, minutes: 12, seconds: 70})] AS d
      RETURN x>d, x<d, x>=d, x<=d, x=d
      """
    Then the result should be, in order:
      | x>d   | x<d   | x>=d  | x<=d  | x=d    |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | false | true  | false | true  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
      | false | false | true  | true  | true   |
      | null  | null  | null  | null  | false  |
      | null  | null  | null  | null  | false  |
    And no side effects

  Scenario: Should compare date times
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year:1980, month:12, day:11, hour:12, minute:31, second:14, timezone: '+00:00'}),
              datetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, timezone: '+05:00'})] AS x
      UNWIND [date({year:1984, month:10, day:11}),
              localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:9, minute:35, second:14, nanosecond: 645876123, timezone: '+00:00'}),
              localdatetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, nanosecond: 645876123}),
              datetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, timezone: '+05:00'}),
              duration({years: 12, months:5, days: 14, hours:16, minutes: 12, seconds: 70})] AS d
      RETURN x>d, x<d, x>=d, x<=d, x=d
      """
    Then the result should be, in order:
      | x>d   | x<d   | x>=d  | x<=d  | x=d  |
      | null  | null  | null  | null  | false|
      | null  | null  | null  | null  | false|
      | null  | null  | null  | null  | false|
      | null  | null  | null  | null  | false|
      | false | true  | false | true  | false|
      | null  | null  | null  | null  | false|
      | null  | null  | null  | null  | false|
      | null  | null  | null  | null  | false|
      | null  | null  | null  | null  | false|
      | null  | null  | null  | null  | false|
      | false | false | true  | true  | true |
      | null  | null  | null  | null  | false|
    And no side effects

  Scenario: Should compare durations for equality
    Given any graph
    When executing query:
      """
      WITH duration({years: 12, months:5, days: 14, hours:16, minutes: 12, seconds: 70}) AS x
      UNWIND [date({year:1984, month:10, day:11}),
              localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:9, minute:35, second:14, nanosecond: 645876123, timezone: '+00:00'}),
              localdatetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, nanosecond: 645876123}),
              datetime({year:1984, month:10, day:11, hour:12, minute:31, second:14, timezone: '+05:00'}),
              duration({years: 12, months:5, days: 14, hours:16, minutes: 12, seconds: 70}),
              duration({years: 12, months:5, days: 14, hours:16, minutes: 13, seconds: 10}),
              duration({years: 12, months:5, days: 13, hours:40, minutes: 13, seconds: 10})] AS d
      RETURN x=d
      """
    Then the result should be, in order:
      | x=d    |
      | false  |
      | false  |
      | false  |
      | false  |
      | false  |
      | true   |
      | true   |
      | false  |
    And no side effects
