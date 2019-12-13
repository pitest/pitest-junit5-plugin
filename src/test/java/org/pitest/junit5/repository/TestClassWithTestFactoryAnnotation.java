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
package org.pitest.junit5.repository;

import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 *
 * @author Tobias Stadler
 */
public class TestClassWithTestFactoryAnnotation {

    @TestFactory
    public Collection<DynamicTest> testFactory() {
		return Collections.singleton(DynamicTest.dynamicTest("dynamic test", () -> {}));
    }

}
