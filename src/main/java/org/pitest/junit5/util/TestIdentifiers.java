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
package org.pitest.junit5.util;

import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;
import org.pitest.testapi.Description;

/**
 *
 * @author Tobias Stadler
 */
public class TestIdentifiers {

    private TestIdentifiers() {

    }

    public static Description toDescription(TestIdentifier testIdentifier) {
        String className = testIdentifier.getSource().map(source -> {
            if (source instanceof ClassSource) {
                return ((ClassSource) source).getClassName();
            } else if (source instanceof MethodSource) {
                return ((MethodSource) source).getClassName();
            }

            return null;
        }).orElse(null);

        return new Description(testIdentifier.getDisplayName(), className);
    }

}
