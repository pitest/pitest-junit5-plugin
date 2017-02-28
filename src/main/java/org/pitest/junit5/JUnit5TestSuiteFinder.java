/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pitest.junit5;

import java.util.Collections;
import java.util.List;
import org.pitest.testapi.TestSuiteFinder;

/**
 *
 * @author Tobias Stadler
 */
public class JUnit5TestSuiteFinder implements TestSuiteFinder {

    @Override
    public List<Class<?>> apply(Class<?> a) {
        return Collections.emptyList();
    }

}
