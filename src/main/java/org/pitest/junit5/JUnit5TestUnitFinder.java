/*
 * Copyright 2017 Tobias Stadler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.junit5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.synchronizedSet;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.pitest.testapi.Description;
import org.pitest.testapi.NullExecutionListener;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitExecutionListener;
import org.pitest.testapi.TestUnitFinder;

/**
 *
 * @author Tobias Stadler
 */
public class JUnit5TestUnitFinder implements TestUnitFinder {

    /**
     * The test group config.
     */
    private final TestGroupConfig testGroupConfig;

    /**
     * Test methods that should be included.
     */
    private final Collection<String> includedTestMethods;

    /**
     * The JUnit platform launcher used to execute tests.
     */
    private final Launcher launcher;

    /**
     * Constructs a new JUnit 5 test unit finder.
     *
     * @param testGroupConfig     the test group config
     * @param includedTestMethods test methods that should be included
     */
    public JUnit5TestUnitFinder(TestGroupConfig testGroupConfig, Collection<String> includedTestMethods) {
        this.testGroupConfig = testGroupConfig;
        this.includedTestMethods = includedTestMethods;
        this.launcher = LauncherFactory.create();
    }

    @Override
    public List<TestUnit> findTestUnits(Class<?> clazz, TestUnitExecutionListener executionListener) {
        if(clazz.getEnclosingClass() != null) {
            return emptyList();
        }

        List<Filter<?>> filters = new ArrayList<>(2);
        try {
            List<String> excludedGroups = testGroupConfig.getExcludedGroups().stream().filter(group -> !group.isEmpty()).collect(Collectors.toList());
            if(!excludedGroups.isEmpty()) {
                filters.add(TagFilter.excludeTags(excludedGroups));
            }

            List<String> includedGroups = testGroupConfig.getIncludedGroups().stream().filter(group -> !group.isEmpty()).collect(Collectors.toList());
            if(!includedGroups.isEmpty()) {
                filters.add(TagFilter.includeTags(includedGroups));
            }
        } catch(PreconditionViolationException e) {
            throw new IllegalArgumentException("Error creating tag filter", e);
        }

        TestIdentifierListener listener = new TestIdentifierListener(clazz, executionListener);

        launcher.execute(LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectClass(clazz))
                .filters(filters.toArray(new Filter[0]))
                .build(), listener);

