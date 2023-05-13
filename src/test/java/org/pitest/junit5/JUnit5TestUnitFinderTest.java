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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.pitest.junit5.cucumber.RunCucumberTest;
import org.pitest.junit5.repository.AbstractTestClass;
import org.pitest.junit5.repository.InterfaceTestClass;
import org.pitest.junit5.repository.ParameterizedNoExplicitSource;
import org.pitest.junit5.repository.TestClassWithAbortingTest;
import org.pitest.junit5.repository.TestClassWithAfterAll;
import org.pitest.junit5.repository.TestClassWithBeforeAll;
import org.pitest.junit5.repository.TestClassWithFailingAfterAll;
import org.pitest.junit5.repository.TestClassWithFailingBeforeAll;
import org.pitest.junit5.repository.TestClassWithFailingTest;
import org.pitest.junit5.repository.TestClassWithIncludedTestMethod;
import org.pitest.junit5.repository.TestClassWithInheritedTestMethod;
import org.pitest.junit5.repository.TestClassWithMixedPassAndFail;
import org.pitest.junit5.repository.TestClassWithMultiplePassingTests;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedClassWithoutAnnotations;
import org.pitest.junit5.repository.TestClassWithParameterizedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithRepeatedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTags;
import org.pitest.junit5.repository.TestClassWithTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithTestTemplateAnnotation;
import org.pitest.junit5.repository.TestClassWithoutAnnotations;
import org.pitest.junit5.repository.TestSpecWithAbortingFeature;
import org.pitest.junit5.repository.TestSpecWithAfterAll;
import org.pitest.junit5.repository.TestSpecWithAfterClass;
import org.pitest.junit5.repository.TestSpecWithBeforeAll;
import org.pitest.junit5.repository.TestSpecWithBeforeClass;
import org.pitest.junit5.repository.TestSpecWithClassRuleField;
import org.pitest.junit5.repository.TestSpecWithClassRuleMethod;
import org.pitest.junit5.repository.TestSpecWithCleanupSpec;
import org.pitest.junit5.repository.TestSpecWithDataDrivenFeature;
import org.pitest.junit5.repository.TestSpecWithFailingCleanupSpec;
import org.pitest.junit5.repository.TestSpecWithFailingFeature;
import org.pitest.junit5.repository.TestSpecWithFailingSetupSpec;
import org.pitest.junit5.repository.TestSpecWithIncludedFeature;
import org.pitest.junit5.repository.TestSpecWithInheritedFeature;
import org.pitest.junit5.repository.TestSpecWithMixedPassAndFail;
import org.pitest.junit5.repository.TestSpecWithMultiplePassingFeatures;
import org.pitest.junit5.repository.TestSpecWithSetupSpec;
import org.pitest.junit5.repository.TestSpecWithSetupSpecWithoutShared;
import org.pitest.junit5.repository.TestSpecWithShared;
import org.pitest.junit5.repository.TestSpecWithSimpleFeature;
import org.pitest.junit5.repository.TestSpecWithStepwise;
import org.pitest.junit5.repository.TestSpecWithStepwiseFeature;
import org.pitest.junit5.repository.TestSpecWithTags;
import org.pitest.junit5.repository.TestSpecWithoutFeatures;
import org.pitest.testapi.Description;
import org.pitest.testapi.ExecutedInDiscovery;
import org.pitest.testapi.NullExecutionListener;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitExecutionListener;
import org.spockframework.runtime.ConditionNotSatisfiedError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Tobias Stadler
 */
class JUnit5TestUnitFinderTest {

    @Test
    void findsAndRunsBasicJupiterTests() {
        findsAndRunsNTests(1, TestClassWithTestAnnotation.class);
    }

    @Test
    void findsAndRunsBasicSpockTests() {
        findsAndRunsNTests(1, TestSpecWithSimpleFeature.class);
    }

    @Test
    void findsAndRunsBasicJupiterTestsWhenMultiplePresent() {
        findsAndRunsNTests(3, TestClassWithMultiplePassingTests.class);
    }

    @Test
    void findsAndRunsBasicSpockTestsWhenMultiplePresent() {
        findsAndRunsNTests(3, TestSpecWithMultiplePassingFeatures.class);
    }

