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
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
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
    /**
     * The Spock {@code Specification} class.
     */
    private static final Optional<Class<?>> SPECIFICATION =
            findClass("spock.lang.Specification");

    /**
     * The Jupiter {@code @BeforeAll} annotation.
     */
    private static final Optional<Class<? extends Annotation>> BEFORE_ALL =
            findClass("org.junit.jupiter.api.BeforeAll");

    /**
     * The JUnit 4 {@code @BeforeClass} annotation.
     */
    private static final Optional<Class<? extends Annotation>> BEFORE_CLASS =
            findClass("org.junit.BeforeClass");

    /**
     * The Jupiter {@code @AfterAll} annotation.
     */
    private static final Optional<Class<? extends Annotation>> AFTER_ALL =
            findClass("org.junit.jupiter.api.AfterAll");

    /**
     * The JUnit 4 {@code @AfterClass} annotation.
     */
    private static final Optional<Class<? extends Annotation>> AFTER_CLASS =
            findClass("org.junit.AfterClass");

    /**
     * The JUnit 4 {@code @ClassRule} annotation.
     */
    private static final Optional<Class<? extends Annotation>> CLASS_RULE =
            findClass("org.junit.ClassRule");

    /**
     * The Spock {@code @Shared} annotation.
     */
    private static final Optional<Class<? extends Annotation>> SHARED =
            findClass("spock.lang.Shared");

    /**
     * The Spock {@code @Stepwise} annotation.
     */
    private static final Optional<Class<? extends Annotation>> STEPWISE =
            findClass("spock.lang.Stepwise");

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
     * Finds a class via reflection and returns it if found, or an empty {@code Optional} otherwise.
     *
     * @param className the name of the class to find
     * @return the class if present
     * @param <T> the type of the class
     */
    @SuppressWarnings("unchecked")
    private static <T> Optional<Class<? extends T>> findClass(String className) {
        try {
            return Optional.of(((Class<? extends T>) Class.forName(className)));
        } catch (final ClassNotFoundException ex) {
            return Optional.empty();
        }
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
            if (shouldTreatAsOneUnit(testIdentifier)) {
                executionOfAtomicPartStarted(testIdentifier);
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

                testUnitExecutionListener.executionStarted(new Description(testIdentifier.getUniqueId(), testClass), true);
                identifiers.add(testIdentifier);
            }
        }

        /**
         * Handle the start of execution of an atomic part.
         *
         * @param testIdentifier the test identifier of the atomic part
         */
        private void executionOfAtomicPartStarted(TestIdentifier testIdentifier) {
            if (hasClassSource(testIdentifier)) {
                if (serializeExecution) {
                    lock(testIdentifier);
                }

                testUnitExecutionListener.executionStarted(new Description(testIdentifier.getUniqueId(), testClass), true);
                identifiers.add(testIdentifier);
            }
        }

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            if (shouldTreatAsOneUnit(testIdentifier)) {
                if (hasClassSource(testIdentifier)) {
                    testUnitExecutionListener.executionFinished(new Description(testIdentifier.getUniqueId(), testClass),
                            testExecutionResult.getStatus() != TestExecutionResult.Status.FAILED,
                            testExecutionResult.getThrowable().orElse(null));
                    // unlock the serializer for the finished tests to let the next test continue
                    unlock(testIdentifier);
                }
                return;
            }

            // Jupiter classes with failing BeforeAlls never start execution and identify as 'containers' not 'tests'
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
                unlock(testIdentifier);
            }
        }

        /**
         * Locks the correct serializer lock for the given test identifier, so that all tests are run sequentially.
         *
         * @param testIdentifier the test identifier to lock for
         */
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

        /**
         * Unlocks the correct serializer lock for the given test identifier, so that the next test can start.
         *
         * @param testIdentifier the test identifier to unlock for
         */
        public void unlock(TestIdentifier testIdentifier) {
            ReentrantLock lock = coverageSerializers.remove(testIdentifier.getUniqueIdObject());
            if (lock != null) {
                lock.unlock();
            }
        }

        /**
         * Returns whether the given test identifier has a class source.
         *
         * @param testIdentifier the test identifier to check
         * @return whether the given test identifier has a class source
         */
        private boolean hasClassSource(TestIdentifier testIdentifier) {
            return testIdentifier.getSource().filter(ClassSource.class::isInstance).isPresent();
        }

        /**
         * Returns whether the given test identifier has a method source.
         *
         * @param testIdentifier the test identifier to check
         * @return whether the given test identifier has a method source
         */
        private boolean hasMethodSource(TestIdentifier testIdentifier) {
            return testIdentifier.getSource().filter(MethodSource.class::isInstance).isPresent();
        }

        /**
         * Returns whether the given test identifier is part of an atomic unit.
         *
         * @param testIdentifier the test identifier to check
         * @return whether the given test identifier is part of an atomic unit
         */
        private boolean shouldTreatAsOneUnit(TestIdentifier testIdentifier) {
            return shouldTreatSpockSpecificationAsOneUnit(testIdentifier);
        }

        /**
         * Returns whether the test class of the given test identifier is a Spock specification
         * that should be treated atomically.
         *
         * @param testIdentifier the test identifier to check
         * @return whether the test class of the given test identifier is a Spock specification that should be treated atomically
         */
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

        /**
         * Returns the test class of the given test identifier, if any.
         *
         * @param testIdentifier the test identifier to check
         * @return the test class of the given test identifier, if any
         */
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

        /**
         * Returns whether the given class is a Spock specification.
         *
         * @param clazz the class to check
         * @return whether the given class is a Spock specification
         */
        private boolean isSpockSpecification(Class<?> clazz) {
            return SPECIFICATION.filter(specification -> specification.isAssignableFrom(clazz)).isPresent();
        }

        /**
         * Returns whether any of the given methods has a Jupiter {@code @BeforeAll} annotation.
         *
         * @param methods the methods to check
         * @return whether any of the given methods has a Jupiter {@code @BeforeAll} annotation
         */
        private boolean hasBeforeAllAnnotations(Set<Method> methods) {
            return BEFORE_ALL.filter(beforeAll -> hasAnnotation(methods, beforeAll)).isPresent();
        }

        /**
         * Returns whether any of the given methods has a JUnit 4 {@code @BeforeClass} annotation.
         *
         * @param methods the methods to check
         * @return whether any of the given methods has a JUnit 4 {@code @BeforeClass} annotation
         */
        private boolean hasBeforeClassAnnotations(Set<Method> methods) {
            return BEFORE_CLASS.filter(beforeClass -> hasAnnotation(methods, beforeClass)).isPresent();
        }

        /**
         * Returns whether any of the given methods has a Jupiter {@code @AfterAll} annotation.
         *
         * @param methods the methods to check
         * @return whether any of the given methods has a Jupiter {@code @BeforeClass} annotation
         */
        private boolean hasAfterAllAnnotations(Set<Method> methods) {
            return AFTER_ALL.filter(afterAll -> hasAnnotation(methods, afterAll)).isPresent();
        }

        /**
         * Returns whether any of the given methods has a JUnit 4 {@code @AfterClass} annotation.
         *
         * @param methods the methods to check
         * @return whether any of the given methods has a JUnit 4 {@code @AfterClass} annotation
         */
        private boolean hasAfterClassAnnotations(Set<Method> methods) {
            return AFTER_CLASS.filter(afterClass -> hasAnnotation(methods, afterClass)).isPresent();
        }

        /**
         * Returns whether the given class or any of the given methods has a JUnit 4 {@code @ClassRule} annotation.
         *
         * @param clazz   the class to check
         * @param methods the methods to check
         * @return whether the given class or any of the given methods has a JUnit 4 {@code @ClassRule} annotation
         */
        private boolean hasClassRuleAnnotations(Class<?> clazz, Set<Method> methods) {
            return CLASS_RULE.filter(aClass -> hasAnnotation(methods, aClass)
                    || hasAnnotation(Reflection.publicFields(clazz), aClass)).isPresent();
        }

        /**
         * Returns whether the given annotated element is annotated with the given annotation class.
         *
         * @param annotatedElement the annotated element to check
         * @param annotation       the class of the annotation to check for
         * @return whether the given annotated element is annotated with the given annotation class
         */
        private boolean hasAnnotation(AnnotatedElement annotatedElement, Class<? extends Annotation> annotation) {
            return annotatedElement.isAnnotationPresent(annotation);
        }

        /**
         * Returns whether any of the given annotated elements is annotated with the given annotation class.
         *
         * @param annotatedElements the annotated elements to check
         * @param annotation        the class of the annotation to check for
         * @return whether any of the given annotated elements is annotated with the given annotation class
         */
        private boolean hasAnnotation(Set<? extends AnnotatedElement> annotatedElements, Class<? extends Annotation> annotation) {
            return FCollection.contains(annotatedElements, annotatedElement -> annotatedElement.isAnnotationPresent(annotation));
        }

        /**
         * Returns whether any of the given methods has the given name.
         *
         * @param methods    the methods to check
         * @param methodName the method name to check for
         * @return whether any of the given methods has the given name
         */
        private boolean hasMethodNamed(Set<Method> methods, String methodName) {
            return FCollection.contains(methods, havingName(methodName));
        }

        /**
         * Returns a predicate that checks whether a method has the given name.
         *
         * @param methodName the method name to check for
         * @return a predicate that checks whether a method has the given name
         */
        private Predicate<Method> havingName(String methodName) {
            return method -> method.getName().equals(methodName);
        }

        /**
         * Returns whether the given class has a Spock {@code @Shared} field.
         *
         * @param clazz the class to check
         * @return whether the given class has a Spock {@code @Shared} field
         */
        private boolean hasSharedField(Class<?> clazz) {
            return hasAnnotation(allFields(clazz), SHARED.orElseThrow(AssertionError::new));
        }

        /**
         * Returns all fields of the given class and its class hierarchy.
         *
         * @param clazz the class to get the fields for
         * @return all fields of the given class and its class hierarchy
         */
        private Set<Field> allFields(Class<?> clazz) {
            final Set<Field> fields = new LinkedHashSet<>();
            if (clazz != null) {
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                fields.addAll(allFields(clazz.getSuperclass()));
            }
            return fields;
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
