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
package org.opencypher.tools.tck.constants

object TCKSideEffects {

  val ADDED_NODES = "+nodes"
  val DELETED_NODES = "-nodes"
  val ADDED_RELATIONSHIPS = "+relationships"
  val DELETED_RELATIONSHIPS = "-relationships"
  val ADDED_LABELS = "+labels"
  val DELETED_LABELS = "-labels"
  val ADDED_PROPERTIES = "+properties"
  val DELETED_PROPERTIES = "-properties"

  val ALL = Set(ADDED_NODES, ADDED_RELATIONSHIPS, ADDED_LABELS, ADDED_PROPERTIES,
                DELETED_NODES, DELETED_RELATIONSHIPS, DELETED_LABELS, DELETED_PROPERTIES)

}
