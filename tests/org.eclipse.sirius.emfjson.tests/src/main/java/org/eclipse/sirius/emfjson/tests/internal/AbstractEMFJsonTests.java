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
package org.eclipse.sirius.emfjson.tests.internal;

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.junit.Assert;

/**
 * Common super class of all the test classes.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public abstract class AbstractEMFJsonTests {
    /**
     * LF string.
     */
    protected static final String LF = "\n"; //$NON-NLS-1$

    /**
     * CR LF string.
     */
    protected static final String CRLF = "\r\n"; //$NON-NLS-1$

    /**
     * Serializations options.
     */
    protected Map<Object, Object> options = new HashMap<Object, Object>();

    /**
     * Returns the path of the folder containing the models used during the tests of this class.
     *
     * @return A path within this bundle
     */
    protected abstract String getRootPath();

    /**
     * Load the resource with the given name from the folder defined by the root path and compare the result of the save
     * with the expected result.
     *
     * @param resourceName
     *            The name of the resource to save
     */
    protected void testSave(String resourceName) {
        Resource modelResource = this.getModelResource(resourceName, true);

        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(JsonResourceFactoryImpl.EXTENSION, new JsonResourceFactoryImpl());

        String jsonResourceURI = modelResource.getURI().toString();
        jsonResourceURI = jsonResourceURI.substring(0, jsonResourceURI.lastIndexOf('.') + 1) + JsonResourceFactoryImpl.EXTENSION;
        Resource resource = resourceSet.createResource(URI.createURI(jsonResourceURI));
        resource.getContents().addAll(modelResource.getContents());

        resource.getResourceSet().getPackageRegistry().putAll(modelResource.getResourceSet().getPackageRegistry());

        String expectedResult = ""; //$NON-NLS-1$

        String jsonResourceName = resourceName.substring(0, resourceName.lastIndexOf('.') + 1) + JsonResourceFactoryImpl.EXTENSION;
        expectedResult = this.readResource(jsonResourceName);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            this.options.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);
            this.options.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, JsonResource.INDENT_2_SPACES);
            this.options.put(JsonResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);

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
     * Returns the String value of the file represents by the given filename.
     *
     * @param jsonResourceName
     *            the file name to read
     * @return the String value of the file
     */
    protected String readResource(String jsonResourceName) {
        URL jsonResourceUrl = this.getClass().getResource(this.getRootPath() + jsonResourceName);
        InputStream inputStream = null;
        InputStreamReader reader = null;
        String expectedResult = ""; //$NON-NLS-1$
        try {
            inputStream = jsonResourceUrl.openStream();
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
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }
        return expectedResult;
    }

    /**
     * Load the Json resource and the model saved by the XMI serialization and compare both of them to ensure that they
     * are identical.
     *
     * @param resourceName
     *            The name of the resource to load
     */
    protected void testLoad(String resourceName) {
        String jsonResourceName = resourceName.substring(0, resourceName.lastIndexOf('.') + 1) + JsonResourceFactoryImpl.EXTENSION;

        Resource jsonResource = this.getModelResource(jsonResourceName, false);

        List<EPackage> ePackageList = Lists.newArrayList();
        Set<Entry<String, Object>> entrySet = jsonResource.getResourceSet().getPackageRegistry().entrySet();
        for (Entry<String, Object> entry : entrySet) {
            Object value = entry.getValue();
            if (value instanceof EPackage) {
                ePackageList.add((EPackage) value);
            }
        }
        Resource modelResource = this.loadResource(this.getRootPath() + resourceName, ePackageList);
        modelResource.setURI(URI.createURI(this.getRootPath() + resourceName));
        modelResource.setURI(jsonResource.getURI());

        Assert.assertTrue(this.equals(modelResource.getContents(), jsonResource.getContents()));

    }

    /**
     * Return whether the two given objects are equals.
     *
     * @param eObject1
     *            the first Object
     * @param eObject2
     *            the Second Object
     * @return whether the two given objects are equals
     */
    @SuppressWarnings("unchecked")
    protected boolean equals(List<? extends EObject> eObject1, List<? extends EObject> eObject2) {
        return new ExtendedEqualityHelper().equals((List<EObject>) eObject1, (List<EObject>) eObject2);
    }

    /**
     * Returns the resource with the given name.
     *
     * @param resourceName
     *            The name of the resource
     * @param loadMetamodel
     *            Indicates if the meta-model should be loaded directly
     * @return The resource
     */
    protected Resource getModelResource(String resourceName, boolean loadMetamodel) {
        Resource resource = null;
        if (loadMetamodel) {
            ArrayList<EPackage> arrayList = new ArrayList<EPackage>();
            arrayList.add(this.loadMetaModel("/nodes.ecore")); //$NON-NLS-1$
            arrayList.add(this.loadMetaModel("/unit/references/noncontainment/extlibrary.ecore")); //$NON-NLS-1$
            if (!arrayList.isEmpty()) {
                resource = this.loadResource(this.getRootPath() + resourceName, arrayList);
            }
        } else {
            resource = this.loadResource(this.getRootPath() + resourceName, Lists.<EPackage> newArrayList());
        }
        return resource;
    }

    /**
     * Load the an ecore model an return its EPackage.
     *
     * @param mModelPath
     *            the path where the ecore model is
     * @return the EPackage match the mModelPath
     */
    private EPackage loadMetaModel(String mModelPath) {
        EPackage ePackage = null;
        Resource ecoreMetamodelResource = this.loadResource(mModelPath, Lists.newArrayList((EPackage) EcorePackage.eINSTANCE));
        if (ecoreMetamodelResource != null && ecoreMetamodelResource.getContents().size() > 0 && ecoreMetamodelResource.getContents().get(0) instanceof EPackage) {
            ePackage = (EPackage) ecoreMetamodelResource.getContents().get(0);

        }
        return ePackage;
    }

    /**
     * Loads the resource with the given path in a new resource set and return the resource.
     *
     * @param resourcePath
     *            The path of the resource to load
     * @param ePackages
     *            The packages to register in the resource set
     * @return The resource loaded
     */
    protected Resource loadResource(String resourcePath, List<EPackage> ePackages) {
        final URL fileURL = AbstractEMFJsonTests.class.getResource(resourcePath);

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
            JsonResourceFactoryImpl jsonResourceFactory = new JsonResourceFactoryImpl() {
                @Override
                public Resource createResource(URI uriValue) {
                    return new JsonResourceImpl(uriValue, AbstractEMFJsonTests.this.options);
                }
            };
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(JsonResourceFactoryImpl.EXTENSION, jsonResourceFactory);

            // resourceFactory cannot be null
            resource = resourceSet.createResource(uri);
            String extension = resource.getURI().fileExtension();
            if (extension.equals(JsonResourceFactoryImpl.EXTENSION)) {
                resource.load(inputStream, this.options);
            } else {
                resource.load(inputStream, Collections.EMPTY_MAP);
            }
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
     * Extended version of EMF's EcoreUtil.EqualityHelper to support bytearrays
     *
     * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
     */
    private static class ExtendedEqualityHelper extends EcoreUtil.EqualityHelper {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean equalValues(Object value1, Object value2) {
            boolean result = false;
            if (value1 instanceof byte[] && value2 instanceof byte[]) {
                result = Arrays.equals((byte[]) value1, (byte[]) value2);
            } else if (value1 instanceof List<?> && value2 instanceof List<?>) {
                result = this.listEquality((List<?>) value1, (List<?>) value2);
            } else {
                result = super.equalValues(value1, value2);
            }
            return result;
        }

        /**
         * Replace the AbstractEList#equals call, it useful when values to test are ByteArray typed.
         *
         * @param list1
         *            the first list
         * @param list2
         *            the second list
         * @return return whether the two given lists are equals
         */
        public boolean listEquality(List<?> list1, List<?> list2) {
            boolean equals = true;

            int size = list1.size();
            if (list2.size() != size) {
                equals = false;
            }

            Iterator<?> objects = list2.iterator();
            int i = 0;
            while (equals && i < size) {
                Object o1 = list1.get(i);
                Object o2 = objects.next();

                if (o1 == null && o2 != null) {
                    equals = false;
                }
                if (o1 != null) {
                    if ((!(o1 instanceof byte[]) || !(o2 instanceof byte[])) && !o1.equals(o2)) {
                        equals = false;
                    }
                    if (o1 instanceof byte[] && o2 instanceof byte[] && !Arrays.equals((byte[]) o1, (byte[]) o2)) {
                        equals = false;
                    }
                }

                ++i;
            }
            return equals;
        }
    };
}
