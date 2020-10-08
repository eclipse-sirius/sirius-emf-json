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

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.ExtendedMetaDataTests;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests class for options on serialization.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class DeserializeOptionsTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/attributes/serializeoptions/"; //$NON-NLS-1$
    }

    /**
     * Test the deserialization of a sub tree of EObject contained in a resource.
     */
    @Test
    public void testDeserializationOfSubTree() {
        String resourceName = "TestSubTreeOption.ecore"; //$NON-NLS-1$
        // load resourceName
        Resource modelResource = this.getModelResource(resourceName, this.options);

        // create resourceSet and adding jsonExtenstion to it.
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(JsonResourceFactoryImpl.EXTENSION, new JsonResourceFactoryImpl());

        ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(modelResource.getResourceSet().getPackageRegistry());
        resourceSet.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);

        // create a new Resource and adding the previous resource contents to it
        String jsonResourceURI = modelResource.getURI().toString();
        jsonResourceURI = jsonResourceURI.substring(0, jsonResourceURI.lastIndexOf('.') + 1) + JsonResourceFactoryImpl.EXTENSION;
        Resource resource = resourceSet.createResource(URI.createURI(jsonResourceURI));

        // get a sub tree of modelResource
        EList<EObject> subTree = new BasicEList<EObject>();
        EList<EObject> contents = modelResource.getContents();
        EPackage ePackage = (EPackage) contents.get(0);

        EList<EClassifier> eClassifiers = ePackage.getEClassifiers();
        EClassifier otherEClass = eClassifiers.get(1);
        subTree.add(otherEClass);
        // ePackage.getEClassifiers().remove(otherEClass);

        resource.getContents().addAll(subTree);

        String expectedResult = ""; //$NON-NLS-1$
        String jsonResourceName = resourceName.substring(0, resourceName.lastIndexOf('.') + 1) + JsonResourceFactoryImpl.EXTENSION;
        URL jsonResourceURL = this.getClass().getResource(this.getRootPath() + jsonResourceName);
        InputStream inputStream = null;
        InputStreamReader reader = null;
        try {
            inputStream = jsonResourceURL.openStream();
            reader = new InputStreamReader(inputStream, Charset.forName("utf-8")); //$NON-NLS-1$
            expectedResult = CharStreams.toString(reader);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }

        // create outpustream to save the resource into json and then compare with the expected result
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            this.options.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);
            this.options.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, JsonResource.INDENT_2_SPACES);
            this.options.put(JsonResource.OPTION_ROOT_OBJECTS, subTree);

            resource.save(outputStream, this.options);
            String json = new String(outputStream.toByteArray(), Charset.forName("utf-8")); //$NON-NLS-1$
            Assert.assertEquals(expectedResult.replaceAll(CRLF, LF), json.replaceAll(CRLF, LF));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    /**
     * Return the resource with the given name.
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

    /**
     * Test the deserialization of a model with URIHandler.
     */
    @Test
    public void testDeserializationWithURIHandler() {
        // CHECKSTYLE:OFF
        JsonResource.URIHandler handler = new JsonResource.URIHandler() {

            @Override
            public void setBaseURI(URI uri) {
                //
            }

            @Override
            public URI resolve(URI uriToResolve) {
                URI anURI = URI.createURI("http://www.eclipse.org/emfjson/eclipse/emf/ecore/"); //$NON-NLS-1$
                return uriToResolve.resolve(anURI);
            }

            @Override
            public URI deresolve(URI uriToDeresolve) {
                return null;
            }
        };
        // CHECKSTYLE:ON
        this.options.put(JsonResource.OPTION_URI_HANDLER, handler);

        this.testLoad("TestURIHandlerOption.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization with a Resource Entity.
     */
    @Test
    public void testDeserializationResourceEntityHandler() {
        // CHECKSTYLE:OFF
        JsonResource.ResourceEntityHandler handler = new JsonResource.ResourceEntityHandler() {

            private Map<String, String> nameToValueMap = new HashMap<String, String>();

            @Override
            public void reset() {
                //
            }

            @Override
            public void handleEntity(String entityName, String entityValue) {
                this.nameToValueMap.put(entityName, entityValue);
            }

            @Override
            public Map<String, String> getNameToValueMap() {
                return this.nameToValueMap;
            }

            @Override
            public String getEntityName(String entityValue) {
                return null;
            }
        };
        // CHECKSTYLE:ON
        handler.handleEntity("EcoreEntity", "http://www.eclipse.org/emf/2002/Ecore"); //$NON-NLS-1$//$NON-NLS-2$
        this.options.put(JsonResource.OPTION_RESOURCE_ENTITY_HANDLER, handler);
        this.testLoad("TestResourceEntityHandlerOption.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization using a resource handler on pre-loading. This test defined an URIHandler in option, the
     * same as {@link DeserializeOptionsTests#testDeserializationWithURIHandler()}, and also defined a ResourceHandler
     * that clear all option. So deserialization will not taking account of the URIHandler.
     */
    @Test
    public void testDeserializationResourceHandlerOnPreLoading() {
        // CHECKSTYLE:OFF
        JsonResource.URIHandler uriHandler = new JsonResource.URIHandler() {

            @Override
            public void setBaseURI(URI uri) {
                //
            }

            @Override
            public URI resolve(URI uriToResolve) {
                URI anURI = URI.createURI("http://www.eclipse.org/emfjson/eclipse/emf/ecore/"); //$NON-NLS-1$
                return uriToResolve.resolve(anURI);
            }

            @Override
            public URI deresolve(URI uriToDeresolve) {
                return null;
            }
        };
        // CHECKSTYLE:ON
        this.options.put(JsonResource.OPTION_URI_HANDLER, uriHandler);

        // CHECKSTYLE:OFF
        JsonResource.ResourceHandler resourceHandler = new JsonResource.ResourceHandler() {

            @Override
            public void preSave(JsonResource resource, OutputStream outputStream, Map<?, ?> saveOptions) {
                // do nothing
            }

            @Override
            public void preLoad(JsonResource resource, InputStream inputStream, Map<?, ?> loadOptions) {
                loadOptions.clear();
            }

            @Override
            public void postSave(JsonResource resource, OutputStream outputStream, Map<?, ?> saveOtions) {
                // do nothing
            }

            @Override
            public void postLoad(JsonResource resource, InputStream inputStream, Map<?, ?> loadOtions) {
                // do nothing
            }
        };
        // CHECKSTYLE:ON

        this.options.put(JsonResource.OPTION_RESOURCE_HANDLER, resourceHandler);
        this.testLoad("TestResourceHandlerPreLoadOption.ecore"); //$NON-NLS-1$

    }

    /**
     * Test the deserialization using a resource handler on post loading. This test add a subpackage to the root package
     * of the resource that had been loaded.
     */
    @Test
    public void testDeserializationResourceHandlerOnPostLoading() {
        // CHECKSTYLE:OFF
        JsonResource.ResourceHandler resourceHandler = new JsonResource.ResourceHandler() {

            @Override
            public void preSave(JsonResource resource, OutputStream outputStream, Map<?, ?> saveOptions) {
                // do nothing
            }

            @Override
            public void preLoad(JsonResource resource, InputStream inputStream, Map<?, ?> loadOptions) {
                // do nothing
            }

            @Override
            public void postSave(JsonResource resource, OutputStream outputStream, Map<?, ?> saveOtions) {
                // do nothing
            }

            @Override
            public void postLoad(JsonResource resource, InputStream inputStream, Map<?, ?> loadOtions) {

                EPackage ePackage = (EPackage) resource.getContents().get(0);
                EPackage subPackage = (EPackage) EcoreUtil.create(EcorePackage.Literals.EPACKAGE);
                subPackage.setName("subpackage"); //$NON-NLS-1$
                subPackage.setNsPrefix("subpackage"); //$NON-NLS-1$
                subPackage.setNsURI(ePackage.getNsURI() + "/subpackage"); //$NON-NLS-1$
                ePackage.getESubpackages().add(subPackage);

            }
        };
        // CHECKSTYLE:ON

        this.options.put(JsonResource.OPTION_RESOURCE_HANDLER, resourceHandler);
        this.testLoad("TestResourceHandlerPostLoadOption.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization using a missing package handler. In this test
     * {@link org.eclipse.sirius.emfjson.resource.JsonResource.MissingPackageHandler#getEPackage(String)} create a
     * resource corresponding to the "nodes.ecore" file, load this resource and return the first element which is an
     * EPackage.
     */
    @Test
    public void testDeserializeWithMissingPackageHandler() {
        // CHECKSTYLE:OFF
        JsonResource.MissingPackageHandler missingPackageHandler = new JsonResource.MissingPackageHandler() {

            @Override
            public EPackage getEPackage(String nsURI) {
                if ("nodes.ecore".equals(nsURI)) { //$NON-NLS-1$
                    URI uri = URI.createURI("/nodes.ecore"); //$NON-NLS-1$

                    URL resourceURL = AbstractEMFJsonTests.class.getResource(uri.toString());
                    URI resourceURI = URI.createURI(resourceURL.toString());

                    ResourceSet resourceSet = new ResourceSetImpl();
                    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl()); //$NON-NLS-1$
                    Resource createdResource = resourceSet.createResource(resourceURI);

                    try {
                        createdResource.load(DeserializeOptionsTests.this.options);
                        return (EPackage) createdResource.getContents().get(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        // CHECKSTYLE:ON
        this.options.put(JsonResource.OPTION_MISSING_PACKAGE_HANDLER, missingPackageHandler);
        this.testLoad("TestMissingPackageHandlerOption.ecore"); //$NON-NLS-1$
    }
}
