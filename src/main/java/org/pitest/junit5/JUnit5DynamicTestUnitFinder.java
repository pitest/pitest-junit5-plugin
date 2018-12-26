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

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * @author Tobias Stadler
 */
public class JUnit5DynamicTestUnitFinder implements TestUnitFinder {

    private final Launcher launcher;

    public JUnit5DynamicTestUnitFinder() {
        launcher = LauncherFactory.create();
    }

    @Override
    public List<TestUnit> findTestUnits(Class<?> clazz) {
        if (clazz.getEnclosingClass() != null)
            return emptyList();

        DynamicTestListener listener = new DynamicTestListener();
        launcher.execute(LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectClass(clazz))
                .build(), listener);

        return listener.getIdentifiers()
                .stream()
                .map(testIdentifier -> new JUnit5TestUnit(clazz, testIdentifier))
                .collect(Collectors.toList());
    }

    private class DynamicTestListener extends AbstractTestExecutionListener {
        private final List<TestIdentifier> identifiers = new ArrayList<>();

        List<TestIdentifier> getIdentifiers() {
            return Collections.unmodifiableList(identifiers);
        }

        @Override
        public void dynamicTestRegistered(TestIdentifier testIdentifier) {
            final TestDescriptor.Type type = testIdentifier.getType();
            if (type == TestDescriptor.Type.TEST || type == TestDescriptor.Type.CONTAINER_AND_TEST) {
                identifiers.add(testIdentifier);
            }
        }
    }
}
