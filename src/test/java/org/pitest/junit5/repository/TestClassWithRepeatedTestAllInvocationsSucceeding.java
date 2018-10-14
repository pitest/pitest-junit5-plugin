package org.pitest.junit5.repository;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestClassWithRepeatedTestAllInvocationsSucceeding {

    public static final int NUM_REPETITIONS = 3;

    @RepeatedTest(NUM_REPETITIONS)
    void test(RepetitionInfo info) {
        assertNotNull(info);
    }
}
