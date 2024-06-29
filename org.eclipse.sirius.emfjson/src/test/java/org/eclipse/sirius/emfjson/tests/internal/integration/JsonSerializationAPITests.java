/*******************************************************************************
 * Copyright (c) 2020, 2025 Obeo.
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.gson.JsonElement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
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

    @SuppressWarnings("javadoc")
    private static final String CLASS21NAME = "CLASS21"; //$NON-NLS-1$

    @SuppressWarnings("javadoc")
    private static final String CLASS22NAME = "CLASS22"; //$NON-NLS-1$

    @SuppressWarnings("javadoc")
    private static final String CLASS23NAME = "CLASS23"; //$NON-NLS-1$

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

    /**
     * Check that the deserialization of a mono and multivalued reference are correctly done so that the proxy can be
     * properly resolved.
     */
    @Test
    public void serializeInterResourceReferenceTest() {
        this.options.put(JsonResource.OPTION_DISPLAY_DYNAMIC_INSTANCES, Boolean.TRUE);

        JsonResourceImpl resource1 = new JsonResourceImpl(URI.createURI("sirius:///resource1"), this.options); //$NON-NLS-1$
        JsonResourceImpl resource2 = new JsonResourceImpl(URI.createURI("sirius:///resource2"), this.options); //$NON-NLS-1$
        this.initializeResource(resource1, resource2);

        // get the json of the resource1
        String jsonRes1 = ""; //$NON-NLS-1$
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            resource1.save(outputStream, this.options);

            byte[] bytes = outputStream.toByteArray();
            jsonRes1 = new String(bytes);
            assertFalse("The json should not be empty", jsonRes1.isEmpty()); //$NON-NLS-1$

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }

        // get the json of the resource2
        String jsonRes2 = ""; //$NON-NLS-1$
        outputStream = new ByteArrayOutputStream();
        try {
            resource2.save(outputStream, this.options);

            byte[] bytes = outputStream.toByteArray();
            jsonRes2 = new String(bytes);
            assertFalse("The json should not be empty", jsonRes2.isEmpty()); //$NON-NLS-1$

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }

        // create the resource from the json string
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);

        Resource loadedResource1 = new JsonResourceImpl(URI.createURI("sirius:///resource1"), this.options); //$NON-NLS-1$
        resourceSet.getResources().add(loadedResource1);
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(jsonRes1.getBytes());
        try {
            loadedResource1.load(inputStream1, null);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            try {
                inputStream1.close();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }

        Resource loadedResource2 = new JsonResourceImpl(URI.createURI("sirius:///resource2"), this.options); //$NON-NLS-1$
        resourceSet.getResources().add(loadedResource2);
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(jsonRes2.getBytes());
        try {
            loadedResource2.load(inputStream2, null);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            try {
                inputStream2.close();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }

        // Check that the resource has been correctly loaded
        EClass eReferenceType = ((EClass) ((EPackage) loadedResource1.getContents().get(0)).getEClassifiers().get(0)).getEAllReferences().get(0).getEReferenceType();
        EPackage ePackageRes2 = (EPackage) loadedResource2.getContents().get(0);
        assertEquals(eReferenceType, ePackageRes2.getEClassifier(CLASS23NAME));

        EList<EClass> eAllSuperTypes = ((EClass) ((EPackage) loadedResource1.getContents().get(0)).getEClassifiers().get(0)).getEAllSuperTypes();
        assertArrayEquals(eAllSuperTypes.toArray(), Arrays.asList(ePackageRes2.getEClassifier(CLASS21NAME), ePackageRes2.getEClassifier(CLASS22NAME)).toArray());
    }

    /**
     * Initialize the resources. resource1 will contain a mono valued reference to an EClass in resource2 and a multi
     * valued reference to two instances in resource2.
     *
     * @param resource1
     * @param resource2
     */
    private void initializeResource(JsonResourceImpl resource1, JsonResourceImpl resource2) {
        // Create resource1
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        EClass eClass1 = EcoreFactory.eINSTANCE.createEClass();
        ePackage.getEClassifiers().add(eClass1);
        EReference eReference = EcoreFactory.eINSTANCE.createEReference();
        eClass1.getEStructuralFeatures().add(eReference);

        resource1.getContents().add(ePackage);
        resourceSet.getResources().add(resource1);

        // Create resource2
        EPackage ePackage2 = EcoreFactory.eINSTANCE.createEPackage();
        EClass eClass21 = EcoreFactory.eINSTANCE.createEClass();
        eClass21.setName(CLASS21NAME);
        EClass eClass22 = EcoreFactory.eINSTANCE.createEClass();
        eClass22.setName(CLASS22NAME);
        EClass eClass23 = EcoreFactory.eINSTANCE.createEClass();
        eClass23.setName(CLASS23NAME);
        ePackage2.getEClassifiers().add(eClass21);
        ePackage2.getEClassifiers().add(eClass22);
        ePackage2.getEClassifiers().add(eClass23);

        resource2.getContents().add(ePackage2);
        resourceSet.getResources().add(resource2);

        // create mono and multi valued references
        eClass1.getESuperTypes().add(eClass21);
        eClass1.getESuperTypes().add(eClass22);
        eReference.setEType(eClass23);
    }
}
