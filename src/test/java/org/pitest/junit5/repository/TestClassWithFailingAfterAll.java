package org.pitest.junit5.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class TestClassWithFailingAfterAll {

    @AfterAll
    static void oops() {
      fail();
    }

    @Test
    void aTest() {

    }

    @Test
    void anotherTest() {

    }
}
