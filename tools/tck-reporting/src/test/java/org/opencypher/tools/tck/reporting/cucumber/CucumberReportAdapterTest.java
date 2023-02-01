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
package org.opencypher.tools.tck.reporting.cucumber;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.opencypher.tools.tck.api.CypherTCK;
import org.opencypher.tools.tck.api.CypherValueRecords;
import org.opencypher.tools.tck.api.ExecutionFailed;
import org.opencypher.tools.tck.api.Graph;
import org.opencypher.tools.tck.api.QueryType;
import org.opencypher.tools.tck.api.Scenario;
import org.opencypher.tools.tck.api.StringRecords;
import org.opencypher.tools.tck.values.CypherValue;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import scala.collection.immutable.List;
import scala.collection.immutable.Map;
import scala.collection.immutable.Set;
import scala.runtime.AbstractFunction0;
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;

@ExtendWith({ReportValidator.class, CucumberReportAdapter.class})
public class CucumberReportAdapterTest {
    @TestFactory
    public Stream<DynamicTest> runFeatures() throws Exception {
        Path featuresPath = Paths.get(CucumberReportAdapterTest.class.getResource( ".").toURI());
        Path featureFile = featuresPath.relativize(featuresPath.resolve("Foo.feature"));
        String content = getResource("Foo.feature");

        Seq<Scenario> scenariosSeq = CypherTCK.parseFeature(featureFile, content, JavaConverters.asScalaBuffer(new java.util.ArrayList<String>()).toSeq()).scenarios();
        java.util.List<Scenario> scenarios = JavaConverters.seqAsJavaList(scenariosSeq);

        AbstractFunction0<Graph> graph = graph();

        return scenarios.stream()
            .map(scenario -> {
                String name = scenario.toString();
                Runnable runnable = scenario.apply(graph);
                return DynamicTest.dynamicTest(name, runnable::run);
            });
    }

    private class FakeGraph implements Graph {
        @Override
        public Either<ExecutionFailed, CypherValueRecords> cypher(String query, Map<String, CypherValue> params, QueryType meta) {
            if (query.contains("foo()")) {
                return new Left<>(new ExecutionFailed("SyntaxError", "compile time", "UnknownFunction", null));
            } else if (query.startsWith("RETURN ")) {
                String result = query.replace("RETURN ", "");
                System.out.println("Producing some output " + result);
                List<String> header = new Set.Set1<>(result).toList();
                Map<String, String> row = new Map.Map1<>(result, result);
                List<Map<String, String>> rows = new Set.Set1<>(row).toList();
                return new Right<>(new StringRecords(header, rows).asValueRecords());
            } else {
                return new Right<>(CypherValueRecords.empty());
            }
        }
    }

    private AbstractFunction0<Graph> graph() {
        FakeGraph fakeGraph = new FakeGraph();
        return new AbstractFunction0<Graph>() {
            @Override
            public Graph apply() {
                return fakeGraph;
            }
        };
    }

    static String getResource(String path) throws IOException {
        InputStream resource = CucumberReportAdapterTest.class.getResourceAsStream(path);
        if (resource == null) {
            throw new IOException(path + " not found");
        }
        return new Scanner(resource, "UTF-8").useDelimiter("\\A").next();
    }
}
