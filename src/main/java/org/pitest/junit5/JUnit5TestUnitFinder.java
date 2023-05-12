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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.pitest.functional.FCollection;
import org.pitest.reflection.Reflection;
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
    private static final Optional<Class<?>> SPECIFICATION =
            findClass("spock.lang.Specification");
    private static final Optional<Class<? extends Annotation>> BEFORE_ALL =
            findClass("org.junit.jupiter.api.BeforeAll");
    private static final Optional<Class<? extends Annotation>> BEFORE_CLASS =
            findClass("org.junit.BeforeClass");
    private static final Optional<Class<? extends Annotation>> AFTER_ALL =
            findClass("org.junit.jupiter.api.AfterAll");
    private static final Optional<Class<? extends Annotation>> AFTER_CLASS =
            findClass("org.junit.AfterClass");
    private static final Optional<Class<? extends Annotation>> CLASS_RULE =
            findClass("org.junit.ClassRule");
    private static final Optional<Class<? extends Annotation>> SHARED =
            findClass("spock.lang.Shared");
    private static final Optional<Class<? extends Annotation>> STEPWISE =
            findClass("spock.lang.Stepwise");

    private final TestGroupConfig testGroupConfig;

    private final Collection<String> includedTestMethods;

    private final Launcher launcher;

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

        List<Filter> filters = new ArrayList<>(2);
        try {
            List<String> excludedGroups = testGroupConfig.getExcludedGroups();
            if(excludedGroups != null && !excludedGroups.isEmpty()) {
                filters.add(TagFilter.excludeTags(excludedGroups));
            }

            List<String> includedGroups = testGroupConfig.getIncludedGroups();
            if(includedGroups != null && !includedGroups.isEmpty()) {
                filters.add(TagFilter.includeTags(includedGroups));
            }
        } catch(PreconditionViolationException e) {
            throw new IllegalArgumentException("Error creating tag filter", e);
        }

        TestIdentifierListener listener = new TestIdentifierListener(clazz, executionListener);

        launcher.execute(LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectClass(clazz))
                .filters(filters.toArray(new Filter[filters.size()]))
                .build(), listener);

        return listener.getIdentifiers()
                .stream()
                .map(testIdentifier -> new JUnit5TestUnit(clazz, testIdentifier))
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<Class<? extends T>> findClass(String className) {
        try {
            return Optional.of(((Class<? extends T>) Class.forName(className)));
        } catch (final ClassNotFoundException ex) {
            return Optional.empty();
        }
    }

    private class TestIdentifierListener implements TestExecutionListener {
        private final Class<?> testClass;
        private final TestUnitExecutionListener l;
        private final List<TestIdentifier> identifiers = synchronizedList(new ArrayList<>());
        private final boolean serializeExecution;
        // This map holds the locks that child tests of locked parent tests should use.
        // For example parallel data-driven Spock features start the feature execution which is CONTAINER_AND_TEST,
        // then wait for the parallel iteration executions to be finished which are TEST,
        // then finish the feature execution.
        // Due to that we cannot lock the iteration executions on the same lock as the feature executions,
        // as the feature execution is around all the subordinate iteration executions.
        //
        // This logic will of course break if there is some test engine that does strange setups like
        // having CONTAINER_AND_TEST with child CONTAINER that have child TEST and similar.
        // If those engines happen to be used, tests will start to deadlock, as the grand-child test
        // would not find the parent serializer and thus use the root serializer on which the grand-parent
        // CONTAINER_AND_TEST already locks.
        //
        // This setup would probably not make much sense, so should not be taken into account
        // unless such an engine actually pops up. If it does and someone tries to use it with PIT,
        // the logic should maybe be made more sophisticated like remembering the parent-child relationships
        // to be able to find the grand-parent serializer which is not possible stateless, because we are
        // only able to get the parent identifier directly, but not further up stateless.
        private final Map<UniqueId, AtomicReference<ReentrantLock>> parentCoverageSerializers = new ConcurrentHashMap<>();
        // This map holds the actual lock used for a specific test to be able to easily and safely unlock
        // without the need to recalculate which lock to use.
        private final Map<UniqueId, ReentrantLock> coverageSerializers = new ConcurrentHashMap<>();
        private final ReentrantLock rootCoverageSerializer = new ReentrantLock();

        public TestIdentifierListener(Class<?> testClass, TestUnitExecutionListener l) {
            this.testClass = testClass;
            this.l = l;
            // PIT gives a coverage recording listener here during coverage recording
            // At the later stage during minion hunting a NullExecutionListener is given
            // as PIT is only interested in the resulting list of identifiers.
            // Serialization of test execution is only necessary during coverage calculation
            // currently. To be on the safe side serialize test execution for any listener
            // type except listener types where we know tests can run in parallel safely,
            // i.e. currently the NullExecutionListener which is the only other one besides
            // the coverage recording listener.
            serializeExecution = !(l instanceof NullExecutionListener);
        }

        List<TestIdentifier> getIdentifiers() {
            return unmodifiableList(new ArrayList<>(identifiers));
        }

        @Override
        public void executionStarted(TestIdentifier testIdentifier) {
            if (shouldTreatAsOneUnit(testIdentifier)) {
                if (hasClassSource(testIdentifier)) {
                    if (serializeExecution) {
                        lock(testIdentifier);
                    }

                    l.executionStarted(new Description(testIdentifier.getUniqueId(), testClass), true);
                    identifiers.add(testIdentifier);
                }
                return;
            }

            if (testIdentifier.isTest()) {
                // filter out testMethods
                if (includedTestMethods != null && !includedTestMethods.isEmpty()
                        && hasMethodSource(testIdentifier)
                        && !includedTestMethods.contains(((MethodSource) testIdentifier.getSource().get()).getMethodName())) {
                    return;
                }

                if (serializeExecution) {
                    lock(testIdentifier);
                    // record a potential serializer for child tests to lock on
                    parentCoverageSerializers.put(testIdentifier.getUniqueIdObject(), new AtomicReference<>());
                }

                l.executionStarted(new Description(testIdentifier.getUniqueId(), testClass), true);
                identifiers.add(testIdentifier);
            }
        }

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            if (shouldTreatAsOneUnit(testIdentifier)) {
                if (hasClassSource(testIdentifier)) {
                    l.executionFinished(new Description(testIdentifier.getUniqueId(), testClass),
                            testExecutionResult.getStatus() != TestExecutionResult.Status.FAILED);
                    // unlock the serializer for the finished tests to let the next test continue
                    unlock(testIdentifier);
                }
                return;
            }

            // Jupiter classes with failing BeforeAlls never start execution and identify as 'containers' not 'tests'
            if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
                if (!identifiers.contains(testIdentifier)) {
                    identifiers.add(testIdentifier);
                }
                l.executionFinished(new Description(testIdentifier.getUniqueId(), testClass), false);
            } else if (testIdentifier.isTest()) {
                l.executionFinished(new Description(testIdentifier.getUniqueId(), testClass), true);
            }

            if (serializeExecution) {
                // forget the potential serializer for child tests
                parentCoverageSerializers.remove(testIdentifier.getUniqueIdObject());
                // unlock the serializer for the finished tests to let the next test continue
                unlock(testIdentifier);
            }
        }

        public void lock(TestIdentifier testIdentifier) {
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
        }

        public void unlock(TestIdentifier testIdentifier) {
            ReentrantLock lock = coverageSerializers.remove(testIdentifier.getUniqueIdObject());
            if (lock != null) {
                lock.unlock();
            }
        }

        private boolean hasClassSource(TestIdentifier testIdentifier) {
            return testIdentifier.getSource().filter(ClassSource.class::isInstance).isPresent();
        }

        private boolean hasMethodSource(TestIdentifier testIdentifier) {
            return testIdentifier.getSource().filter(MethodSource.class::isInstance).isPresent();
        }

        private boolean shouldTreatAsOneUnit(TestIdentifier testIdentifier) {
            return shouldTreatSpockSpecificationAsOneUnit(testIdentifier);
        }

        private boolean shouldTreatSpockSpecificationAsOneUnit(TestIdentifier testIdentifier) {
            Optional<Class<?>> optionalTestClass = getTestClass(testIdentifier);
            if (!optionalTestClass.isPresent()) {
                return false;
            }

            Class<?> testClass = optionalTestClass.get();
            if (!isSpockSpecification(testClass)) {
                return false;
            }

            Set<Method> methods = Reflection.allMethods(testClass);
            return hasBeforeAllAnnotations(methods)
                    || hasBeforeClassAnnotations(methods)
                    || hasAfterAllAnnotations(methods)
                    || hasAfterClassAnnotations(methods)
                    || hasClassRuleAnnotations(testClass, methods)
                    || hasAnnotation(testClass, STEPWISE.orElseThrow(AssertionError::new))
                    || hasAnnotation(methods, STEPWISE.orElseThrow(AssertionError::new))
                    || hasMethodNamed(methods, "setupSpec")
                    || hasMethodNamed(methods, "cleanupSpec")
                    || hasSharedField(testClass);
        }

        private Optional<Class<?>> getTestClass(TestIdentifier testIdentifier) {
            if (hasClassSource(testIdentifier)) {
                return Optional.of(
                        testIdentifier
                                .getSource()
                                .map(ClassSource.class::cast)
                                .orElseThrow(AssertionError::new)
                                .getJavaClass());
            }

            if (hasMethodSource(testIdentifier)) {
                return Optional.of(
                        testIdentifier
                                .getSource()
                                .map(MethodSource.class::cast)
                                .orElseThrow(AssertionError::new)
                                .getJavaClass());
            }

            return Optional.empty();
        }

        private boolean isSpockSpecification(Class<?> clazz) {
            return SPECIFICATION.filter(specification -> specification.isAssignableFrom(testClass)).isPresent();
        }

        private boolean hasBeforeAllAnnotations(Set<Method> methods) {
            return BEFORE_ALL.filter(beforeAll -> hasAnnotation(methods, beforeAll)).isPresent();
        }

        private boolean hasBeforeClassAnnotations(Set<Method> methods) {
            return BEFORE_CLASS.filter(beforeClass -> hasAnnotation(methods, beforeClass)).isPresent();
        }

        private boolean hasAfterAllAnnotations(Set<Method> methods) {
            return AFTER_ALL.filter(afterAll -> hasAnnotation(methods, afterAll)).isPresent();
        }

        private boolean hasAfterClassAnnotations(Set<Method> methods) {
            return AFTER_CLASS.filter(afterClass -> hasAnnotation(methods, afterClass)).isPresent();
        }

        private boolean hasClassRuleAnnotations(Class<?> clazz, Set<Method> methods) {
            return CLASS_RULE.filter(aClass -> hasAnnotation(methods, aClass)
                    || hasAnnotation(Reflection.publicFields(clazz), aClass)).isPresent();
        }

        private boolean hasAnnotation(AnnotatedElement annotatedElement, Class<? extends Annotation> annotation) {
            return annotatedElement.isAnnotationPresent(annotation);
        }

        private boolean hasAnnotation(Set<? extends AnnotatedElement> methods, Class<? extends Annotation> annotation) {
            return FCollection.contains(methods, annotatedElement -> annotatedElement.isAnnotationPresent(annotation));
        }

        private boolean hasMethodNamed(Set<Method> methods, String methodName) {
            return FCollection.contains(methods, havingName(methodName));
        }

        private Predicate<Method> havingName(String methodName) {
            return method -> method.getName().equals(methodName);
        }

        private boolean hasSharedField(Class<?> clazz) {
            return hasAnnotation(allFields(clazz), SHARED.orElseThrow(AssertionError::new));
        }

        private Set<Field> allFields(Class<?> clazz) {
            final Set<Field> fields = new LinkedHashSet<>();
            if (clazz != null) {
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                fields.addAll(allFields(clazz.getSuperclass()));
            }
            return fields;
        }

    }

}
