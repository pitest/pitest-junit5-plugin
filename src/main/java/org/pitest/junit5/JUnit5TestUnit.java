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

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.pitest.testapi.AbstractTestUnit;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;

/**
 *
 * @author Tobias Stadler
 */
public class JUnit5TestUnit extends AbstractTestUnit {

    private final Class<?> testClass;

    private final TestIdentifier testIdentifier;

    public JUnit5TestUnit(Class<?> testClass, TestIdentifier testIdentifier) {
        super(new Description(testIdentifier.getDisplayName(), testClass));
        this.testClass = testClass;
        this.testIdentifier = testIdentifier;
    }

    @Override
    public void execute(ResultCollector resultCollector) {
        Launcher launcher = LauncherFactory.create();
        LauncherDiscoveryRequest launcherDiscoveryRequest = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectUniqueId(testIdentifier.getUniqueId()))
                .build();

            launcher.registerTestExecutionListeners(new TestExecutionListener() {
                @Override
                public void executionSkipped(TestIdentifier testIdentifier, String reason) {
                	if (testIdentifier.isTest()) {
                            resultCollector.notifySkipped(new Description(testIdentifier.getDisplayName(), testClass));
                    }
                }

                @Override
                public void executionStarted(TestIdentifier testIdentifier) {
                	if (testIdentifier.isTest()) {
                            resultCollector.notifyStart(new Description(testIdentifier.getDisplayName(), testClass));
                    }
                }

                @Override
                public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
                	if (testIdentifier.isTest()) {
                            Optional<Throwable> throwable = testExecutionResult.getThrowable();

                            if (TestExecutionResult.Status.ABORTED == testExecutionResult.getStatus()) {
                                // abort treated as success
                                // see: https://junit.org/junit5/docs/5.0.0/api/org/junit/jupiter/api/Assumptions.html
                                resultCollector.notifyEnd(new Description(testIdentifier.getDisplayName(), testClass));
                            } else if (throwable.isPresent()) {
                                resultCollector.notifyEnd(new Description(testIdentifier.getDisplayName(), testClass), throwable.get());
                            } else {
                                resultCollector.notifyEnd(new Description(testIdentifier.getDisplayName(), testClass));
                            }
                    }
                }

            });
            launcher.execute(launcherDiscoveryRequest);
    }

}
