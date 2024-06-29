/*******************************************************************************
 * Copyright (c) 2020, 2024 Obeo.
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
package org.eclipse.sirius.emfjson.tests.internal.integration;

import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Test;

/**
 * This class will be used in order to test the serialization of real EMF-based models.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class StandaloneIntegrationTests extends AbstractEMFJsonTests {

    /**
     * Test the serialization of Ecore.ecore.
     */
    @Test
    public void testEcore() {
        this.options.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, JsonResource.INDENT_2_SPACES);
        this.testSave("ecore/PrettyEcore.ecore"); //$NON-NLS-1$
    }

    @Override
    protected String getRootPath() {
        return "/integration/examples/"; //$NON-NLS-1$
    }

    /**
     * Test the meta-modle basicfamily.
     */
    @Test
    public void testBasicFamilyMetaModel() {
        this.testSave("basicfamily.json"); //$NON-NLS-1$
    }

    /**
     * Test an example of family.
     */
    @Test
    public void testFamilySample() {
        this.testSave("family.json"); //$NON-NLS-1$
    }
}
