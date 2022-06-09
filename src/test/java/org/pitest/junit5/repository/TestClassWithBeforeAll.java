package org.pitest.junit5.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class TestClassWithBeforeAll {
    static boolean fail = true;

    @BeforeAll
    static void fails() {
        fail = false;
    }

    @Test
    void aTest() {
        if (fail) {
            fail();
        }
    }

    @Test
    void anotherTest() {
        if (fail) {
            fail();
        }
    }
}
