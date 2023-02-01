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

import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.PluginOption;
import io.cucumber.core.plugin.JSONFormatter;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.*;
import io.cucumber.core.options.RuntimeOptions;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
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

    private final Map<String, URI> featureNameToUri = new HashMap<>();
    private final Map<String, Instant> stepTimestamp = new HashMap<>();

    private TestCase currentTestCase;

    @Override
    public void beforeAll(ExtensionContext context) {
        initCucumberPlugins();

        TCKEvents.feature().subscribe(adapt(featureReadEvent()));
        TCKEvents.scenario().subscribe(adapt(scenarioStartedEvent()));
        TCKEvents.stepStarted().subscribe(adapt(stepStartedEvent()));
        TCKEvents.stepFinished().subscribe(adapt(stepFinishedEvent()));

        bus.handle(new TestRunStarted(Instant.now()));
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        bus.handle(new TestRunFinished(Instant.now()));
        output.close();
    }

    private void initCucumberPlugins() {
        Map<String, String> properties = System.getProperties().entrySet().stream()
                .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
        RuntimeOptions options = new CucumberPropertiesParser().parse(properties).build();
        Appendable appendable = options.plugins().stream()
            .filter(PluginOption.class::isInstance)
            .map(PluginOption.class::cast)
            .filter(pluginOption -> pluginOption.pluginClass() == JSONFormatter.class)
            .findFirst()
            .map(pluginOption -> {
                try {
                    return (Appendable) new FileWriter(pluginOption.argument());
                } catch (IOException e) {
                    throw new IllegalStateException("File " + pluginOption.argument() + " not found");
                }
            }).orElse(System.out);
        EventListener eventListener = new JSONFormatter(appendable);
        eventListener.setEventPublisher(bus);
    }

    private Consumer<TCKEvents.FeatureRead> featureReadEvent() {
        return feature -> {
            featureNameToUri.put(feature.name(), feature.uri());
            bus.handle(new TestSourceRead(Instant.now(), feature.uri(), feature.source()));
        };
    }

    private Consumer<Scenario> scenarioStartedEvent() {
        return scenario -> {
            currentTestCase = new TCKTestCase(scenario.source());
            bus.handle(new TestCaseStarted(Instant.now(), currentTestCase));
        };
    }

    private Consumer<TCKEvents.StepStarted> stepStartedEvent() {
        return event -> {
            Instant startedAt = Instant.now();
            stepTimestamp.put(event.correlationId(), startedAt);
            Step step = event.step();
            if (shouldReport(step)) {
                TCKTestStep testStep = new TCKTestStep(step.source(), checkNull(currentTestCase).getUri());
                TestStepStarted cucumberEvent = new TestStepStarted(startedAt, checkNull(currentTestCase), testStep);
                bus.handle(cucumberEvent);
            }
        };
    }

    private Consumer<TCKEvents.StepFinished> stepFinishedEvent() {
        return event -> {
            Instant finishedAt = Instant.now();
            Step step = event.step();
            if (shouldReport(step)) {
                logOutput(step, finishedAt);
                Instant startedAt = checkNull(stepTimestamp.get(event.correlationId()));
                Duration duration = Duration.between(startedAt, finishedAt);
                Status status = getStatus(event.result());
                PickleStepTestStep testStep = new TCKTestStep(step.source(), checkNull(currentTestCase).getUri());
                Result result = new Result(status, duration, errorOrNull(event.result()));
                TestStepFinished cucumberEvent = new TestStepFinished(finishedAt, checkNull(currentTestCase), testStep, result);
                bus.handle(cucumberEvent);
            } else {
                output.clear();
            }
        };
    }

    private void logOutput(Step step, Instant timeNow) {
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

    private Status getStatus(Either<Throwable, Either<ExecutionFailed, CypherValueRecords>> result) {
        return result.isRight() ? Status.PASSED : Status.FAILED;
    }

    private Throwable errorOrNull(Either<Throwable, Either<ExecutionFailed, CypherValueRecords>> result) {
        return result.isLeft() ? result.left().get() : null;
    }

    private  <T> T checkNull(T value) {
        if (value == null) {
            throw new IllegalStateException("Wrong order of test events. Disable parallel execution of tests (forkCount).");
        }
        return value;
    }
}
