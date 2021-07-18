/*
 * Copyright (c) 2015-2021 "Neo Technology,"
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


/** `Publisher[A,This]` objects publish events of type `A`
 *  to all registered subscribers. When subscribing, a subscriber may specify
 *  a filter which can be used to constrain the number of events sent to the
 *  subscriber. Subscribers may suspend their subscription, or reactivate a
 *  suspended subscription. Class `Publisher` is typically used
 *  as a mixin. The abstract type `Pub` models the type of the publisher itself.
 *
 *  @tparam Evt      type of the published event.
 *
 *  @author  Matthias Zenger
 *  @author  Martin Odersky
 *  @since   1
 */
package org.opencypher.tools.tck.api.events.mutable

import scala.collection.{mutable => mut}

trait Publisher[Evt] {

  type Pub <: Publisher[Evt]
  type Sub = Subscriber[Evt, Pub]
  type Filter = Evt => Boolean

  /** The publisher itself of type `Pub`. Implemented by a cast from `this` here.
   *  Needs to be overridden if the actual publisher is different from `this`.
   */
  protected val self: Pub = this.asInstanceOf[Pub]

  private val filters = new mut.HashMap[Sub, mut.Set[Filter]] with mut.MultiMap[Sub, Filter]
  private val suspended = new mut.HashSet[Sub]

  def subscribe(sub: Sub) { subscribe(sub, event => true) }
  def subscribe(sub: Sub, filter: Filter) { filters.addBinding(sub, filter) }
  def suspendSubscription(sub: Sub) { suspended += sub }
  def activateSubscription(sub: Sub) { suspended -= sub }
  def removeSubscription(sub: Sub) { filters -= sub }
  def removeSubscriptions() { filters.clear() }

  protected def publish(event: Evt) {
    filters.keys.foreach(sub =>
      if (!suspended.contains(sub) &&
        filters.entryExists(sub, p => p(event)))
        sub.notify(self, event)
    )
  }

  /** Checks if two publishers are structurally identical.
   *
   *  @return true, iff both publishers contain the same sequence of elements.
   */
  override def equals(obj: Any): Boolean = obj match {
    case that: Publisher[_] => filters == that.filters && suspended == that.suspended
    case _                  => false
  }
}