        return listener.getIdentifiers()
                .stream()
                .map(testIdentifier -> new JUnit5TestUnit(clazz, testIdentifier))
                .collect(toList());
    }

    /**
     * A test execution listener that listens for test identifiers, supporting atomic test units
     * and notifying the supplied test unit execution listener so that for example coverage can
     * be recorded right away during discovery phase already.
     */
    private class TestIdentifierListener implements TestExecutionListener {
        /**
         * The test class as given to the test unit finder for forwarding to the test unit execution listener.
         */
        private final Class<?> testClass;

        /**
         * The test unit execution listener, that for example is used for coverage recording per test.
         */
        private final TestUnitExecutionListener testUnitExecutionListener;

        /**
         * The collected test identifiers.
         */
        private final Set<TestIdentifier> identifiers = synchronizedSet(new LinkedHashSet<>());

        /**
         * Whether to serialize test execution, because we are during coverage recording which is
         * done through static fields and thus does not support parallel test execution.
         */
        private final boolean serializeExecution;

        /**
         * A map that holds the locks that child tests of locked parent tests should use.
         * For example parallel data-driven Spock features start the feature execution which is CONTAINER_AND_TEST,
         * then wait for the parallel iteration executions to be finished which are TEST,
         * then finish the feature execution.
         * Due to that we cannot lock the iteration executions on the same lock as the feature executions,
         * as the feature execution is around all the subordinate iteration executions.
         *
         * <p>This logic will of course break if there is some test engine that does strange setups like
         * having CONTAINER_AND_TEST with child CONTAINER that have child TEST and similar.
         * If those engines happen to be used, tests will start to deadlock, as the grand-child test
         * would not find the parent serializer and thus use the root serializer on which the grand-parent
         * CONTAINER_AND_TEST already locks.
         *
         * <p>This setup would probably not make much sense, so should not be taken into account
         * unless such an engine actually pops up. If it does and someone tries to use it with PIT,
         * the logic should maybe be made more sophisticated like remembering the parent-child relationships
         * to be able to find the grand-parent serializer which is not possible stateless, because we are
         * only able to get the parent identifier directly, but not further up stateless.
         */
        private final Map<UniqueId, AtomicReference<ReentrantLock>> parentCoverageSerializers = new ConcurrentHashMap<>();

        /**
         * A map that holds the actual lock used for a specific test to be able to easily and safely unlock
         * without the need to recalculate which lock to use.
         */
        private final Map<UniqueId, ReentrantLock> coverageSerializers = new ConcurrentHashMap<>();

        /**
         * The root coverage serializer to be used for the top-most recorded tests.
         */
        private final ReentrantLock rootCoverageSerializer = new ReentrantLock();

        /**
         * Constructs a new test identifier listener.
         *
         * @param testClass                 the test class as given to the test unit finder for forwarding to the result collector
         * @param testUnitExecutionListener the test unit execution listener to notify during test execution
         */
        public TestIdentifierListener(Class<?> testClass, TestUnitExecutionListener testUnitExecutionListener) {
            this.testClass = testClass;
            this.testUnitExecutionListener = testUnitExecutionListener;
            // PIT gives a coverage recording listener here during coverage recording
            // At the later stage during minion hunting a NullExecutionListener is given
            // as PIT is only interested in the resulting list of identifiers.
            // Serialization of test execution is only necessary during coverage calculation
            // currently. To be on the safe side serialize test execution for any listener
            // type except listener types where we know tests can run in parallel safely,
            // i.e. currently the NullExecutionListener which is the only other one besides
            // the coverage recording listener.
            serializeExecution = !(testUnitExecutionListener instanceof NullExecutionListener);
        }

        /**
         * Returns the collected test identifiers.
         *
         * @return the collected test identifiers
         */
        private List<TestIdentifier> getIdentifiers() {
            return unmodifiableList(new ArrayList<>(identifiers));
        }

        @Override
        public void executionStarted(TestIdentifier testIdentifier) {
            if (testIdentifier.isTest()) {
                // filter out testMethods
                if (includedTestMethods != null && !includedTestMethods.isEmpty()
                        && testIdentifier.getSource().isPresent()
                        && testIdentifier.getSource().get() instanceof MethodSource
                        && !includedTestMethods.contains(((MethodSource)testIdentifier.getSource().get()).getMethodName())) {
                    return;
                }

                if (serializeExecution) {
                    coverageSerializers.compute(testIdentifier.getUniqueIdObject(), (uniqueId, lock) -> {
                        if (lock != null) {
                            throw new AssertionError("No lock should be present");
                        }

                        // find the serializer to lock the test on
                        // if there is a parent test locked, use the lock for its children if not,
                        // use the root serializer
                        return testIdentifier
                                .getParentIdObject()
                                .map(parentCoverageSerializers::get)
                                .map(lockRef -> lockRef.updateAndGet(parentLock ->
                                        parentLock == null ? new ReentrantLock() : parentLock))
                                .orElse(rootCoverageSerializer);
                    }).lock();
                    // record a potential serializer for child tests to lock on
                    parentCoverageSerializers.put(testIdentifier.getUniqueIdObject(), new AtomicReference<>());
                }

                testUnitExecutionListener.executionStarted(new Description(testIdentifier.getUniqueId(), testClass), true);
                identifiers.add(testIdentifier);
            }
        }

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            // Classes with failing BeforeAlls never start execution and identify as 'containers' not 'tests'
            if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
                identifiers.add(testIdentifier);
                testUnitExecutionListener.executionFinished(new Description(testIdentifier.getUniqueId(), testClass)
                        , false, testExecutionResult.getThrowable().orElse(null));
            } else if (testIdentifier.isTest()) {
                testUnitExecutionListener.executionFinished(new Description(testIdentifier.getUniqueId(), testClass)
                        , true);
            }

            if (serializeExecution) {
                // forget the potential serializer for child tests
                parentCoverageSerializers.remove(testIdentifier.getUniqueIdObject());
                // unlock the serializer for the finished tests to let the next test continue
                ReentrantLock lock = coverageSerializers.remove(testIdentifier.getUniqueIdObject());
                if (lock != null) {
                    lock.unlock();
                }
            }
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", JUnit5TestUnitFinder.class.getSimpleName() + "[", "]")
                .add("testGroupConfig=" + testGroupConfig)
                .add("includedTestMethods=" + includedTestMethods)
                .toString();
    }
}