    @Test
    void descriptionsOfTestsRunDuringDiscoveryMatchThoseOfDiscoveredTests() {
        JUnit5TestUnitFinder underTest = basicConfig();
        List<Description> runInDiscovery = run(underTest, TestClassWithMultiplePassingTests.class).started;
        List<Description> discovered = underTest.findTestUnits(TestClassWithMultiplePassingTests.class, new NullExecutionListener())
                .stream()
                .map(tu -> tu.getDescription())
                .collect(Collectors.toList());

        assertThat(runInDiscovery).containsExactlyInAnyOrderElementsOf(discovered);
    }

    @Test
    void descriptionsOfTestsRunDuringDiscoveryMatchThoseOfDiscoveredSpockTests() {
        JUnit5TestUnitFinder underTest = basicConfig();
        List<Description> runInDiscovery = run(underTest, TestSpecWithMultiplePassingFeatures.class).started;
        List<Description> discovered = underTest.findTestUnits(TestSpecWithMultiplePassingFeatures.class, new NullExecutionListener())
                .stream()
                .map(tu -> tu.getDescription())
                .collect(Collectors.toList());

        assertThat(runInDiscovery).containsExactlyInAnyOrderElementsOf(discovered);
    }

    @Test
    void discoveredTestsAreMarkedAsExecuted() {
       JUnit5TestUnitFinder underTest = basicConfig();
       List<TestUnit> discovered = underTest.findTestUnits(TestClassWithMultiplePassingTests.class,
               new NullExecutionListener());

        assertThat(discovered).allMatch(tu -> tu instanceof ExecutedInDiscovery);
    }

    @Test
    void discoveredSpockTestsAreMarkedAsExecuted() {
       JUnit5TestUnitFinder underTest = basicConfig();
       List<TestUnit> discovered = underTest.findTestUnits(TestSpecWithMultiplePassingFeatures.class,
               new NullExecutionListener());

        assertThat(discovered).allMatch(tu -> tu instanceof ExecutedInDiscovery);
    }

    @Test
    void detectsFailingTests() {
        findsAndRunsNTests(1, TestClassWithFailingTest.class);
        nTestsFails(1, TestClassWithFailingTest.class);
    }

    @Test
    void detectsFailingSpockTests() {
        findsAndRunsNTests(1, TestSpecWithFailingFeature.class);
        nTestsFails(1, TestSpecWithFailingFeature.class);
        errorIsRecorded(t -> t instanceof ConditionNotSatisfiedError, TestSpecWithFailingFeature.class);
    }

    @Test
    void detectsErroringTestsWhenPassingTestsPresent() {
        nTestsPass(2, TestClassWithMixedPassAndFail.class);
        nTestsFails(2, TestClassWithMixedPassAndFail.class);
        errorIsRecorded(t -> t instanceof RuntimeException, TestClassWithMixedPassAndFail.class);
    }

    @Test
    void detectsErroringSpockTestsWhenPassingTestsPresent() {
        nTestsPass(2, TestSpecWithMixedPassAndFail.class);
        nTestsFails(2, TestSpecWithMixedPassAndFail.class);
    }

    @Test
    void findsAndRunsParameterizedTests() {
        findsAndRunsNTests(4, TestClassWithParameterizedTestAnnotation.class);
    }

    @Test
    void findsAndRunsParameterizedTestsWithoutExplicitMethodSource() {
        findsAndRunsNTests(2, ParameterizedNoExplicitSource.class);
    }


    @Test
    void findsAndRunsTestsWithRepeatedATestAnnotation() {
        findsAndRunsNTests(1, TestClassWithRepeatedTestAnnotation.class);
    }

    @Test
    void findsAndRunsTestsFromTestFactoryAnnotation() {
        findsAndRunsNTests(1, TestClassWithTestFactoryAnnotation.class);
    }

    @Test
    void findsAndRunsTestsFromTestTemplateAnnotation() {
        findsAndRunsNTests(1, TestClassWithTestTemplateAnnotation.class);
    }

    @Test
    void findsAndRunsTestsFromDataDrivenSpockFeature() {
        findsAndRunsNTests(2, TestSpecWithDataDrivenFeature.class);
    }

    @Test
    void findsAndRunsTestsFromClassWithNestedAnnotationAndNestedTestAnnotation() {
        findsAndRunsNTests(1, TestClassWithNestedAnnotationAndNestedTestAnnotation.class);
        findsAndRunsNTests(0, TestClassWithNestedAnnotationAndNestedTestAnnotation.NestedClass.class);
    }

