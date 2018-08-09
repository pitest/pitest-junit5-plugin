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

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitFinder;

/**
 *
 * @author Tobias Stadler
 */
public class JUnit5TestUnitFinder implements TestUnitFinder {

    private final Launcher launcher;

    public JUnit5TestUnitFinder() {
        launcher = LauncherFactory.create();
    }

    @Override
    public List<TestUnit> findTestUnits(Class<?> clazz) {
        if(clazz.getEnclosingClass() != null) {
            return emptyList();
        }

        TestPlan testPlan = launcher.discover(LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectClass(clazz))
                .build());

        return testPlan.getRoots()
                .stream()
                .map(testPlan::getDescendants)
                .flatMap(Set::stream)
                .filter(testIdentifier -> testIdentifier.getSource().isPresent())
                .filter(testIdentifier -> testIdentifier.getSource().get() instanceof MethodSource)
                .map(testIdentifier -> new JUnit5TestUnit(clazz, testIdentifier))
                .collect(toList());
    }

}
