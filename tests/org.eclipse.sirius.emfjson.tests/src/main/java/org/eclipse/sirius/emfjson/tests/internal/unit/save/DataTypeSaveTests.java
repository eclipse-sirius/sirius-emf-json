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
 * Tests class for EDataType serialization.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class DataTypeSaveTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/attributes/datatypes/"; //$NON-NLS-1$
    }

    /**
     * Test the serialization for Simple DataType.
     */
    @Test
    public void testSimpleSerialiationDataType() {
        this.testSave("NodeSingleCustomDataType.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization for Multiple DataType.
     */
    @Test
    public void testMultipleSerialiationDataType() {
        this.testSave("NodeMultipleCustomDataType.xmi"); //$NON-NLS-1$
    }

}
