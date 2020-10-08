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

package org.eclipse.sirius.emfjson.tests.internal.unit.load;

import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests class for EReferences.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class ContainmentReferencesLoadTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/references/containment/"; //$NON-NLS-1$
    }

    /**
     * Test the deserialization of single valued Containment EReference.
     */
    @Test
    public void testLoadSingleContainment() {
        this.testLoad("NodeSingleValueContainment.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of multi-valued Containment ERference.
     */
    @Test
    public void testLoadMultiContainment() {
        this.testLoad("NodeMultiValuedContainment.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of single valued fragmented EReference.
     */
    @Test
    @Ignore
    public void testLoadSingleFragmentedReference() {
        this.testLoad("FirstnodeSingleValueFragmentedReference.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the no deserialization of opposite containment EReference.
     */
    @Test
    public void testLoadNoSerializationOfOppositeContainmentReference() {
        this.testLoad("NodeOppositeContainingReference.xmi"); //$NON-NLS-1$
    }

}
