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

import com.google.gson.JsonElement;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class test for JsonResource serialization API.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class JsonSerializationAPITests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/integration/examples/ecore/"; //$NON-NLS-1$
    }

    /**
     * Test the jsonElement returned by the resource serialization. The read file must be uglified.
     */
    @Test
    public void serializeResourceToJsonElementTest() {
        String expectedResource = this.readResource("Ecore.json"); //$NON-NLS-1$

        Resource modelResource = this.getModelResource("Ecore.json", true); //$NON-NLS-1$
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(JsonResourceFactoryImpl.EXTENSION, new JsonResourceFactoryImpl());

        String jsonResourceURI = modelResource.getURI().toString();
        jsonResourceURI = jsonResourceURI.substring(0, jsonResourceURI.lastIndexOf('.') + 1) + JsonResourceFactoryImpl.EXTENSION;
        Resource resource = resourceSet.createResource(URI.createURI(jsonResourceURI));
        resource.getContents().addAll(modelResource.getContents());

        JsonElement jsonTree = JsonResourceImpl.toJsonTree(resource, this.options);
        String jsonTreeString = jsonTree.toString();

        Assert.assertEquals(expectedResource, jsonTreeString);
    }
}
