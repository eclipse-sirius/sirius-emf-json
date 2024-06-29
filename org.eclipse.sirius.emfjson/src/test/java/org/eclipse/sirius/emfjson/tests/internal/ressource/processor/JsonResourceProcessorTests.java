/*******************************************************************************
 * Copyright (c) 2024 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.emfjson.tests.internal.ressource.processor;

import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests class for JsonResourceProcessor.
 *
 * @author <a href="mailto:michael.charfadi@obeo.fr">Michael Charfadi</a>
 */
public class JsonResourceProcessorTests {
    /**
     * LF string.
     */
    protected static final String LF = "\n"; //$NON-NLS-1$

    /**
     * CR LF string.
     */
    protected static final String CRLF = "\r\n"; //$NON-NLS-1$

    /**
     * The path of the folder containing the models used during the tests of this class.
     */
    private static final String ROOT_PATH = "/unit/resource/processor/"; //$NON-NLS-1$

    /**
     * The name of the JSON file used during the tests of this class.
     */
    private static final String FILE_NAME = "EmptyResourceWithAddedField.json"; //$NON-NLS-1$

    /**
     * Serializations options.
     */
    protected Map<Object, Object> saveOptions = new HashMap<Object, Object>();

    /**
     * DeSerializations options.
     */
    protected Map<Object, Object> loadOptions = new HashMap<Object, Object>();

    @Test
    public void testPreDeserialization() {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(JsonResourceFactoryImpl.EXTENSION, new JsonResourceFactoryImpl());

        Resource resource = resourceSet.createResource(URI.createURI("inmemory.json")); //$NON-NLS-1$

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            this.saveOptions.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);
            this.saveOptions.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, JsonResource.INDENT_2_SPACES);
            this.saveOptions.put(JsonResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);

            JsonResource.IJsonResourceProcessor jsonResourceProcessor = new JsonResource.IJsonResourceProcessor.NoOp() {
                @Override
                public void postSerialization(JsonResource resource, JsonObject jsonObject) {
                    jsonObject.addProperty("version", "versionNumber"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            };

            this.saveOptions.put(JsonResource.OPTION_JSON_RESSOURCE_PROCESSOR, jsonResourceProcessor);

            resource.save(outputStream, this.saveOptions);

            String json = new String(outputStream.toByteArray(), Charset.forName("utf-8")); //$NON-NLS-1$
            String expectedResult = this.readResource(FILE_NAME);

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

    @Test
    public void testPostSerialization() {
        final URL fileURL = this.getClass().getResource(ROOT_PATH + FILE_NAME);

        Resource resource = null;
        InputStream inputStream = null;
        try {
            inputStream = fileURL.openStream();
            final URI uri = URI.createURI(fileURL.toString());
            ResourceSet resourceSet = new ResourceSetImpl();

            JsonResourceFactoryImpl jsonResourceFactory = new JsonResourceFactoryImpl();
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(JsonResourceFactoryImpl.EXTENSION, jsonResourceFactory);
            resource = resourceSet.createResource(uri);

            JsonResource.IJsonResourceProcessor jsonResourceProcessor = new JsonResource.IJsonResourceProcessor.NoOp() {
                @Override
                public void preDeserialization(JsonResource resource, JsonObject jsonObject) {
                    String versionValue = jsonObject.get("version").getAsString(); //$NON-NLS-1$
                    Assert.assertEquals(versionValue, "versionNumber"); //$NON-NLS-1$
                }
            };

            this.loadOptions.put(JsonResource.OPTION_JSON_RESSOURCE_PROCESSOR, jsonResourceProcessor);

            resource.load(inputStream, this.loadOptions);
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
    }

    /**
     * Returns the String value of the file represents by the given filename.
     *
     * @param jsonResourceName
     *            the file name to read
     * @return the String value of the file
     */
    protected String readResource(String jsonResourceName) {
        URL jsonResourceUrl = this.getClass().getResource(ROOT_PATH + jsonResourceName);
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
}
