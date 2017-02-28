/*
 * Copyright 2017 Tobias Stadler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.junit5;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.pitest.classinfo.Repository;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.junit5.repository.AbstractTestClass;
import org.pitest.junit5.repository.InterfaceTestClass;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithoutAnnotations;
import org.pitest.util.IsolationUtils;

/**
 *
 * @author Tobias Stadler
 */
public class JUnit5TestClassIdentifierTest {

    private final Repository repository;

    public JUnit5TestClassIdentifierTest() {
        repository = new Repository(new ClassloaderByteArraySource(IsolationUtils.getContextClassLoader()));
    }

    @Test
    public void testTestClassWithTestAnnotation() {
        assertTrue(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithTestAnnotation.class).value()));
    }

    @Test
    public void test3TestClassWithTestFactoryAnnotation() {
        assertTrue(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithTestFactoryAnnotation.class).value()));
    }

    @Test
    public void testTestClassWithNestedAnnotationAndNestedTestAnnotation() {
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedAnnotationAndNestedTestAnnotation.class).value()));
        assertTrue(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedAnnotationAndNestedTestAnnotation.NestedClass.class).value()));
    }

    @Test
    public void testTestClassWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation.class).value()));
        assertTrue(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.class).value()));
    }

    @Test
    public void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation() {
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.class).value()));
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.NestedClass.class).value()));
        assertTrue(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.NestedClass.NestedNestedClass.class).value()));
    }

    @Test
    public void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.class).value()));
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.class).value()));
        assertTrue(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.NestedNestedClass.class).value()));
    }

    @Test
    public void testTestClassWithoutAnnotations() {
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithoutAnnotations.class).value()));
    }

    @Test
    public void testTestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation() {
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation.class).value()));
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation.NestedClass.class).value()));
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation.NestedClass.NestedNestedClass.class).value()));
    }

    @Test
    public void testTestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation.class).value()));
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.class).value()));
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(TestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.NestedNestedClass.class).value()));
    }

    @Test
    public void testInterfaceTestClass() {
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(InterfaceTestClass.class).value()));
    }

    @Test
    public void testAbstractTestClass() {
        assertFalse(new JUnit5TestClassIdentifier().isATestClass(repository.fetchClass(AbstractTestClass.class).value()));
    }

}
