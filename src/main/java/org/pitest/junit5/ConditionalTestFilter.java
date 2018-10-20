package org.pitest.junit5;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A test filter accepting all tests until requested to reject
 * all subsequent tests through an {@linkplain TestUnitExecutionsRegistry executions registry}.
 */
public class ConditionalTestFilter implements ExecutionCondition {

    static final String EXECUTION_ID_KEY = "org.pitest.junit5.executionId";

    private static final ConditionEvaluationResult DISABLED = ConditionEvaluationResult.disabled(
            "Pitest has requested to skip remaining tests in a test unit");
    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(null);

    private final TestUnitExecutionsRegistry executionsRegistry;

    @SuppressWarnings("unused") // Used through reflection by the ServiceLoader
    // when this extension is requested by JUnit Jupiter Engine
    public ConditionalTestFilter() {
        this(TestUnitExecutionsRegistry.getInstance());
    }

    // Visible for testing to be able to inject executionsRegistry
    ConditionalTestFilter(TestUnitExecutionsRegistry executionsRegistry) {
        this.executionsRegistry = executionsRegistry;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        boolean shallReject = context.getConfigurationParameter(EXECUTION_ID_KEY)
                .map(this::shallRejectTests)
                .orElse(false);

        if (shallReject) {
            return DISABLED;
        } else {
            return ENABLED;
        }
    }

    private boolean shallRejectTests(String executionId) {
        return executionsRegistry.getStatus(executionId) == TestUnitExecutionsRegistry.Status.ABORT;
    }
}
