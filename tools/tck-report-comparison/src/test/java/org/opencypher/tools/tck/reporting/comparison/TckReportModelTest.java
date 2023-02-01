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
package org.opencypher.tools.tck.reporting.comparison;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.text.DecimalFormat;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opencypher.tools.tck.reporting.comparison.TckReportModel.Diff;
import org.opencypher.tools.tck.reporting.comparison.TckReportModel.Scenario;
import org.opencypher.tools.tck.reporting.comparison.TckReportModel.Scenarios;

public class TckReportModelTest {
    Scenarios suite0;
    Scenarios suite1;
    Scenarios suite2;
    Scenarios suite3;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        suite0 = Scenarios.create(asList(
            new Scenario("Feature1", "Scenario1", false),
            new Scenario("Feature1", "Scenario2", false),
            new Scenario("Feature2", "Scenario3", false)
        ));

        suite1 = Scenarios.create(asList(
            new Scenario("Feature1", "Scenario1", true),
            new Scenario("Feature1", "Scenario2", false),
            new Scenario("Feature2", "Scenario3", false)
        ));

        suite2 = Scenarios.create(asList(
            new Scenario("Feature1", "Scenario1", true),
            new Scenario("Feature1", "Scenario2", true)
        ));

        suite3 = Scenarios.create(asList(
            new Scenario("Feature1", "Scenario1", true),
            new Scenario("Feature1", "Scenario2", true),
            new Scenario("Feature2", "Scenario3", true)
        ));
    }

    @Test
    public void testScenarios100() {
        assertThat(suite0.all(), hasSize(3));
        assertThat(suite0.passing, hasSize(3));
        assertThat(suite0.failed, empty());
    }

    @Test
    public void testScenarios66() {
        assertThat(suite1.all(), hasSize(3));
        assertThat(suite1.passing, hasSize(2));
        assertThat(suite1.failed, hasSize(1));
    }

    @Test
    public void testDiff100() {
        Diff diff = Diff.create(suite0.passing,
            suite0.failed,
            3,
            3,
            suite0.all());

        assertThat(diff.getPassingPercentage(), equalTo(formatPercentage(1)));
    }

    @Test
    public void testDiff66() {
        Diff diff = Diff.create(suite1.passing,
            suite1.failed,
            2,
            3,
            suite1.all());

        assertThat(diff.getPassingPercentage(), equalTo(formatPercentage(2f/3)));
    }

    @Test
    public void testDiff0() {
        Diff diff = Diff.create(new ArrayList<>(),
            new ArrayList<>(),
            0,
            0,
            new ArrayList<>());

        assertThat(diff.getPassingPercentage(), equalTo("N/A"));
    }

    @Test
    public void testFailedScenario() {
        Scenario scenario = Scenario.create("Feature \"Feature1\": Scenario \"Scenario2\"", new TckReportModel.Failure());

        assertThat(scenario.getFeatureName(), equalTo("Feature1"));
        assertThat(scenario.getName(), equalTo("Scenario2"));
        assertThat(scenario.getStatus(), equalTo("Failed"));
    }

    @Test
    public void testPassedScenario() {
        Scenario scenario = Scenario.create("Feature \"Feature1\": Scenario \"Scenario2\"", (TckReportModel.Failure) null);

        assertThat(scenario.getFeatureName(), equalTo("Feature1"));
        assertThat(scenario.getName(), equalTo("Scenario2"));
        assertThat(scenario.getStatus(), equalTo("Passed"));
    }

    @Test
    public void testCompareImprove() throws Exception {
        Diff result = suite0.compare(suite1);

        assertThat(result.getPassingPercentage(), equalTo(formatPercentage(1)));
        assertThat(result.getNewlyPassingScenarios(), hasSize(1));
        assertThat(result.getNewlyFailedScenarios(), hasSize(0));
        assertThat(result.getAllScenarios(), hasSize(3));
        assertThat(result.getTotalPassingScenarios(), equalTo(3));
        assertThat(result.getTotalScenarios(), equalTo(3));
    }

    @Test
    public void testCompareNoChange() throws Exception {
        Diff result = suite0.compare(suite0);

        assertThat(result.getPassingPercentage(), equalTo(formatPercentage(1)));
        assertThat(result.getNewlyPassingScenarios(), hasSize(0));
        assertThat(result.getNewlyFailedScenarios(), hasSize(0));
        assertThat(result.getAllScenarios(), hasSize(3));
        assertThat(result.getTotalPassingScenarios(), equalTo(3));
        assertThat(result.getTotalScenarios(), equalTo(3));
    }

    @Test
    public void testCompareDegrade() throws Exception {
        Diff result = suite1.compare(suite0);

        assertThat(result.getPassingPercentage(), equalTo(formatPercentage(2f/3)));
        assertThat(result.getNewlyPassingScenarios(), hasSize(0));
        assertThat(result.getNewlyFailedScenarios(), hasSize(1));
        assertThat(result.getAllScenarios(), hasSize(3));
        assertThat(result.getTotalPassingScenarios(), equalTo(2));
        assertThat(result.getTotalScenarios(), equalTo(3));
    }

    @Test
    public void testDiffSize1() throws Exception {
        thrown.expectMessage("number of all scenarios changed");
        suite1.compare(suite2);
    }

    @Test
    public void testDiffSize2() throws Exception {
        thrown.expectMessage("number of all scenarios changed");
        suite2.compare(suite1);
    }


    @Test
    public void testVerifyOnlyFailed() throws Exception {
        thrown.expectMessage("only failed scenarios");
        suite0.verify(suite1);
    }

    @Test
    public void testVerifyImprove() throws Exception {
        Diff result = suite0.verify(suite2);

        assertThat(result.getPassingPercentage(), equalTo(formatPercentage(1)));
        assertThat(result.getNewlyPassingScenarios(), hasSize(2));
        assertThat(result.getNewlyFailedScenarios(), hasSize(0));
        assertThat(result.getAllScenarios(), hasSize(3));
        assertThat(result.getTotalPassingScenarios(), equalTo(3));
        assertThat(result.getTotalScenarios(), equalTo(3));
    }

    @Test
    public void testVerifyNoChange() throws Exception {
        Diff result = suite2.verify(suite2);

        assertThat(result.getPassingPercentage(), equalTo(formatPercentage(0)));
        assertThat(result.getNewlyPassingScenarios(), hasSize(0));
        assertThat(result.getNewlyFailedScenarios(), hasSize(0));
        assertThat(result.getAllScenarios(), hasSize(2));
        assertThat(result.getTotalPassingScenarios(), equalTo(0));
        assertThat(result.getTotalScenarios(), equalTo(2));
    }

    @Test
    public void testVerifyDegrade() throws Exception {
        Diff result = suite3.verify(suite2);

        assertThat(result.getPassingPercentage(), equalTo(formatPercentage(0)));
        assertThat(result.getNewlyPassingScenarios(), hasSize(0));
        assertThat(result.getNewlyFailedScenarios(), hasSize(1));
        assertThat(result.getAllScenarios(), hasSize(3));
        assertThat(result.getTotalPassingScenarios(), equalTo(0));
        assertThat(result.getTotalScenarios(), equalTo(3));
    }

    private String formatPercentage(float percentage) {
        return new DecimalFormat("#.##%").format(percentage);
    }
}