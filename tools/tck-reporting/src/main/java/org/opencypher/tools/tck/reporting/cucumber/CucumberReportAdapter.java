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
package org.opencypher.tools.tck.reporting.cucumber;

import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.TestStep;
import cucumber.api.event.EventListener;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.api.event.TestSourceRead;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.api.event.WriteEvent;
import cucumber.runner.CanonicalOrderEventPublisher;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.formatter.PluginFactory;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opencypher.tools.tck.api.CypherValueRecords;
import org.opencypher.tools.tck.api.ExecutionFailed;
import org.opencypher.tools.tck.api.Measure;
import org.opencypher.tools.tck.api.Scenario;
import org.opencypher.tools.tck.api.SideEffects;
import org.opencypher.tools.tck.api.Step;
import org.opencypher.tools.tck.api.events.TCKEvents;
import org.opencypher.tools.tck.reporting.cucumber.model.TCKTestCase;
import org.opencypher.tools.tck.reporting.cucumber.model.TCKTestStep;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;
import scala.util.Either;

public class CucumberReportAdapter implements BeforeAllCallback, AfterAllCallback {
    private final CanonicalOrderEventPublisher bus = new CanonicalOrderEventPublisher();
    private final SystemOutReader output = new SystemOutReader();

    private final Map<String, String> featureNameToUri = new HashMap<>();
    private final Map<String, Long> stepTimestamp = new HashMap<>();

    private TestCase currentTestCase;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        initCucumberPlugins();

        TCKEvents.feature().subscribe(adapt(featureReadEvent()));
        TCKEvents.scenario().subscribe(adapt(scenarioStartedEvent()));
        TCKEvents.stepStarted().subscribe(adapt(stepStartedEvent()));
        TCKEvents.stepFinished().subscribe(adapt(stepFinishedEvent()));

        bus.handle(new TestRunStarted(time()));
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        bus.handle(new TestRunFinished(time()));
        output.close();
    }

    private void initCucumberPlugins() {
        PluginFactory pluginFactory = new PluginFactory();
        RuntimeOptions options = new RuntimeOptions("");
        options.getPluginFormatterNames()
            .stream()
            .map(pluginFactory::create)
            .filter(EventListener.class::isInstance)
            .map(EventListener.class::cast)
            .forEach(json -> json.setEventPublisher(bus));
    }

    private Consumer<TCKEvents.FeatureRead> featureReadEvent() {
        return feature -> {
            featureNameToUri.put(feature.name(), feature.uri());
            bus.handle(new TestSourceRead(time(), feature.uri(), feature.source()));
        };
    }

    private Consumer<Scenario> scenarioStartedEvent() {
        return scenario -> {
            Long startedAt = time();
            String featureUri = checkNull(featureNameToUri.get(scenario.featureName()));
            Pickle pickle = scenario.source();
            List<TestStep> steps = pickle.getSteps()
                .stream()
                .map(step -> new TCKTestStep(step, featureUri, outlineLocation(step.getLocations())))
                .collect(Collectors.toList());
            int line = outlineLocation(pickle.getLocations());
            currentTestCase = new TCKTestCase(pickle, steps, featureUri, line);
            bus.handle(new TestCaseStarted(startedAt, currentTestCase));
        };
    }

    private Consumer<TCKEvents.StepStarted> stepStartedEvent() {
        return event -> {
            Long startedAt = time();
            stepTimestamp.put(event.correlationId(), startedAt);
            Step step = event.step();
            if (shouldReport(step)) {
                int line = outlineLocation(step.source().getLocations());
                TCKTestStep testStep = new TCKTestStep(step.source(), checkNull(currentTestCase).getUri(), line);
                TestStepStarted cucumberEvent = new TestStepStarted(startedAt, checkNull(currentTestCase), testStep);
                bus.handle(cucumberEvent);
            }
        };
    }

    private Consumer<TCKEvents.StepFinished> stepFinishedEvent() {
        return event -> {
            Long finishedAt = time();
            Step step = event.step();
            if (shouldReport(step)) {
                logOutput(step, finishedAt);
                Long startedAt = checkNull(stepTimestamp.get(event.correlationId()));
                Long duration = finishedAt - startedAt;
                Result.Type status = getStatus(event.result());
                int line = outlineLocation(step.source().getLocations());
                PickleStepTestStep testStep = new TCKTestStep(step.source(), checkNull(currentTestCase).getUri(), line);
                Result result = new Result(status, duration, errorOrNull(event.result()));
                TestStepFinished cucumberEvent = new TestStepFinished(finishedAt, checkNull(currentTestCase), testStep, result);
                bus.handle(cucumberEvent);
            } else {
                output.clear();
            }
        };
    }

    private void logOutput(Step step, Long timeNow) {
        String log = output.clear();

        if (step instanceof SideEffects) {
            SideEffects sideEffects = (SideEffects) step;
            log = log + sideEffects.expected();
        }

        if (!log.isEmpty()) {
            bus.handle(new WriteEvent(timeNow, checkNull(currentTestCase), log));
        }
    }

    private boolean shouldReport(Step step) {
        return !(step instanceof Measure);
    }

    private <T> AbstractFunction1<T, BoxedUnit> adapt(Consumer<T> procedure) {
        return new AbstractFunction1<T, BoxedUnit>() {
            @Override
            public BoxedUnit apply(T v1) {
                procedure.accept(v1);
                return BoxedUnit.UNIT;
            }
        };
    }

    private Result.Type getStatus(Either<Throwable, Either<ExecutionFailed, CypherValueRecords>> result) {
        return result.isRight() ? Result.Type.PASSED : Result.Type.FAILED;
    }

    private Throwable errorOrNull(Either<Throwable, Either<ExecutionFailed, CypherValueRecords>> result) {
        return result.isLeft() ? result.left().get() : null;
    }

    private Long time() {
        return System.currentTimeMillis();
    }

    private int outlineLocation(List<PickleLocation> locations) {
        return locations.get(locations.size() - 1).getLine();
    }

    private  <T> T checkNull(T value) {
        if (value == null) {
            throw new IllegalStateException("Wrong order of test events. Disable parallel execution of tests (forkCount).");
        }
        return value;
    }
}
