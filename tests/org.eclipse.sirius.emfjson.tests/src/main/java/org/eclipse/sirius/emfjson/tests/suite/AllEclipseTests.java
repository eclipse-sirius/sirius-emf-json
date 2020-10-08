/*******************************************************************************
 * Copyright (c) 2020 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.emfjson.tests.suite;

import org.eclipse.sirius.emfjson.tests.internal.integration.EclipseIntegrationTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.textui.TestRunner;

/**
 * This suite launch all the tests defined for the emfJson project.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
@RunWith(Suite.class)
@SuiteClasses({ EclipseIntegrationTests.class })
public final class AllEclipseTests {

    /**
     * The constructor.
     */
    private AllEclipseTests() {
    }

    /**
     * Launches the test with the given arguments.
     *
     * @param args
     *            Arguments of the testCase.
     */
    public static void main(String[] args) {
        TestRunner.run(AllEclipseTests.suite());
    }

    /**
     * Creates the {@link junit.framework.TestSuite TestSuite} for all the test.
     *
     * @return The test suite containing all the tests
     */
    public static Test suite() {
        return new JUnit4TestAdapter(AllEclipseTests.class);
    }

}
