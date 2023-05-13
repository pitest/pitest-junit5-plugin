/*
 * Copyright 2023 Björn Kautler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pitest.junit5.repository

import org.junit.AfterClass
import spock.lang.Specification

class TestSpecWithAfterClass extends Specification {
    @AfterClass
    static afterClass() {
    }

    def test() {
        expect:
            true

        where:
            i << (1..2)
    }
}
