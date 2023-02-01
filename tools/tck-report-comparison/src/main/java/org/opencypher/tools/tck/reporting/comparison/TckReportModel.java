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

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.opencypher.tools.tck.reporting.comparison.TckResultsComparator.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TckReportModel {
    static class Scenarios {
        private final List<Scenario> all;
        protected final List<Scenario> passing;
        protected final List<Scenario> failed;

        private Scenarios(List<Scenario> all, List<Scenario> passing, List<Scenario> failed) {
            this.all = all;
            this.passing = passing;
            this.failed = failed;
        }

        static Scenarios create(List<Scenario> scenarios) {
            List<Scenario> all = unmodifiableList(scenarios);

            List<Scenario> passed = all.stream()
                .filter(Scenario::isPassed)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));

            List<Scenario> failed = all.stream()
                .filter(Scenario::isFailed)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));

            return new Scenarios(all, passed, failed);
        }

        List<Scenario> all() {
            return all;
        }

        Diff compare(Scenarios other) {
            checkState(this.all.size() == other.all.size(),
                "Cannot compare: the number of all scenarios changed from %s to %s.", other.all.size(), this.all.size());

            List<Scenario> newlyPassingScenarios = subtract(this.passing, other.passing);
            List<Scenario> newlyFailedScenarios = subtract(this.failed, other.failed);
            int totalPassingScenarios = passing.size();
            int totalScenarios = all.size();

            return Diff.create(newlyPassingScenarios, newlyFailedScenarios,
                totalPassingScenarios, totalScenarios, all);
        }

        Diff verify(Scenarios other) {
            checkState(other.all().stream().noneMatch(Scenario::isPassed),
                "Verification should contain only failed scenarios");

            List<Scenario> newlyPassingScenarios = subtract(other.failed, this.failed);
            List<Scenario> newlyFailedScenarios = subtract(this.failed, other.failed);
            int totalPassingScenarios = passing.size();
            int totalScenarios = all.size();

            return Diff.create(newlyPassingScenarios, newlyFailedScenarios,
                totalPassingScenarios, totalScenarios, all);
        }

        private <T> List<T> subtract(final List<T> list1, final List<T> list2) {
            final ArrayList<T> result = new ArrayList<>(list1);
            list2.forEach(result::remove);

            return result;
        }
    }

    @SuppressWarnings("unused")
    public static class Diff {
        private final List<Scenario> newlyPassingScenarios;
        private final List<Scenario> newlyFailedScenarios;
        private final List<Scenario> allScenarios;
        private final int totalPassingScenarios;
        private final int totalScenarios;
        private final String passingPercentage;

        private Diff(List<Scenario> newlyPassingScenarios, List<Scenario> newlyFailedScenarios, List<Scenario> allScenarios, int totalPassingScenarios, int totalScenarios, String passingPercentage) {
            this.newlyPassingScenarios = newlyPassingScenarios;
            this.newlyFailedScenarios = newlyFailedScenarios;
            this.allScenarios = allScenarios;
            this.totalPassingScenarios = totalPassingScenarios;
            this.totalScenarios = totalScenarios;
            this.passingPercentage = passingPercentage;
        }

        static Diff create(List<Scenario> newlyPassingScenarios, List<Scenario> newlyFailedScenarios,
                           int totalPassingScenarios, int totalScenarios, List<Scenario> allScenarios) {
            String passingPercentage = totalScenarios == 0 ? "N/A" :
                new DecimalFormat("#.##%").format((float) totalPassingScenarios / totalScenarios);
            return new Diff(
                unmodifiableList(newlyPassingScenarios),
                unmodifiableList(newlyFailedScenarios),
                unmodifiableList(allScenarios),
                totalPassingScenarios,
                totalScenarios,
                passingPercentage);
        }

        public List<Scenario> getNewlyPassingScenarios() {
            return newlyPassingScenarios;
        }

        public List<Scenario> getNewlyFailedScenarios() {
            return newlyFailedScenarios;
        }

        public int getTotalPassingScenarios() {
            return totalPassingScenarios;
        }

        public int getTotalScenarios() {
            return totalScenarios;
        }

        public String getPassingPercentage() {
            return passingPercentage;
        }

        public List<Scenario> getAllScenarios() {
            return allScenarios;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JacksonXmlRootElement(localName = "testsuite")
    static class TestSuite {
        @JacksonXmlElementWrapper(localName = "testcase", useWrapping = false)
        private final List<Scenario> testcase;

        @JsonCreator
        public TestSuite(@JacksonXmlProperty(localName = "testcase") List<Scenario> testcase) {
            this.testcase = testcase;
        }

        public List<Scenario> getScenarios() {
            return testcase;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Failure {
    }

    @SuppressWarnings("unused")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Scenario {
        private static Pattern pattern = Pattern.compile("Feature \"(.*?)\": Scenario \"(.*?)\"");

        private final String name;
        private final String featureName;
        private final boolean failed;

        public Scenario(String featureName, String name, boolean failed) {
            this.name = name;
            this.featureName = featureName;
            this.failed = failed;
        }

        @JsonCreator
        public static Scenario create(@JsonProperty("name") String title, @JsonProperty("failure") Failure failure) {
            Matcher matcher = pattern.matcher(title);
            checkState(matcher.find(),
                "Expecting string in format `Feature \"[name]\": Scenario \"[name]\"`, got `%s`." +
                    "This will work with junit-jupiter-engine<=5.1.1 or maven-surefire >=3.0.0-M4." +
                    "If version change is not possible - use Cucumber report."
                , title);
            String featureName = matcher.group(1);
            String name = matcher.group(2);
            return new Scenario(featureName, name, failure != null);
        }

        public boolean isFailed() {
            return failed;
        }

        public boolean isPassed() {
            return !failed;
        }

        public String getStatus() {
            return isPassed() ? "Passed" : "Failed";
        }

        public String getName() {
            return name;
        }

        public String getFeatureName() {
            return featureName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Scenario scenario = (Scenario) o;
            return Objects.equals(featureName, scenario.featureName) &&
                Objects.equals(name, scenario.name);
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CucumberFeature {
        private List<Scenario> scenarios;

        private CucumberFeature(List<Scenario> scenarios) {
            this.scenarios = scenarios;
        }

        @JsonCreator
        public static CucumberFeature create(@JsonProperty("name") String featureName, @JsonProperty("elements") List<CucumberScenario> cucumberScenarios) {
            List<Scenario> scenarios = cucumberScenarios.stream()
                .map(s -> new Scenario(featureName, s.name, s.failed))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));

            return new CucumberFeature(scenarios);
        }


        public List<Scenario> getScenarios() {
            return scenarios;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CucumberScenario {
        private final String name;
        private final boolean failed;

        private CucumberScenario(String name, boolean failed) {
            this.name = name;
            this.failed = failed;
        }

        @JsonCreator
        public static CucumberScenario create(@JsonProperty("name") String name, @JsonProperty("steps") List<CucumberStep> steps) {
            boolean failed = steps.stream()
                .anyMatch(s -> s.result.status.equals("failed"));

            return new CucumberScenario(name, failed);
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CucumberStep {
        private CucumberResult result;

        @JsonCreator
        public CucumberStep(@JsonProperty("result") CucumberResult result) {
            this.result = result;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CucumberResult {
        private String status;

        @JsonCreator
        public CucumberResult(@JsonProperty("status") String status) {
            this.status = status;
        }
    }
}
