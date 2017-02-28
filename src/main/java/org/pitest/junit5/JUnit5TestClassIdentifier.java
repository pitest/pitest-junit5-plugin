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

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.testapi.TestClassIdentifier;

/**
 *
 * @author Tobias Stadler
 */
public class JUnit5TestClassIdentifier implements TestClassIdentifier {

    private static final ClassName TEST_ANNOTATION_NAME = new ClassName("org.junit.jupiter.api.Test");

    private static final ClassName TEST_FACTORY_ANNOTATION_NAME = new ClassName("org.junit.jupiter.api.TestFactory");

    private static final ClassName NESTED_ANNOTATION_NAME = new ClassName("org.junit.jupiter.api.Nested");

    public JUnit5TestClassIdentifier() {
    }

    @Override
    public boolean isATestClass(ClassInfo classInfo) {
        return !classInfo.isInterface()
                && !classInfo.isAbstract()
                && isTopLevelOrNestedTestClass(classInfo)
                && (classInfo.hasAnnotation(TEST_ANNOTATION_NAME) || classInfo.hasAnnotation(TEST_FACTORY_ANNOTATION_NAME));
    }

    @Override
    public boolean isIncluded(ClassInfo classInfo) {
        return true;
    }

    private boolean isTopLevelOrNestedTestClass(ClassInfo classInfo) {
        if (classInfo.getOuterClass().hasNone()) {
            return true;
        }

        return isTopLevelOrNestedTestClass(classInfo.getOuterClass().value()) && classInfo.hasAnnotation(NESTED_ANNOTATION_NAME);
    }

}
