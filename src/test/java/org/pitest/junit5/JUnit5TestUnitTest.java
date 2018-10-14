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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.pitest.junit5.repository.TestClassWithAbortingTest;
import org.pitest.junit5.repository.TestClassWithFailingTest;
import org.pitest.junit5.repository.TestClassWithInheritedTestMethod;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithRepeatedTestAllInvocationsSucceeding;
import org.pitest.junit5.repository.TestClassWithRepeatedTestSecondInvocationFailing;
import org.pitest.junit5.repository.TestClassWithTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTestFactoryAnnotation;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.execute.ExitingResultCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 *
 * @author tobias
 */
public class JUnit5TestUnitTest {

    @Test
    public void testTestClassWithTestAnnotation() {
        TestResultCollector resultCollector = runTestsIn(TestClassWithTestAnnotation.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    public void test3TestClassWithTestFactoryAnnotation() {
        TestResultCollector resultCollector = runTestsIn(TestClassWithTestFactoryAnnotation.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationAndNestedTestAnnotation() {
        TestResultCollector resultCollector =
        runTestsIn(TestClassWithNestedAnnotationAndNestedTestAnnotation.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }


    @Test
    public void testTestClassWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        TestResultCollector resultCollector =
        runTestsIn(TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation() {
        TestResultCollector resultCollector =
        runTestsIn(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        TestResultCollector resultCollector =
        runTestsIn(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    public void testTestClassWithInheritedTestMethod() {
        TestResultCollector resultCollector =
        runTestsIn(TestClassWithInheritedTestMethod.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
    }

    @Test
    public void testTestClassWithFailingTest() {
        TestResultCollector resultCollector = runTestsIn(TestClassWithFailingTest.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
        assertThat(resultCollector.getFailure()).isPresent();
    }

    @Test
    public void testTestClassWithAbortingTest() {
        TestResultCollector resultCollector = runTestsIn(TestClassWithAbortingTest.class);

        assertThat(resultCollector.getSkipped()).isEmpty();
        assertThat(resultCollector.getStarted()).hasSize(1);
        assertThat(resultCollector.getEnded()).hasSize(1);
        assertThat(resultCollector.getFailure()).isEmpty();
    }


    @Test
    void testClassWithRepeatedSuccessfulTests() {
        TestResultCollector testCollector = runTestsIn(
                TestClassWithRepeatedTestAllInvocationsSucceeding.class);

        // Includes one for a test container plus the number of repetitions.
        int numLifecycleEvents = 1 + TestClassWithRepeatedTestAllInvocationsSucceeding.NUM_REPETITIONS;
        assertAll(
                () -> assertThat(testCollector.getStarted())
                        .hasSize(numLifecycleEvents),
                () -> assertThat(testCollector.getEnded())
                        .hasSize(numLifecycleEvents),
                () -> assertThat(testCollector.getSkipped())
                        .isEmpty()
        );
    }

    @RepeatedTest(2) // Run repeatedly to check that previous executions do not affect subsequent
    @DisplayName("Test execution shall stop once failing test is discovered")
    void testClassWithRepeatedFailingTests() {
        TestResultCollector testCollector = new TestResultCollector();
        ResultCollector exitingCollector = new ExitingResultCollector(testCollector);

        runTestsIn(TestClassWithRepeatedTestSecondInvocationFailing.class, exitingCollector);


        // Includes one for a test container plus the number of repetitions
        // until the first failure.
        int numExpectedExecutions =
                1 + TestClassWithRepeatedTestSecondInvocationFailing.FAILING_REPETITION_NUM;
        int numSkipped = TestClassWithRepeatedTestSecondInvocationFailing.NUM_REPETITIONS
                - TestClassWithRepeatedTestSecondInvocationFailing.FAILING_REPETITION_NUM;
        assertAll(
                () -> assertThat(testCollector.getStarted())
                        .hasSize(numExpectedExecutions),
                () -> assertThat(testCollector.getEnded())
                        .hasSize(numExpectedExecutions),
                () -> assertThat(testCollector.getSkipped())
                        .hasSize(numSkipped)
        );
    }

    @RepeatedTest(10) // Run repeatedly to check that previous executions
    // do not affect subsequent and to increase the likelihood of discovering threading problems
    @DisplayName("Test execution shall stop once failing test is discovered but shall not affect other launchers")
    void testClassWithRepeatedFailingTestsFromMultipleThreads() throws InterruptedException {
        int numThreads = 4;
        CountDownLatch startLatch = new CountDownLatch(numThreads);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        ConcurrentMap<String, Throwable> testExceptions = new ConcurrentHashMap<>();
        for (int i = 0; i < numThreads; i++) {
            String threadName = "T" + i;
            Thread thread = new Thread(() -> {
                startLatch.countDown();
                try {
                    // Wait for other threads to start
                    startLatch.await();

                    // Invoke the test unit with repeated tests
                    testClassWithRepeatedFailingTests();
                } catch (Throwable t) {
                    testExceptions.put(threadName, t);
                } finally {
                    finishLatch.countDown();
                }
            }, threadName);

            thread.start();
        }
        finishLatch.await();

        assertThat(testExceptions)
                .as("Expected no exceptions in test threads")
                .isEmpty();
    }

    private TestResultCollector runTestsIn(Class<?> clazz) {
        TestResultCollector resultCollector = new TestResultCollector();
        runTestsIn(clazz, resultCollector);
        return resultCollector;
    }

    private void runTestsIn(Class<?> clazz, ResultCollector resultCollector) {
        new JUnit5TestUnitFinder().findTestUnits(clazz)
                .stream()
                .forEach(testUnit -> testUnit.execute(resultCollector));
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

      // todo: Is it OK for a test collector to control whether a test must stop?
      //   Shan't such commands come explicitly from some other abstraction?
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
