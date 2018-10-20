package org.pitest.junit5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConditionalTestFilterTest {

    private static final String TEST_EXECUTION_ID = "execution-1";

    @Mock
    private TestUnitExecutionsRegistry executions;

    private ConditionalTestFilter filter;

    @BeforeEach
    void setUp() {
        when(executions.getStatus(TEST_EXECUTION_ID))
                .thenReturn(TestUnitExecutionsRegistry.Status.KEEP_RUNNING);
        filter = new ConditionalTestFilter(executions);
    }

    @Test
    void enabledOnceInstantiated() {
        ConditionEvaluationResult result =
                filter.evaluateExecutionCondition(getExtensionContext());
        assertFalse(result.isDisabled());
    }

    @Test
    void filtersOnceDisabled() {
        rejectSubsequentTests();
        ConditionEvaluationResult result =
                filter.evaluateExecutionCondition(getExtensionContext());
        assertTrue(result.isDisabled());
    }

    @RepeatedTest(16) // fixme: does this test make any sense once we use mock?
    void filtersOnceDisabledFromDifferentThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Thread t = new Thread(() -> {
            rejectSubsequentTests();
            latch.countDown();
        });
        t.start();

        latch.await(); // fixme: It doesn't test what it shall as #countDown -HB-> #await.

        ConditionEvaluationResult result =
                filter.evaluateExecutionCondition(getExtensionContext());
        assertTrue(result.isDisabled());
    }

    private ExtensionContext getExtensionContext() {
        ExtensionContext context = mock(ExtensionContext.class);
        when(context.getConfigurationParameter(ConditionalTestFilter.EXECUTION_ID_KEY))
                .thenReturn(Optional.of(TEST_EXECUTION_ID));
        return context;
    }

    private void rejectSubsequentTests() {
        when(executions.getStatus(TEST_EXECUTION_ID))
                .thenReturn(TestUnitExecutionsRegistry.Status.ABORT);
    }
}
