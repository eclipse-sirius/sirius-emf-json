/*******************************************************************************
 * Copyright (c) 2020, 2026 Obeo.
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

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class test for ecore subpackages deserialization.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class SubPackageLoadTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/attributes/subpackages/"; //$NON-NLS-1$
    }

    /**
     * Test the specific deserialization for subPackage in ecore.
     */
    @Test
    public void testDeserializationOfSimpleSubPackages() {
        this.testLoad("TestSimpleSubPackages.ecore"); //$NON-NLS-1$
    }

    /**
     * Verifies that 85 nested packages exceed the default limit and round-trip with a higher limit.
     *
     * @throws IOException
     *             if saving or loading the model with the higher limit fails
     */
    @Test
    public void testDeserializationOfDeeplyNestedSubPackages() throws IOException {
        var resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(JsonResourceFactoryImpl.EXTENSION, new JsonResourceFactoryImpl());
        var source = resourceSet.createResource(URI.createURI("memory:/source.json")); //$NON-NLS-1$
        EPackage root = EcoreFactory.eINSTANCE.createEPackage();
        root.setName("p0"); //$NON-NLS-1$
        source.getContents().add(root);

        EPackage parent = root;
        for (int i = 1; i < 85; i++) {
            EPackage child = EcoreFactory.eINSTANCE.createEPackage();
            child.setName("p" + i); //$NON-NLS-1$
            parent.getESubpackages().add(child);
            parent = child;
        }

        var output = new ByteArrayOutputStream();
        source.save(output, Map.of());
        byte[] json = output.toByteArray();

        var defaultTarget = resourceSet.createResource(URI.createURI("memory:/default.json")); //$NON-NLS-1$
        IOException exception = Assert.assertThrows(IOException.class, () -> defaultTarget.load(new ByteArrayInputStream(json), Map.of()));
        Assert.assertTrue(exception.getCause() instanceof JsonSyntaxException);
        Assert.assertTrue(exception.getCause().getCause() instanceof MalformedJsonException);
        Assert.assertTrue(exception.getCause().getCause().getMessage().contains("Nesting limit 255 reached")); //$NON-NLS-1$

        var target = resourceSet.createResource(URI.createURI("memory:/target.json")); //$NON-NLS-1$
        target.load(new ByteArrayInputStream(json), Map.of(JsonResource.OPTION_JSON_NESTING_LIMIT, 512));
        Assert.assertEquals(1, target.getContents().size());
        Assert.assertTrue(EcoreUtil.equals(root, target.getContents().get(0)));
        Assert.assertTrue(target.getErrors().isEmpty());
    }

    /**
     * Verifies that a valid JSON document without model elements loads successfully.
     *
     * @throws IOException
     *             if loading the document fails
     */
    @Test
    public void testDeserializationOfEmptyDocument() throws IOException {
        var resource = new JsonResourceFactoryImpl().createResource(URI.createURI("memory:/empty.json")); //$NON-NLS-1$
        byte[] json = "{\"content\":[]}".getBytes(StandardCharsets.UTF_8); //$NON-NLS-1$

        resource.load(new ByteArrayInputStream(json), Map.of());

        Assert.assertTrue(resource.isLoaded());
        Assert.assertTrue(resource.getContents().isEmpty());
        Assert.assertTrue(resource.getErrors().isEmpty());
    }

    /**
     * Verifies that a non-positive nesting limit is rejected.
     */
    @Test
    public void testDeserializationWithInvalidNestingLimit() {
        var resource = new JsonResourceFactoryImpl().createResource(URI.createURI("memory:/empty.json")); //$NON-NLS-1$
        byte[] json = "{\"content\":[]}".getBytes(StandardCharsets.UTF_8); //$NON-NLS-1$

        IOException exception = Assert.assertThrows(IOException.class,
                () -> resource.load(new ByteArrayInputStream(json), Map.of(JsonResource.OPTION_JSON_NESTING_LIMIT, 0)));
        Assert.assertTrue(exception.getMessage().contains(JsonResource.OPTION_JSON_NESTING_LIMIT));
    }

}
