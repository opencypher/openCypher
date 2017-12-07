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

  /**
    * Executes a Cypher query with the provided parameters. This version also
    * returns the graph in its state after the query has been executed, for
    * implementations that have immutable graphs.
    *
    * Additionally, this call provides a metadata tag for the step type that
    * requests the query to be executed. This allows implementations to use the
    * TCK for validation of supported subsets of Cypher functionality by
    * mapping initial state setup and side effect validation to alternative
    * functionality in the implementation. Implementations that support all the
    * necessary constructs used in these steps should ignore this parameter.
    *
    * @param query the Cypher query to execute.
    * @param params the parameters for the query.
    * @param meta metadata tag which specifies what kind of step the query is executed in.
    * @return the graph in its state after having executed the query, and the result table of the query.
    */
  def execute(query: String, params: Map[String, CypherValue], meta: QueryType): (Graph, Records) =
    this -> cypher(query, params, meta)

  /**
    * Executes a Cypher query with the provided parameters.
    *
    * Additionally, this call provides a metadata tag for the step type that
    * requests the query to be executed. This allows implementations to use the
    * TCK for validation of supported subsets of Cypher functionality by
    * mapping initial state setup and side effect validation to alternative
    * functionality in the implementation. Implementations that support all the
    * necessary constructs used in these steps should ignore this parameter.
    *
    * @param query the Cypher query to execute.
    * @param params the parameters for the query.
    * @param meta metadata tag which specifies what kind of step the query is executed in.
    * @return the graph in its state after having executed the query, and the result table of the query.
    */
  def cypher(query: String, params: Map[String, CypherValue], meta: QueryType): Records =
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
