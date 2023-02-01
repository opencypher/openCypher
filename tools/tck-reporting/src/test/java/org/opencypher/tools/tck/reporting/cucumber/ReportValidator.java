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

import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ReportValidator implements BeforeAllCallback, AfterAllCallback {
    private Path cucumberReport;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        cucumberReport = Files.createTempFile("cucumber", ".json");
        cucumberReport.toFile().deleteOnExit();
        System.setProperty("cucumber.plugin", "json:" + cucumberReport);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        String expected = CucumberReportAdapterTest.getResource("expected.json");
        String actual = new String(readAllBytes(cucumberReport));

        assertEquals(
            ignoreTimeAndDuration(expected),
            ignoreTimeAndDuration(actual));
    }

    /**
     * duration 0 == no duration. See `cucumber.runtime.formatter.JSONFormatter`
     */
    private String ignoreTimeAndDuration(String report) {
        return report.
                replaceAll("\n\\s*\"duration\":\\s*\\d+\\s*,", "").
                replaceAll("\n\\s*\"start_timestamp\":\\s*\"[\\w:.-]*\"\\s*,", "");
    }
}