    @Test
    void findsAndRunsTestsFromClassWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        findsAndRunsNTests(1, TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation.class);
        findsAndRunsNTests(0, TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.class);
    }

    @Test
    void findsAndRunsTestsWhenMultipleNesting() {
        findsAndRunsNTests(1, TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.class);
        findsAndRunsNTests(0, TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.NestedClass.class);
        findsAndRunsNTests(0, TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.NestedClass.NestedNestedClass.class);
    }

    @Test
    void findsAndRunsTestsWhenMultipleNestingWithTestFactories() {
        findsAndRunsNTests(1, TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.class);
        findsAndRunsNTests(0, TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.class);
        findsAndRunsNTests(0, TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.NestedNestedClass.class);
    }

    @Test
    void findsNoTestsWhenNoTestAnnotations() {
        findsAndRunsNTests(0, TestClassWithoutAnnotations.class);
    }

    @Test
    void findsNoSpockTestsWhenNoFeaturesDefined() {
        findsAndRunsNTests(0, TestSpecWithoutFeatures.class);
    }

    @Test
    void findsNoTestsInOuterClassWhenNestedAnnotationPresent() {
        findsAndRunsNTests(0, TestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation.class);
    }

    @Test
    void findsNoTestsInOuterClassWhenNestedAnnotationPresentForFactory() {
        findsAndRunsNTests(0, TestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation.class);
    }

    @Test
    void findsInheritedTests() {
        findsAndRunsNTests(1, TestClassWithInheritedTestMethod.class);
    }

    @Test
    void findsInheritedSpockTests() {
        findsAndRunsNTests(1, TestSpecWithInheritedFeature.class);
    }

    @Test
    void findsTestsIncludedByMethodName() {
        findsAndRunsNTests(1, new JUnit5TestUnitFinder(new TestGroupConfig(), singletonList("included")), TestClassWithIncludedTestMethod.class);
    }

    @Test
    void findsSpockTestsIncludedByMethodName() {
        findsAndRunsNTests(1, new JUnit5TestUnitFinder(new TestGroupConfig(), singletonList("included")), TestSpecWithIncludedFeature.class);
    }

    @Test
    void excludesTestsByTag() {
        findsAndRunsNTests(3, new JUnit5TestUnitFinder(new TestGroupConfig().withExcludedGroups("excluded"), emptyList()), TestClassWithTags.class);
    }

    @Test
    void excludesIsEmpty() {
        findsAndRunsNTests(4, new JUnit5TestUnitFinder(new TestGroupConfig().withExcludedGroups(""), emptyList()), TestClassWithTags.class);
    }

    @Test
    void excludesSpockTestsByTag() {
        findsAndRunsNTests(3, new JUnit5TestUnitFinder(new TestGroupConfig().withExcludedGroups("excluded"), emptyList()), TestSpecWithTags.class);
    }

    @Test
    void includesTestsByTag() {
        findsAndRunsNTests(1, new JUnit5TestUnitFinder(new TestGroupConfig().withIncludedGroups("included"), emptyList()), TestClassWithTags.class);
    }

    @Test
    void includesIsEmpty() {
        findsAndRunsNTests(4, new JUnit5TestUnitFinder(new TestGroupConfig().withIncludedGroups(""), emptyList()), TestClassWithTags.class);
    }

    @Test
    void includesSpockTestsByTag() {
        findsAndRunsNTests(1, new JUnit5TestUnitFinder(new TestGroupConfig().withIncludedGroups("included"), emptyList()), TestSpecWithTags.class);
    }

    @Test
    void findsNoTestsInAbstractTestClass() {
        findsAndRunsNTests(0, AbstractTestClass.class);
    }

    @Test
    void findsNoTestsInTestInterface() {
        findsAndRunsNTests(0, InterfaceTestClass.class);
    }

    @Test
    void findsAndRunsAbortedTest() {
        findsAndRunsNTests(1, TestClassWithAbortingTest.class);
    }

    @Test
    void findsAndRunsAbortedSpockTest() {
        findsAndRunsNTests(3, TestSpecWithAbortingFeature.class);
    }

    @Test
    void findsAndRunsTestsWithAfterAll() {
        findsAndRunsNTests(2, TestClassWithAfterAll.class);
    }

    @Test
    void findsAndRunsAtomicTestWithCleanupSpec() {
        findsAndRunsNTests(1, TestSpecWithCleanupSpec.class);
    }

    @Test
    void findsAndRunsTestsWithBeforeAll() {
        findsAndRunsNTests(2, TestClassWithBeforeAll.class);
    }

    @Test
    void findsAndRunsAtomicTestWithSetupSpec() {
        findsAndRunsNTests(1, TestSpecWithSetupSpec.class);
    }

    @Test
    void findsAndRunsTestsWithFailingAfterAll() {
        findsAndRunsNTests(2, TestClassWithFailingAfterAll.class);
        errorIsRecorded(t -> t instanceof AssertionFailedError, TestClassWithFailingAfterAll.class);
    }

    @Test
    void findsAndRunsAtomicTestWithFailingCleanupSpec() {
        findsAndRunsNTests(1, TestSpecWithFailingCleanupSpec.class);
    }

    @Test
    void findsNoTestsWithFailingBeforeAll() {
        findsAndRunsNTests(0, TestClassWithFailingBeforeAll.class);
    }

    @Test
    void findsAndRunsAtomicTestWithFailingSetupSpec() {
        findsAndRunsNTests(1, TestSpecWithFailingSetupSpec.class);
    }

    @Test
    void findsNoTestsWithNestedTestClassWithoutAnnotations() {
        findsAndRunsNTests(0, TestClassWithNestedClassWithoutAnnotations.class);
    }

    @Test
    void findsAndRunsAtomicTestWithAfterAll() {
        findsAndRunsNTests(1, TestSpecWithAfterAll.class);
    }

    @Test
    void findsAndRunsAtomicTestWithAfterClass() {
        findsAndRunsNTests(1, TestSpecWithAfterClass.class);
    }

    @Test
    void findsAndRunsAtomicTestWithBeforeAll() {
        findsAndRunsNTests(1, TestSpecWithBeforeAll.class);
    }

    @Test
    void findsAndRunsAtomicTestWithBeforeClass() {
        findsAndRunsNTests(1, TestSpecWithBeforeClass.class);
    }

    @Test
    void findsAndRunsAtomicTestWithClassRuleField() {
        findsAndRunsNTests(1, TestSpecWithClassRuleField.class);
    }

    @Test
    void findsAndRunsAtomicTestWithClassRuleMethod() {
        findsAndRunsNTests(1, TestSpecWithClassRuleMethod.class);
    }

    @Test
    void findsAndRunsAtomicTestWithSetupSpecWithoutShared() {
        findsAndRunsNTests(1, TestSpecWithSetupSpecWithoutShared.class);
    }

    @Test
    void findsAndRunsAtomicTestWithShared() {
        findsAndRunsNTests(1, TestSpecWithShared.class);
    }

    @Test
    void findsAndRunsAtomicTestWithStepwise() {
        findsAndRunsNTests(1, TestSpecWithStepwise.class);
    }

    @Test
    void findsAndRunsAtomicTestWithStepwiseFeature() {
        findsAndRunsNTests(1, TestSpecWithStepwiseFeature.class);
    }

    @Test
    void findsAndRunsCucumberTests() {
        findsAndRunsNTests(1, RunCucumberTest.class);
    }

    private void findsAndRunsNTests(int n, Class<?> clazz) {
        findsAndRunsNTests(n, basicConfig(), clazz);
    }

    private void findsAndRunsNTests(int n, JUnit5TestUnitFinder underTest, Class<?> clazz) {
        RecordingListener l = run(underTest, clazz);
        assertThat(l.started).hasSize(n);
    }

    private void nTestsPass(int n, Class<?> clazz) {
        RecordingListener l = run(basicConfig(), clazz);
        assertThat(l.passed).hasSize(n);
    }

    private void nTestsFails(int n, Class<?> clazz) {
        RecordingListener l = run(basicConfig(), clazz);
        assertThat(l.failed).hasSize(n);
    }

    private void errorIsRecorded(Predicate<Throwable> p, Class<?> clazz) {
        RecordingListener l = run(basicConfig(), clazz);
        assertThat(l.errors).anyMatch(p);
    }

    private RecordingListener run(JUnit5TestUnitFinder underTest, Class<?> clazz) {
        RecordingListener l = new RecordingListener();
        underTest.findTestUnits(clazz, l);
        return l;
    }

    private JUnit5TestUnitFinder basicConfig() {
        return new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList());
    }

}

class RecordingListener implements TestUnitExecutionListener {
    List<Description> started = new ArrayList<>();
    List<Description> failed = new ArrayList<>();
    List<Description> passed = new ArrayList<>();

    List<Throwable> errors = new ArrayList<>();

    @Override
    public void executionStarted(Description description) {
        started.add(description);
    }

    @Override
    public void executionFinished(Description description, boolean pass, Throwable optional) {
        if (pass) {
            passed.add(description);
        } else {
            failed.add(description);
        }

        if (optional != null) {
            errors.add(optional);
        }
    }
}
