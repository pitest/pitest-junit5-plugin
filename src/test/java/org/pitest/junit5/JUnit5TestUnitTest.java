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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.pitest.junit5.repository.TestClassWithAbortingTest;
import org.pitest.junit5.repository.TestClassWithAfterAll;
import org.pitest.junit5.repository.TestClassWithBeforeAll;
import org.pitest.junit5.repository.TestClassWithFailingAfterAll;
import org.pitest.junit5.repository.TestClassWithFailingBeforeAll;
import org.pitest.junit5.repository.TestClassWithFailingTest;
import org.pitest.junit5.repository.TestClassWithInheritedTestMethod;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTestFactoryAnnotation;
import org.pitest.testapi.Description;
import org.pitest.testapi.NullExecutionListener;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestGroupConfig;

/**
 *
 * @author tobias
 */
class JUnit5TestUnitTest {

    @Test
    void testTestClassWithTestAnnotation() {
        TestResultCollector resultCollector = findTestsIn(TestClassWithTestAnnotation.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    void test3TestClassWithTestFactoryAnnotation() {
        TestResultCollector resultCollector = findTestsIn(TestClassWithTestFactoryAnnotation.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    void testTestClassWithNestedAnnotationAndNestedTestAnnotation() {
        TestResultCollector resultCollector = 
        findTestsIn(TestClassWithNestedAnnotationAndNestedTestAnnotation.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }


    @Test
    void testTestClassWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        TestResultCollector resultCollector =
        findTestsIn(TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation() {
        TestResultCollector resultCollector = 
        findTestsIn(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.class);
     
        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        TestResultCollector resultCollector = 
        findTestsIn(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    void testTestClassWithInheritedTestMethod() {
        TestResultCollector resultCollector =
        findTestsIn(TestClassWithInheritedTestMethod.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    void testTestClassWithFailingTest() {
        TestResultCollector resultCollector = findTestsIn(TestClassWithFailingTest.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
        assertThat(resultCollector.getFailure()).isPresent();
    }

    @Test
    void testTestClassWithAbortingTest() {
        TestResultCollector resultCollector = findTestsIn(TestClassWithAbortingTest.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
        assertThat(resultCollector.getFailure()).isEmpty();
    }

    @Test
    void testRunsBeforeAlls() {
        TestResultCollector resultCollector = findTestsIn(TestClassWithBeforeAll.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(2);
        assertThat(resultCollector.getFailure()).isEmpty();
    }

    @Test
    void testFailsWhenBeforeAllFails() {
        TestResultCollector resultCollector = findTestsIn(TestClassWithFailingBeforeAll.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(0);
        assertThat(resultCollector.getFailure()).isPresent();
    }

    @Test
    void runsAfterAlls() {
        TestResultCollector resultCollector = findTestsIn(TestClassWithAfterAll.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(2);
        assertThat(resultCollector.getFailure()).isEmpty();
    }

    @Test
    void testFailsWhenAfterAllFails() {
        TestResultCollector resultCollector = findTestsIn(TestClassWithFailingAfterAll.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        // We get 4 start notifications, 1 for each test and again for the container. Not clear
        // what consequence this has.
        //assertThat(resultCollector.getStarted()).hasSize(2);
        assertThat(resultCollector.getFailure()).isPresent();
    }

    private TestResultCollector findTestsIn(Class<?> clazz) {
      TestResultCollector resultCollector = new TestResultCollector();
      new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(clazz, new NullExecutionListener())
      .stream()
      .forEach(testUnit -> testUnit.execute(resultCollector));
      return resultCollector;
    }

    private static class TestResultCollector implements ResultCollector {

      private final List<Description> skipped = new ArrayList<>();
      private final List<Description> started = new ArrayList<>();
      private final List<Description> ended = new ArrayList<>();
      private volatile Throwable failure;

      @Override
      public void notifyEnd(Description description, Throwable t) {
          this.failure = t;
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

      public Optional<Throwable> getFailure() {
          return Optional.ofNullable(failure);
      }

  }
    
}
