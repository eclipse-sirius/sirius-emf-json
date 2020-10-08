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
import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResource.EStructuralFeaturesFilter;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests class for options on serialization.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class SerializeOptionsTests extends AbstractEMFJsonTests {

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
     * Test the serialization on a model containing an enumeration with its default value set.
     */
    @Test
    public void testEnumerationModelWithDefaultValue() {
        this.testSave("EnumerationModelWithDefaultValue.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization on a model containing an enumeration with an other value than the default value set.
     */
    @Test
    public void testEnumerationModelWithoutDefaultValue() {
        this.testSave("EnumerationModelWithoutDefaultValue.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization on a model containing an enumeration with its default value set, with the option which
     * force the serialization of default value.
     */
    @Test
    public void testEnumerationModelWithDefaultValueForcedToSerialize() {
        this.options.put(JsonResource.OPTION_SAVE_UNSETTED_FEATURES, Boolean.valueOf(true));
        this.testSave("EnumerationModelWithDefaultValueForcedToSerialize.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization with custom filter that serialize a simple model containing only default values.
     */
    @Test
    public void testSerializationWithFilterOnSimpleModelWithOnlyDefaultValue() {
        JsonResource.EStructuralFeaturesFilter featuresFilter = new EStructuralFeaturesFilter() {

            @Override
            public boolean shouldSave(EObject eObject, EStructuralFeature eStructuralFeature) {
                return true;
            }

            @Override
            public boolean shouldLoad(EObject eObject, EStructuralFeature eStructuralFeature) {
                return false;
            }
        };

        this.options.put(JsonResource.OPTION_ESTRUCTURAL_FEATURES_FILTER, featuresFilter);
        this.testSave("SimpleModelWithOnlyDefaultValueForFilterTest.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization with a model that contain a derived attribute but force serialization of derived features
     * by setting the specific option {@link JsonResource#OPTION_SAVE_DERIVED_FEATURES}.
     */
    @Test
    public void testSerializationWithDerivedAttributeForcedToSerialize() {
        this.options.put(JsonResource.OPTION_SAVE_DERIVED_FEATURES, Boolean.valueOf(true));
        this.testSave("NodeSingleDerivedValueAttributeForcedToSerialize.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization with a model that contain a derived.
     */
    @Test
    public void testSerializationWithDerivedAttribute() {
        this.testSave("NodeSingleDerivedValueAttribute.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization with a model that contain a transient attribute but force serialization of derived
     * features by setting the specific option {@link JsonResource#OPTION_SAVE_TRANSIENT_FEATURES}.
     */
    @Test
    public void testSerializationWithTransientAttributeForcedToSerialize() {
        this.options.put(JsonResource.OPTION_SAVE_TRANSIENT_FEATURES, Boolean.valueOf(true));
        this.testSave("NodeSingleTransientValueAttributeForcedToSerialize.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization with a model that contain a transient attribute.
     */
    @Test
    public void testSerializationWithTransientAttribute() {
        this.testSave("NodeSingleTransientValueAttribute.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of a model with dynamic instances.
     */
    @Test
    public void testSerializationOfDynamicInstance() {
        this.options.put(JsonResource.OPTION_DISPLAY_DYNAMIC_INSTANCES, Boolean.TRUE);
        this.testSave("TestDynamicInstanceOption.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of a sub tree of EObject contained in a resource.
     */
    @Test
    public void testSerializationOfSubTree() {
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
     * test the serialization of a model with URIHandler.
     */
    @Test
    public void testSerializationWithURIHandler() {
        // CHECKSTYLE:OFF
        JsonResource.URIHandler handler = new JsonResource.URIHandler() {

            @Override
            public void setBaseURI(URI uri) {
                // do nothing
            }

            @Override
            public URI resolve(URI uriToResolve) {
                return null;
            }

            @Override
            public URI deresolve(URI uriToDeresolve) {
                URI anURI = URI.createURI("http://www.eclipse.org/emfjson/eclipse/emf/ecore/"); //$NON-NLS-1$
                return uriToDeresolve.deresolve(anURI, true, true, false);
            }
        };
        // CHECKSTYLE:ON
        this.options.put(JsonResource.OPTION_URI_HANDLER, handler);

        this.testSave("TestURIHandlerOption.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of a model with Resource EntityHandler.
     */
    @Test
    public void testSerializationWithResourceEntityHandler() {
        JsonResource.ResourceEntityHandler handler = new JsonResource.ResourceEntityHandler() {

            @Override
            public void reset() {
                //
            }

            @Override
            public void handleEntity(String entityName, String entityValue) {
                //
            }

            @Override
            public Map<String, String> getNameToValueMap() {
                return null;
            }

            @Override
            public String getEntityName(String entityValue) {
                if ("http://www.eclipse.org/emf/2002/Ecore".equals(entityValue)) { //$NON-NLS-1$
                    return "EcoreEntity"; //$NON-NLS-1$
                }
                return null;
            }
        };
        this.options.put(JsonResource.OPTION_RESOURCE_ENTITY_HANDLER, handler);

        this.testSave("TestResourceEntityHandlerOption.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization with resource handler on postSaving.
     */
    @Test(expected = AssertionError.class)
    public void testSerializationWithResourceHandlerOnPostSaving() {
        // CHECKSTYLE:OFF
        // This resource handler add a '}' at the end of the result. That throws an error on assertEquals()
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
            public void postSave(JsonResource resource, OutputStream outputStream, Map<?, ?> saveOptions) {
                try {
                    // 35484541 : byte value for '}'
                    outputStream.write(35484541);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void postLoad(JsonResource resource, InputStream inputStream, Map<?, ?> loadOptions) {
                // do nothing
            }
        };
        // CHECKSTYLE:ON

        this.options.put(JsonResource.OPTION_RESOURCE_HANDLER, resourceHandler);
        this.testSave("SimpleModelWithOnlyDefaultValueForResourceHandlerPostSavingTes.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization with resource handler on preSaving.
     */
    @Test
    public void testSerializationWithResourceHandlerOnPreSaving() {
        JsonResource.ResourceHandler resourceHandler = new JsonResource.ResourceHandler() {

            @Override
            public void preSave(JsonResource resource, OutputStream outputStream, Map<?, ?> saveOptions) {
                saveOptions.clear();
            }

            @Override
            public void preLoad(JsonResource resource, InputStream inputStream, Map<?, ?> loadOptions) {
                // do nothing
            }

            @Override
            public void postSave(JsonResource resource, OutputStream outputStream, Map<?, ?> saveOptions) {
                // do nothing
            }

            @Override
            public void postLoad(JsonResource resource, InputStream inputStream, Map<?, ?> loadOptions) {
                // do nothing
            }
        };

        this.options.put(JsonResource.OPTION_RESOURCE_HANDLER, resourceHandler);
        this.testSave("SimpleModelWithOnlyDefaultValueForResourceHandlerPreSavingTest.xmi"); //$NON-NLS-1$

    }
}
