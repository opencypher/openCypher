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

import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.StepDefinedEvent;
import io.cucumber.plugin.event.TestCaseEvent;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class CanonicalEventOrder implements Comparator<Event> {
    private static final CanonicalEventOrder.FixedEventOrderComparator fixedOrder = new CanonicalEventOrder.FixedEventOrderComparator();
    private static final CanonicalEventOrder.TestCaseEventComparator testCaseOrder = new CanonicalEventOrder.TestCaseEventComparator();

    CanonicalEventOrder() {
    }

    public int compare(Event a, Event b) {
        int fixedOrder = CanonicalEventOrder.fixedOrder.compare(a, b);
        if (fixedOrder != 0) {
            return fixedOrder;
        } else {
            return a instanceof TestCaseEvent && b instanceof TestCaseEvent ? testCaseOrder.compare((TestCaseEvent)a, (TestCaseEvent)b) : fixedOrder;
        }
    }

    private static final class TestCaseEventComparator implements Comparator<TestCaseEvent> {
        private TestCaseEventComparator() {
        }

        public int compare(TestCaseEvent a, TestCaseEvent b) {
            int uri = a.getTestCase().getUri().compareTo(b.getTestCase().getUri());
            if (uri != 0) {
                return uri;
            } else {
                int line = Integer.compare(a.getTestCase().getLine(), b.getTestCase().getLine());
                return line != 0 ? line : a.getInstant().compareTo(b.getInstant());
            }
        }
    }

    private static final class FixedEventOrderComparator implements Comparator<Event> {
        private final List<Class<? extends Event>> fixedOrder;

        private FixedEventOrderComparator() {
            this.fixedOrder = Arrays.asList(TestRunStarted.class, TestSourceRead.class, SnippetsSuggestedEvent.class, StepDefinedEvent.class, TestCaseEvent.class, TestRunFinished.class);
        }

        public int compare(Event a, Event b) {
            return Integer.compare(this.requireInFixOrder(a.getClass()), this.requireInFixOrder(b.getClass()));
        }

        private int requireInFixOrder(Class<? extends Event> o) {
            int index = this.findInFixedOrder(o);
            if (index < 0) {
                throw new IllegalStateException(o + " was not in " + this.fixedOrder);
            } else {
                return index;
            }
        }

        private int findInFixedOrder(Class<? extends Event> o) {
            for(int i = 0; i < this.fixedOrder.size(); ++i) {
                if (((Class)this.fixedOrder.get(i)).isAssignableFrom(o)) {
                    return i;
                }
            }

            return -1;
        }
    }
}
