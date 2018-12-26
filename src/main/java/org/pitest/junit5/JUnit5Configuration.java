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

import java.util.Optional;

import org.pitest.help.PitHelpError;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestSuiteFinder;
import org.pitest.testapi.TestUnitFinder;

/**
 *
 * @author Tobias Stadler
 */
public class JUnit5Configuration implements Configuration {

    private final boolean useDynamicTests;

    public JUnit5Configuration(boolean useDynamicTests) {
        this.useDynamicTests = useDynamicTests;
    }

    @Override
    public TestUnitFinder testUnitFinder() {
        if (useDynamicTests)
            return new JUnit5DynamicTestUnitFinder();
        else
            return new JUnit5TestUnitFinder();
    }

    @Override
    public TestSuiteFinder testSuiteFinder() {
        return new JUnit5TestSuiteFinder();
    }

    @Override
    public Optional<PitHelpError> verifyEnvironment() {
        return Optional.empty();
    }

}
