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
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTestFactoryAnnotation;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.util.IsolationUtils;

/**
 *
 * @author tobias
 */
public class JUnit5TestUnitTest {

    private static class TestResultCollector implements ResultCollector {

        private final List<Description> skipped = new ArrayList<>();
        private final List<Description> started = new ArrayList<>();
        private final List<Description> ended = new ArrayList<>();

        @Override
        public void notifyEnd(Description description, Throwable t) {
            notifyEnd(description);
        }

        @Override
        public void notifyEnd(Description description) {
            ended.add(description);
        }

        @Override
        public void notifyStart(Description description) {
            started.add(description);
        }

        @Override
        public void notifySkipped(Description description) {
            skipped.add(description);
        }

        @Override
        public boolean shouldExit() {
            return false;
        }

        public List<Description> getSkipped() {
            return skipped;
        }

        public List<Description> getStarted() {
            return started;
        }

        public List<Description> getEnded() {
            return ended;
        }

    }

    @Test
    public void testTestClassWithTestAnnotation() {
        TestResultCollector resultCollector = new TestResultCollector();
        new JUnit5TestUnitFinder().findTestUnits(TestClassWithTestAnnotation.class).stream().forEach(testUnit -> testUnit.execute(IsolationUtils.getContextClassLoader(), resultCollector));

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    public void test3TestClassWithTestFactoryAnnotation() {
        TestResultCollector resultCollector = new TestResultCollector();
        new JUnit5TestUnitFinder().findTestUnits(TestClassWithTestFactoryAnnotation.class).stream().forEach(testUnit -> testUnit.execute(IsolationUtils.getContextClassLoader(), resultCollector));

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationAndNestedTestAnnotation() {
        TestResultCollector resultCollector = new TestResultCollector();
        new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationAndNestedTestAnnotation.NestedClass.class).stream().forEach(testUnit -> testUnit.execute(IsolationUtils.getContextClassLoader(), resultCollector));

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        TestResultCollector resultCollector = new TestResultCollector();
        new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.class).stream().forEach(testUnit -> testUnit.execute(IsolationUtils.getContextClassLoader(), resultCollector));

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation() {
        TestResultCollector resultCollector = new TestResultCollector();
        new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.NestedClass.NestedNestedClass.class).stream().forEach(testUnit -> testUnit.execute(IsolationUtils.getContextClassLoader(), resultCollector));

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        TestResultCollector resultCollector = new TestResultCollector();
        new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.NestedNestedClass.class).stream().forEach(testUnit -> testUnit.execute(IsolationUtils.getContextClassLoader(), resultCollector));

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

}
