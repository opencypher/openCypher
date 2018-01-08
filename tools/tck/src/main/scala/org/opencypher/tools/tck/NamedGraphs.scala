/*
 * Copyright (c) 2015-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.tools.tck

object NamedGraphs {

  val graphs = Map("binary-tree-1" ->
    """
      |CREATE (a:A {name: 'a'}),
      |       (b1:X {name: 'b1'}),
      |       (b2:X {name: 'b2'}),
      |       (b3:X {name: 'b3'}),
      |       (b4:X {name: 'b4'}),
      |       (c11:X {name: 'c11'}),
      |       (c12:X {name: 'c12'}),
      |       (c21:X {name: 'c21'}),
      |       (c22:X {name: 'c22'}),
      |       (c31:X {name: 'c31'}),
      |       (c32:X {name: 'c32'}),
      |       (c41:X {name: 'c41'}),
      |       (c42:X {name: 'c42'})
      |CREATE (a)-[:KNOWS]->(b1),
      |       (a)-[:KNOWS]->(b2),
      |       (a)-[:FOLLOWS]->(b3),
      |       (a)-[:FOLLOWS]->(b4)
      |CREATE (b1)-[:FRIEND]->(c11),
      |       (b1)-[:FRIEND]->(c12),
      |       (b2)-[:FRIEND]->(c21),
      |       (b2)-[:FRIEND]->(c22),
      |       (b3)-[:FRIEND]->(c31),
      |       (b3)-[:FRIEND]->(c32),
      |       (b4)-[:FRIEND]->(c41),
      |       (b4)-[:FRIEND]->(c42)
      |CREATE (b1)-[:FRIEND]->(b2),
      |       (b2)-[:FRIEND]->(b3),
      |       (b3)-[:FRIEND]->(b4),
      |       (b4)-[:FRIEND]->(b1);
    """.stripMargin,
    "binary-tree-2" ->
      """
        |CREATE (a:A {name: 'a'}),
        |       (b1:X {name: 'b1'}),
        |       (b2:X {name: 'b2'}),
        |       (b3:X {name: 'b3'}),
        |       (b4:X {name: 'b4'}),
        |       (c11:X {name: 'c11'}),
        |       (c12:Y {name: 'c12'}),
        |       (c21:X {name: 'c21'}),
        |       (c22:Y {name: 'c22'}),
        |       (c31:X {name: 'c31'}),
        |       (c32:Y {name: 'c32'}),
        |       (c41:X {name: 'c41'}),
        |       (c42:Y {name: 'c42'})
        |CREATE (a)-[:KNOWS]->(b1),
        |       (a)-[:KNOWS]->(b2),
        |       (a)-[:FOLLOWS]->(b3),
        |       (a)-[:FOLLOWS]->(b4)
        |CREATE (b1)-[:FRIEND]->(c11),
        |       (b1)-[:FRIEND]->(c12),
        |       (b2)-[:FRIEND]->(c21),
        |       (b2)-[:FRIEND]->(c22),
        |       (b3)-[:FRIEND]->(c31),
        |       (b3)-[:FRIEND]->(c32),
        |       (b4)-[:FRIEND]->(c41),
        |       (b4)-[:FRIEND]->(c42)
        |CREATE (b1)-[:FRIEND]->(b2),
        |       (b2)-[:FRIEND]->(b3),
        |       (b3)-[:FRIEND]->(b4),
        |       (b4)-[:FRIEND]->(b1);
    """.stripMargin
  )

}
