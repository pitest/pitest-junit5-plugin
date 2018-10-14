package org.pitest.junit5;

import org.pitest.testapi.ResultCollector;

import java.util.HashMap;
import java.util.Map;

/**
 * A singleton class keeping track of execution status of each test unit.
 * An execution is identified by a unique string identifier derived from
 * the {@link org.pitest.testapi.ResultCollector} supplied into
 * {@link JUnit5TestUnit#execute(ResultCollector)}.
 *
 * <p>Used to notify the {@link ConditionalTestFilter} that a certain unit
 * must be filtered.
 *
 * <p>A singleton is required because the {@link ConditionalTestFilter} is
 * instantiated by the Jupiter Test Engine, and our {@link JUnit5TestUnit}
 * does not seem to be able to inject any parameters into that extension.
 */
class TestUnitExecutionsRegistry {

    enum Status {
        KEEP_RUNNING,
        ABORT
    }

    private static final TestUnitExecutionsRegistry INSTANCE = new TestUnitExecutionsRegistry();

    private final Map<String, Status> executions;

    // Visible for testing
    TestUnitExecutionsRegistry() {
        executions = new HashMap<>();
    }

    synchronized void add(String executionId) {
        if (executions.containsKey(executionId)) {
            throw new IllegalArgumentException("Execution " + executionId
                    + " is already registered");
        }
        executions.put(executionId, Status.KEEP_RUNNING);
    }

    synchronized void remove(String executionId) {
        executions.remove(executionId);
    }

    synchronized void abortExecution(String executionId) {
        if (!executions.containsKey(executionId)) {
            throw new IllegalArgumentException("Cannot abort execution ("
                    + executionId + ") that is not registered");
        }
        executions.put(executionId, Status.ABORT);
    }

    // todo: consider fancier synchronization options?
    synchronized Status getStatus(String executionId) {
        return executions.getOrDefault(executionId, Status.KEEP_RUNNING);
    }

    static TestUnitExecutionsRegistry getInstance() {
        return INSTANCE;
    }
}
