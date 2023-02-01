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
package org.opencypher.tools.tck.api

import org.opencypher.tools.tck.values.CypherValue

import scala.language.implicitConversions

/**
  * Mutable implementations implement .cypher
  * Immutable implementations implement .execute
  *
  * An implementation will not have to implement .cypher if .execute is overridden.
  */
trait Graph extends ResultCreation {

  type Result = Graph.Result

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
  def execute(query: String, params: Map[String, CypherValue], meta: QueryType): (Graph, Result) =
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
  def cypher(query: String, params: Map[String, CypherValue], meta: QueryType): Result =
    throw new UnsupportedOperationException("To use the TCK, implement this method or override .execute()")

  /**
    * When the Graph is used by a Scenario, this method will be called at the end of execution,
    * regardless if the result was an error or not. Immutable implementations that return
    * different graphs have to close the old instances explicitly when returning a new reference.
    *
    * @see execute
    */
  def close(): Unit =
    ()
}

object Graph {
  type Result = Either[ExecutionFailed, CypherValueRecords]
}

/**
  * Mix in this trait in your `Graph` implementation to opt in to running
  * scenarios pertaining to the CALL clause and procedures.
  */
trait ProcedureSupport {
  self: Graph =>

  def registerProcedure(signature: String, values: CypherValueRecords): Unit
}

/**
 * Mix in this trait in your `Graph` implementation to opt in to running
 * scenarios that create temporary CSV files.
 */
trait CsvFileCreationSupport {
  self: Graph =>

  /**
   * Create a CSV file and return file URL as String.
   * @return URL of the created CSV file
   */
  def createCSVFile(contents: CypherValueRecords): String
}

trait ResultCreation {
  implicit def resultFromValueRecords(records: CypherValueRecords): Graph.Result = Right(records)
  implicit def resultFromStringRecords(records: StringRecords): Graph.Result = Right(records.asValueRecords)
  implicit def resultFromError(error: ExecutionFailed): Graph.Result = Left(error)
}
