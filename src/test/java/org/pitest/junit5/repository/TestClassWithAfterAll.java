package org.pitest.junit5.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

public class TestClassWithAfterAll {

    @AfterAll
    static void hello() {

    }

    @Test
    void aTest() {

    }

    @Test
    void anotherTest() {

    }
}
