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

import com.google.common.collect.Lists;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.sirius.emfjson.resource.exception.DanglingHREFException;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.eclipse.sirius.emfjson.utils.IGsonConstants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests class for EReferences.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class NonContainmentReferencesSaveTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/references/noncontainment/"; //$NON-NLS-1$
    }

    /**
     * Tests the serialization of Single valued EReference.
     */
    @Test
    public void testSaveSingleValuedEReference() {
        this.testSave("NodeSingleValueEReference.xmi"); //$NON-NLS-1$
    }

    /**
     * Tests the serialization of multi-valued EReference.
     */
    @Test
    public void testSaveMultiValuedEReference() {
        this.testSave("NodeMultiValuedEReference.xmi"); //$NON-NLS-1$
    }

    /**
     * Tests the serialization of a sub-library containing a book referencing its owner sub-library. For this test we
     * use an modified extlibrary.ecore meta-model.
     */
    @Test
    public void testSaveLibraryWithBookReferencingItsOwnBranchLibrary() {
        this.testSave("TestLibraryWithBookReferencingItsOwnBranchLibrary.xmi"); //$NON-NLS-1$
    }

    /**
     * Tests the serialization of a library containing a book referencing its owner library. For this test we use an
     * modified extlibrary.ecore meta-model.
     */
    @Test
    public void testSaveLibraryWithBookReferencingItsOwnLibrary() {
        this.testSave("TestLibraryWithBookReferencingItsOwnLibrary.xmi"); //$NON-NLS-1$
    }

    /**
     * Tests the serialization of a library containing a book referencing many libraries. For this test we use an
     * modified extlibrary.ecore meta-model.
     */
    @Test
    public void testSaveLibraryWithBookReferencingLibraries() {
        this.testSave("TestLibraryWithBookReferencingLibraries.xmi"); //$NON-NLS-1$
    }

    /**
     * Tests the serialization of a book referencing an library branch in an external resource. For this test we use an
     * modified extlibrary.ecore meta-model.
     */
    @Test
    public void testSaveLibraryWithExternalBookReferencingBranchLibrary() {
        this.testSave("TestLibraryWithExternalBookReferencingBranchLibrary_Book.xmi"); //$NON-NLS-1$
    }

    /**
     * Tests the serialization a book referencing many libraries in an external resource. For this test we use an
     * modified extlibrary.ecore meta-model.
     */
    @Test
    public void testSaveLibraryWithExternalBookReferencingLibraries() {
        this.testSave("TestLibraryWithExternalBookReferencingLibraries_Book.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialisation of a book referencing a root library in an external resource. For this test we use an
     * modified extlibrary.ecore meta-model.
     */
    @Test
    public void testSaveLibraryWithExternalBookReferencingLibrary() {
        this.testSave("TestLibraryWithExternalBookReferencingLibrary_Book.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of a class referencing external eSuperTypes.
     */
    @Test
    public void testSaveExternalMultiNonContainmentReferences() {
        this.testSave("TestExternalMultiNonContainmentReferences.ecore"); //$NON-NLS-1$
    }

    /**
     * Tests the throwing of Dangling exception when expected it.
     *
     * @throws DanglingHREFException
     *             the exception expected
     */
    @Test(expected = DanglingHREFException.class)
    public void testSaveSerializationOfThrowingDanglingReferenceException() throws DanglingHREFException {
        Resource resource = this.initializeDanglingReference();

        this.options.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);
        this.options.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, JsonResource.INDENT_2_SPACES);
        this.options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
        this.options.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_THROW);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            resource.save(outputStream, this.options);

        } catch (IOException e) {
            Throwable throwable = e.getCause();
            if (throwable instanceof DanglingHREFException) {
                throw new DanglingHREFException(e.getMessage());
            }
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Tests the nonThrowing and noSaving exception on serialization of dangling reference.
     */
    @Test
    public void testSaveSerializationOfDiscardDanglingReference() {

        Resource resource = this.initializeDanglingReference();

        this.options.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);
        this.options.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, JsonResource.INDENT_2_SPACES);
        this.options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
        this.options.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_DISCARD);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            resource.save(outputStream, this.options);
            Assert.assertTrue(resource.getErrors().isEmpty());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Tests the nonThrowing but Saving exception on serialization of dangling references.
     */
    @Test
    public void testSevaSerializationOfRecordDanglingReferenceException() {

        Resource resource = this.initializeDanglingReference();

        this.options.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);
        this.options.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, JsonResource.INDENT_2_SPACES);
        this.options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
        this.options.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_RECORD);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean found = false;
        try {
            resource.save(outputStream, this.options);
        } catch (IOException e) {

            EList<Diagnostic> errors = resource.getErrors();
            Iterator<Diagnostic> it = errors.iterator();
            while (!found && it.hasNext()) {
                Diagnostic type = it.next();
                if (type instanceof DanglingHREFException) {
                    found = true;
                }
            }
        }
        try {
            Assert.assertTrue(found);
        } catch (AssertionError e1) {

            Assert.fail(e1.getMessage());
        }
    }

    /**
     * Test the serialization of a non containment EReferences in an Ecore resource.
     */
    @Test
    public void testSaveInternalMultiNonContainmentReferenceOnEcoreResource() {
        this.testSave("TestInternalMultiNonContainmentReference.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of a non containment EReference in an Ecore resource.
     */
    @Test
    public void testSaveInternalSingleNonContainmentReferenceOnEcoreresource() {
        this.testSave("TestInternalSingleNonContainmentReference.ecore"); //$NON-NLS-1$
    }

    /**
     * Creates a EObject that is not contained in a resource and add it as a value of a NonContainment Reference. Then
     * adds the EObject in a resource a return the resource. If this resource is serialized, that will raise a Dangling
     * Exception.
     *
     * @return Resource
     */
    private Resource initializeDanglingReference() {

        Resource modelResource = this.getModelResource("NodeSingleDanglingEReference.xmi", null); //$NON-NLS-1$

        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(IGsonConstants.JSON, new JsonResourceFactoryImpl());

        String jsonResourceURIString = modelResource.getURI().toString();
        jsonResourceURIString = jsonResourceURIString.substring(0, jsonResourceURIString.lastIndexOf('.') + 1) + IGsonConstants.JSON;
        Resource resource = resourceSet.createResource(URI.createURI(jsonResourceURIString));
        resource.getContents().addAll(modelResource.getContents());

        EObject resourceObject = resource.getContents().get(0);

        EClass eClass = resourceObject.eClass();
        EObject eObject = EcoreUtil.create(eClass);

        EStructuralFeature referenceValue = eClass.getEStructuralFeature("singleValuedReference"); //$NON-NLS-1$

        EClassifier eType = referenceValue.getEType(); // The classifier is an EClass with "node" as name
        EObject node = EcoreUtil.create((EClass) eType);
        eObject.eSet(referenceValue, node); // referenceValue ask a Node EObject instance

        resourceObject.eSet(referenceValue, eObject); // Here eObject is set and it is not contain in a
        // Resource, so the serialization will raise a
        // DanglingException

        return resource;

    }

    /**
     * Returns the resource with the given name.
     *
     * @param resourceName
     *            the resource name
     * @param modelOptions
     *            options given for loading resource
     * @return the resource
     */
    private Resource getModelResource(String resourceName, Map<?, ?> modelOptions) {
        Resource metaModelResource = this.loadResource("/nodes.ecore", Lists //$NON-NLS-1$
                .newArrayList((EPackage) EcorePackage.eINSTANCE), modelOptions);
        if (metaModelResource != null && metaModelResource.getContents().size() > 0 && metaModelResource.getContents().get(0) instanceof EPackage) {
            EPackage ePackage = (EPackage) metaModelResource.getContents().get(0);
            return this.loadResource(this.getRootPath() + resourceName, Lists.newArrayList(ePackage), modelOptions);
        }
        return null;
    }

    /**
     * Loads the resource with the given path in a new resource set and return the resource.
     *
     * @param resourcePath
     *            The path of the resource to load
     * @param ePackages
     *            The packages to register in the resource set
     * @param modelOptions
     *            options given for loading resource
     * @return The resource loaded
     */
    private Resource loadResource(String resourcePath, ArrayList<EPackage> ePackages, Map<?, ?> modelOptions) {
        Map<?, ?> loadOptions = modelOptions;
        if (loadOptions == null) {
            loadOptions = Collections.emptyMap();
        }

        final URL fileURL = ExtendedMetaDataTests.class.getResource(resourcePath);

        Resource resource = null;
        InputStream inputStream = null;
        try {
            inputStream = fileURL.openStream();
            final URI uri = URI.createURI(fileURL.toString());

            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
            for (EPackage ePackage : ePackages) {
                resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
            }

            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl()); //$NON-NLS-1$
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl()); //$NON-NLS-1$
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(JsonResourceFactoryImpl.EXTENSION, new JsonResourceFactoryImpl());

            // resourceFactory cannot be null
            resource = resourceSet.createResource(uri);
            resource.load(inputStream, loadOptions);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }
        return resource;
    }
}
