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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithParameterizedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithRepeatedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithTestTemplateAnnotation;
import org.pitest.junit5.repository.TestClassWithoutAnnotations;

/**
 *
 * @author Tobias Stadler
 */
public class JUnit5TestUnitFinderTest {

    public JUnit5TestUnitFinderTest() {
    }

    @Test
    public void testTestClassWithParameterizedTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithParameterizedTestAnnotation.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithRepeatedTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithRepeatedTestAnnotation.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithTestAnnotation.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithTestFactoryAnnotation() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithTestFactoryAnnotation.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithTestTemplateAnnotation() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithTestTemplateAnnotation.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationAndNestedTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationAndNestedTestAnnotation.class)).isEmpty();
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationAndNestedTestAnnotation.NestedClass.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation.class)).isEmpty();
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.class)).isEmpty();
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.NestedClass.class)).isEmpty();
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.NestedClass.NestedNestedClass.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.class)).isEmpty();
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.class)).isEmpty();
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.NestedNestedClass.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithoutAnnotations() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithoutAnnotations.class)).isEmpty();
    }

    @Test
    public void testTestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation.class)).isEmpty();
    }

    @Test
    public void testTestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        assertThat(new JUnit5TestUnitFinder().findTestUnits(TestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation.class)).isEmpty();
    }

}
