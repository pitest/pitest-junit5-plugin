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
package org.pitest.junit5.foreignclassloader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.pitest.junit5.util.TestIdentifiers;
import org.pitest.util.IsolationUtils;

/**
 *
 * @author tobias
 */
public class ForeignClassLoaderLauncher implements Callable<List<String>> {

    private final Launcher launcher;

    private final LauncherDiscoveryRequest launcherDiscoveryRequest;

    public ForeignClassLoaderLauncher(Launcher launcher, LauncherDiscoveryRequest launcherDiscoveryRequest) {
        this.launcher = launcher;
        this.launcherDiscoveryRequest = launcherDiscoveryRequest;
    }

    @Override
    public List<String> call() throws Exception {
        List<String> events = new ArrayList<>();

        launcher.registerTestExecutionListeners(new TestExecutionListener() {
            @Override
            public void executionSkipped(TestIdentifier testIdentifier, String reason) {
                events.add(IsolationUtils.toXml(new Skipped(TestIdentifiers.toDescription(testIdentifier))));
            }

            @Override
            public void executionStarted(TestIdentifier testIdentifier) {
                events.add(IsolationUtils.toXml(new Started(TestIdentifiers.toDescription(testIdentifier))));
            }

            @Override
            public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
                events.add(IsolationUtils.toXml(new Finished(TestIdentifiers.toDescription(testIdentifier), testExecutionResult.getThrowable().orElse(null))));
            }

        });
        launcher.execute(launcherDiscoveryRequest);

        return events;
    }

}
