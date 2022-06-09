package org.pitest.junit5.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class TestClassWithFailingBeforeAll {

    @BeforeAll
    static void fails() {
        fail();
    }

    @Test
    void aTest() {

    }

    @Test
    void anotherTest() {

    }

}
