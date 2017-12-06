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
package org.opencypher.tools.tck.api

import org.opencypher.tools.tck.values.CypherValue

/**
  * Mutable implementations implement .cypher
  * Immutable implementations implement .execute
  *
  * An implementation will not have to implement .cypher if .execute is overridden.
  */
trait Graph {
  def execute(query: String, params: Map[String, CypherValue] = Map.empty): (Graph, Records) =
    this -> cypher(query, params)

  def cypher(query: String, params: Map[String, CypherValue] = Map.empty): Records =
    throw new UnsupportedOperationException("To use the TCK, implement this method or override .execute()")
}

/**
  * Mix in this trait in your `Graph` implementation to opt in to running
  * scenarios pertaining to the CALL clause and procedures.
  */
trait ProcedureSupport {
  self: Graph =>

  def registerProcedure(signature: String, values: CypherValueRecords): Unit
}
