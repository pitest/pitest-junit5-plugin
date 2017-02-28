package org.pitest.junit5.repository;

import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.DynamicTest;

/**
 *
 * @author Tobias Stadler
 */
public class TestClassWithoutAnnotations {

    public void test() {

    }

    public Collection<DynamicTest> testFactory() {
        return Collections.emptyList();
    }

    public static class NestedClasss {

        public void test() {

        }

        public Collection<DynamicTest> testFactory() {
            return Collections.emptyList();
        }

    }

}
