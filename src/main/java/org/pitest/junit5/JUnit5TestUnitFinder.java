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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitFinder;

/**
 *
 * @author Tobias Stadler
 */
public class JUnit5TestUnitFinder implements TestUnitFinder {

    private final TestGroupConfig testGroupConfig;

    private final Collection<String> includedTestMethods;

    private final Launcher launcher;

    public JUnit5TestUnitFinder(TestGroupConfig testGroupConfig, Collection<String> includedTestMethods) {
        this.testGroupConfig = testGroupConfig;
        this.includedTestMethods = includedTestMethods;
        this.launcher = LauncherFactory.create();
    }

    @Override
    public List<TestUnit> findTestUnits(Class<?> clazz) {
        if(clazz.getEnclosingClass() != null) {
            return emptyList();
        }

        List<Filter> filters = new ArrayList<>(2);
        try {
            List<String> excludedGroups = testGroupConfig.getExcludedGroups();
            if(excludedGroups != null && !excludedGroups.isEmpty()) {
                filters.add(TagFilter.excludeTags(excludedGroups));
            }

            List<String> includedGroups = testGroupConfig.getIncludedGroups();
            if(includedGroups != null && !includedGroups.isEmpty()) {
                filters.add(TagFilter.includeTags(includedGroups));
            }
        } catch(PreconditionViolationException e) {
            throw new IllegalArgumentException("Error creating tag filter", e);
        }

        TestPlan testPlan = launcher.discover(LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectClass(clazz))
                .filters(filters.toArray(new Filter[filters.size()]))
                .build());

        return testPlan.getRoots()
                .stream()
                .map(testPlan::getDescendants)
                .flatMap(Set::stream)
                .filter(testIdentifier -> {
                    boolean hasMethodSource = testIdentifier.getSource().isPresent() && testIdentifier.getSource().get() instanceof MethodSource;
                    boolean isUnitTest = !testIdentifier.isContainer() || hasMethodSource;

                    return isUnitTest && (includedTestMethods == null || includedTestMethods.isEmpty() || (hasMethodSource &&
                            includedTestMethods.contains(((MethodSource) testIdentifier.getSource().get()).getMethodName())));
                })
                .map(testIdentifier -> new JUnit5TestUnit(clazz, testIdentifier))
                .collect(toList());
    }

}
