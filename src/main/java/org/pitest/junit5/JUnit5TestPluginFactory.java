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

import java.util.Collection;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestPluginFactory;

/**
 *
 * @author Tobias Stadler
 */
public class JUnit5TestPluginFactory implements TestPluginFactory {

    @Override
    public Configuration createTestFrameworkConfiguration(TestGroupConfig config,
        ClassByteArraySource source,
        Collection<String> excludedRunners,
        Collection<String> includedTestMethods) {
        return new JUnit5Configuration(config, includedTestMethods);
    }

    @Override
    public String description() {
        return "JUnit 5 test framework support";
    }

    @Override
    public String name() {
      return "junit5";
    }


}
