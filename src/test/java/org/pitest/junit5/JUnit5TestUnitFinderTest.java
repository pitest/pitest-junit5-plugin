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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.pitest.junit5.repository.TestClassWithIncludedTestMethod;
import org.pitest.junit5.repository.TestClassWithInheritedTestMethod;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithParameterizedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithRepeatedTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTags;
import org.pitest.junit5.repository.TestClassWithTestAnnotation;
import org.pitest.junit5.repository.TestClassWithTestFactoryAnnotation;
import org.pitest.junit5.repository.TestClassWithTestTemplateAnnotation;
import org.pitest.junit5.repository.TestClassWithoutAnnotations;
import org.pitest.testapi.TestGroupConfig;

/**
 *
 * @author Tobias Stadler
 */
public class JUnit5TestUnitFinderTest {

    public JUnit5TestUnitFinderTest() {
    }

    @Test
    public void testTestClassWithParameterizedTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithParameterizedTestAnnotation.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithRepeatedTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithRepeatedTestAnnotation.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithTestAnnotation.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithTestFactoryAnnotation() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithTestFactoryAnnotation.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithTestTemplateAnnotation() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithTestTemplateAnnotation.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithNestedAnnotationAndNestedTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedAnnotationAndNestedTestAnnotation.class)).hasSize(1);
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedAnnotationAndNestedTestAnnotation.NestedClass.class)).isEmpty();
    }

    @Test
    public void testTestClassWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation.class)).hasSize(1);
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.class)).isEmpty();
    }

    @Test
    public void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.class)).hasSize(1);
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.NestedClass.class)).isEmpty();
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestAnnotation.NestedClass.NestedNestedClass.class)).isEmpty();
    }

    @Test
    public void testTestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.class)).hasSize(1);
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.class)).isEmpty();
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedAnnotationWithNestedAnnotationAndNestedTestFactoryAnnotation.NestedClass.NestedNestedClass.class)).isEmpty();
    }

    @Test
    public void testTestClassWithoutAnnotations() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithoutAnnotations.class)).isEmpty();
    }

    @Test
    public void testTestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedClassWithNestedAnnotationAndNestedTestAnnotation.class)).isEmpty();
    }

    @Test
    public void testTestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithNestedClassWithNestedAnnotationAndNestedTestFactoryAnnotation.class)).isEmpty();
    }

    @Test
    public void testTestClassWithInheritedTestMethod() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), emptyList()).findTestUnits(TestClassWithInheritedTestMethod.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithIncludedTestMethod() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig(), singletonList("included")).findTestUnits(TestClassWithIncludedTestMethod.class)).hasSize(1);
    }

    @Test
    public void testTestClassWithExcludedTag() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig().withExcludedGroups("excluded"), emptyList()).findTestUnits(TestClassWithTags.class)).hasSize(3);
    }

    @Test
    public void testTestClassWithIncludedTag() {
        assertThat(new JUnit5TestUnitFinder(new TestGroupConfig().withIncludedGroups("included"), emptyList()).findTestUnits(TestClassWithTags.class)).hasSize(1);
    }
}
