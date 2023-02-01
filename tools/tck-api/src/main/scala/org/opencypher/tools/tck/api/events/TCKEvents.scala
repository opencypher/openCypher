/*
 * Copyright (c) 2015-2023 "Neo Technology,"
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
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.tools.tck.api.events

import java.net.URI
import java.util.UUID
import org.opencypher.tools.tck.api.Graph.Result
import org.opencypher.tools.tck.api.{Scenario, Step}


/** Publishes TCK scenario execution events.
  *
  * The available event sources are:
  *
  * <li>[[TCKEvents.feature]] - sent for each feature file read, contains the feature file source.
  * <li>[[TCKEvents.scenario]]  - sent before starting the execution of a Scenario.
  * <li>[[TCKEvents.stepStarted]] - sent before starting the execution of a Test Step, contains the Test Step.
  * <li>[[TCKEvents.stepFinished]] - sent after the execution of a Test Step, contains the Test Step and its Result.
  * <p>
  * Usage example, subscribing to [[TCKEvents.scenario]] events:
  * <pre>{@code
  * TCKEvents.scenario.subscribe(scenario => { println scenario.name })
  * }</pre>
  */
object TCKEvents {

  trait CorrelationId {
    val correlationId: String
  }

  type StepResult = Either[Throwable, Result]

  case class StepStarted(step: Step) extends CorrelationId {
    override val correlationId: String = UUID.randomUUID().toString
  }

  case class StepFinished(step: Step, result: StepResult, correlationId: String) extends CorrelationId

  case class FeatureRead(name: String, uri: URI, source: String)

  object Publish {
    def apply[T](): Publish[T] = new Publish[T]
  }

  class Publish[T] extends Collections.Publisher[T] {
    def send(event: T): Unit = publish(event)
    def subscribe(callback: T => Unit): Unit = {
      subscribe(new Collections.Subscriber[T, Collections.Publisher[T]] {
        override def notify(pub: Collections.Publisher[T], event: T): Unit = {
          callback(event)
        }
      })
    }
  }

  val feature: Publish[FeatureRead] = Publish[FeatureRead]()
  val scenario: Publish[Scenario] = Publish[Scenario]()
  val stepStarted: Publish[StepStarted] = Publish[StepStarted]()
  val stepFinished: Publish[StepFinished] = Publish[StepFinished]()

  def setFeature(feature: FeatureRead): Unit = {
    this.feature.send(feature)
  }

  def setScenario(scenario: Scenario): Unit = {
    this.scenario.send(scenario)
  }

  def setStepStarted(step: StepStarted): String = {
    this.stepStarted.send(step)
    step.correlationId
  }

  def setStepFinished(step: StepFinished): Unit = {
    this.stepFinished.send(step)
  }

  def reset(): Unit = {
    this.feature.removeSubscriptions()
    this.scenario.removeSubscriptions()
    this.stepStarted.removeSubscriptions()
    this.stepFinished.removeSubscriptions()
  }
}
