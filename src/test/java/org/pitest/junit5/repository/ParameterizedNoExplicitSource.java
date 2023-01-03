package org.pitest.junit5.repository;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ParameterizedNoExplicitSource {

    @ParameterizedTest
    @MethodSource()
    public void parameterizedTest(String string) {

    }

    static Stream<String> parameterizedTest() {
        return Stream.of("foo", "bar");
    }
}
