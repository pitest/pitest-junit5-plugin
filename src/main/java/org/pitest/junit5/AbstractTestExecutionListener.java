package org.pitest.junit5;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

abstract class AbstractTestExecutionListener implements TestExecutionListener {
    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
    }
}
