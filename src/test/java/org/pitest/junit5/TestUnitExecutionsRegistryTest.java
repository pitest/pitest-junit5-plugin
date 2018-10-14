package org.pitest.junit5;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TestUnitExecutionsRegistryTest {

    private TestUnitExecutionsRegistry executions;

    @Test
    void keepRunningUnknownExecution() {
        executions = new TestUnitExecutionsRegistry();
        assertThat(executions.getStatus("unknown"))
                .isEqualTo(TestUnitExecutionsRegistry.Status.KEEP_RUNNING);
    }

    @Test
    void keepRunningRegisteredExecution() {
        executions = new TestUnitExecutionsRegistry();
        String executionId = "execution-1";
        executions.add(executionId);
        assertThat(executions.getStatus(executionId))
                .isEqualTo(TestUnitExecutionsRegistry.Status.KEEP_RUNNING);
    }

    @Test
    void keepRunningOnceUnregisteredExecution() {
        executions = new TestUnitExecutionsRegistry();
        String executionId = "execution-1";
        executions.add(executionId);
        executions.abortExecution(executionId);
        executions.remove(executionId);

        assertThat(executions.getStatus(executionId))
                .isEqualTo(TestUnitExecutionsRegistry.Status.KEEP_RUNNING);
    }

    @Test
    void abortUnknownExecutionFails() {
        executions = new TestUnitExecutionsRegistry();
        String executionId = "unknown";
        assertThrows(IllegalArgumentException.class,
                () -> executions.abortExecution(executionId));

        assertThat(executions.getStatus(executionId))
                .isEqualTo(TestUnitExecutionsRegistry.Status.KEEP_RUNNING);
    }

    @Test
    void abortRegisteredExecutionOnceRequested() {
        executions = new TestUnitExecutionsRegistry();
        String executionId = "execution-1";
        executions.add(executionId);
        executions.abortExecution(executionId);
        assertThat(executions.getStatus(executionId))
                .isEqualTo(TestUnitExecutionsRegistry.Status.ABORT);
    }

    @Test
    void doubleRegistrationWontCancelAbort() {
        executions = new TestUnitExecutionsRegistry();
        String executionId = "execution-1";
        executions.add(executionId);
        executions.abortExecution(executionId);

        assertThrows(IllegalArgumentException.class,
                () -> executions.add(executionId));

        assertThat(executions.getStatus(executionId))
                .isEqualTo(TestUnitExecutionsRegistry.Status.ABORT);
    }

}
