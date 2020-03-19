/*
 * Copyright (c) 2015-2020 "Neo Technology,"
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
package org.opencypher.tools.tck.inspection.diff

sealed trait ElementaryDiffTag

object ElementaryDiffTag {
  case object Added extends ElementaryDiffTag
  case object Removed extends ElementaryDiffTag
  case object Unchanged extends ElementaryDiffTag
  case object Changed extends ElementaryDiffTag

  def apply(changed: Boolean): ElementaryDiffTag = if(changed) Changed else Unchanged

  def apply[E](pair: (E, E)): ElementaryDiffTag = pair match {
    case (Some(_), None) => Removed
    case (None, Some(_)) => Added
    case (b, a) if b == a => Unchanged
    case (b, a) if b != a => Changed
  }
}