package org.pitest.junit5.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class TestClassWithMixedPassAndFail {

    @Test
    void passingTest() {

    }

    @Test
    void passingTest2() {

    }

    @Test
    void failingTest() {
        fail();
    }

    @Test
    void erroringTest() {
        throw new RuntimeException();
    }
}
