/*******************************************************************************
 * Copyright (c) 2022 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.emfjson.tests.internal.options;

import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Test;

/**
 * Class used to test the schema location support.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class SchemaLocationTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/options/"; //$NON-NLS-1$
    }

    @Test
    public void testSaveSchemaLocation() {
        this.testSave("SchemaLocation.xmi"); //$NON-NLS-1$
    }

    @Test
    public void testLoadSchemaLocation() {
        this.testLoad("SchemaLocation.xmi"); //$NON-NLS-1$
    }

}
