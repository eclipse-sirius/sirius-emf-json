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
package org.eclipse.sirius.emfjson.tests.internal.integration;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.emfjson.ide.utils.IIdeConstants;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class will be used in order to test the serialization of real EMF-based models.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class EclipseIntegrationTests extends AbstractEMFJsonTests {

    /**
     * Test the serialization of Ecore.ecore.
     */
    @Test
    @Ignore
    public void testEcore() {
        this.testSave("ecore/Ecore.ecore"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/integration/examples/"; //$NON-NLS-1$
    }

    /**
     * Test integration of @link{org.eclipse.sirius.emfjson.feature.ide EMFjson Ide} into Eclipse.
     */
    @Test
    public void testIntegrationOfEMFJsonIntoEclipse() {
        ResourceSet resourceSet = new ResourceSetImpl();
        URI uri = URI.createPlatformPluginURI("org.eclipse.sirius.emfjson/test.toto", true); //$NON-NLS-1$
        Resource resource = resourceSet.createResource(uri, IIdeConstants.EMF_JSON_IDE_ID_CONTENT_TYPE);
        Assert.assertTrue(resource instanceof JsonResource);
    }
}
