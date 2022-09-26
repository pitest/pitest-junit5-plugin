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
import org.pitest.junit5.cucumber.RunCucumberTest;
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
import org.pitest.junit5.repository.TestClassWithParameterizedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithRepeatedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTags;
import org.pitest.junit5.repository.TestClassWithTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithTestTemplateAnnotation;
import org.pitest.junit5.repository.TestClassWithoutAnnotations;
import org.pitest.testapi.Description;
import org.pitest.testapi.ExecutedInDiscovery;
import org.pitest.testapi.NullExecutionListener;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitExecutionListener;

import java.util.ArrayList;
import java.util.List;
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
    void findsAndRunsBasicJupiterTestsWhenMultiplePresent() {
        findsAndRunsNTests(3, TestClassWithMultiplePassingTests.class);
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
    void discoveredTestsAreMarkedAsExecuted() {
       JUnit5TestUnitFinder underTest = basicConfig();
       List<TestUnit> discovered = underTest.findTestUnits(TestClassWithMultiplePassingTests.class,
               new NullExecutionListener());

        assertThat(discovered).allMatch(tu -> tu instanceof ExecutedInDiscovery);
    }

    @Test
    void detectsFailingTests() {
        findsAndRunsNTests(1, TestClassWithFailingTest.class);
        nTestsFails(1, TestClassWithFailingTest.class);
    }

    @Test
    void detectsErroringTestsWhenPassingTestsPresent() {
        nTestsPass(2, TestClassWithMixedPassAndFail.class);
        nTestsFails(2, TestClassWithMixedPassAndFail.class);
    }


    @Test
    void findsAndRunsParameterizedTests() {
        findsAndRunsNTests(2, TestClassWithParameterizedTestAnnotation.class);
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
    void findsTestsIncludedByMethodName() {
        findsAndRunsNTests(1, new JUnit5TestUnitFinder(new TestGroupConfig(), singletonList("included")), TestClassWithIncludedTestMethod.class);
    }

    @Test
    void excludesTestsByTag() {
        findsAndRunsNTests(3, new JUnit5TestUnitFinder(new TestGroupConfig().withExcludedGroups("excluded"), emptyList()), TestClassWithTags.class);
    }

    @Test
    void includesTestsByTag() {
        findsAndRunsNTests(1, new JUnit5TestUnitFinder(new TestGroupConfig().withIncludedGroups("included"), emptyList()), TestClassWithTags.class);
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
    TestUnitExecutionListener l = new TestUnitExecutionListener() {
        @Override
        public void executionStarted(Description description) {
            started.add(description);
        }

        @Override
        public void executionFinished(Description description, boolean pass) {
            if (pass) {
                passed.add(description);
            } else {
                failed.add(description);
            }
        }
    };

    @Override
    public void executionStarted(Description description) {
        started.add(description);
    }

    @Override
    public void executionFinished(Description description, boolean pass) {
        if (pass) {
            passed.add(description);
        } else {
            failed.add(description);
        }
    }
}