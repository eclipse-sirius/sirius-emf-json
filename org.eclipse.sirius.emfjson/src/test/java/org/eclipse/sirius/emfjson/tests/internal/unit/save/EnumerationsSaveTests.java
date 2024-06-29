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

package org.eclipse.sirius.emfjson.tests.internal.unit.save;

import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Test;

/**
 * This class will be used in order to test the serialization of EEnumerations.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class EnumerationsSaveTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/attributes/enumerations/"; //$NON-NLS-1$
    }

    /**
     * Test the serialization of EEnumeration.
     */
    @Test
    public void testSaveEnumeration() {
        this.testSave("NodeSingleValueEnumeration.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of Multiple EEnumeration.
     */
    @Test
    public void testSaveMultipleEnumeration() {
        this.testSave("NodeMultiValuedEnumeration.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of Literals.
     */
    @Test
    public void testSaveLiterals() {
        this.testSave("TestSaveLiterals.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of a Literal with its value setted.
     */
    @Test
    public void testSaveLiteralWithValueSetted() {
        this.testSave("TestSaveLiteralWithValueSetted.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of a Literal with its literal value different from its literal name.
     */
    @Test
    public void testSaveLiteralWithLiteralValueSetted() {
        this.testSave("TestSaveLiteralWithLiteralValueSetted.ecore"); //$NON-NLS-1$
    }

}
