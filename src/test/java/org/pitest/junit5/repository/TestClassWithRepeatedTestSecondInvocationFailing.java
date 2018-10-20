package org.pitest.junit5.repository;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import static org.junit.jupiter.api.Assertions.fail;

public class TestClassWithRepeatedTestSecondInvocationFailing {

    public static final int NUM_REPETITIONS = 3;
    public static final int FAILING_REPETITION_NUM = 2;

    @RepeatedTest(NUM_REPETITIONS)
    void oneInvocationFailing(RepetitionInfo info) {
        if (info.getCurrentRepetition() == FAILING_REPETITION_NUM) {
            fail("Failing the second repetition. Subsequent must be skipped");
        }
    }
}
