/*
 * Copyright (c) 2015-2017 "Neo Technology,"
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

import org.junit.jupiter.api.function.Executable

case class Scenario(featureName: String, name: String, steps: List[Step]) extends (Graph => Executable) {
  override def toString() = s"""Feature "$featureName": Scenario "$name""""

  override def apply(graph: Graph): Executable = new Executable {
    override def execute(): Unit = executeOnGraph(graph)
  }

  def executeOnGraph(empty: Graph): Unit = {
    steps.foldLeft(empty -> Records.empty) {
      case ((g, _), Execute(query)) => g.execute(query)
      case ((g, r), ExpectOrderedResult(expected)) =>
        assert(r == expected, s"Got result $r, but expected $expected")
        g -> r
      case ((g, r), NoSideEffects) =>  g -> r
      case (in, _) => in //TODO: Implement all steps
    }
  }
}
