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

import com.google.common.io.CharStreams;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.sirius.emfjson.utils.GsonResourceSerializer;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class test is used in order to test the serialization of common Object containing a Resource as Attribute.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class SerializeCommonObjectWithResourceAsAttribute {
    /**
     * LF string.
     */
    protected static final String LF = "\n"; //$NON-NLS-1$

    /**
     * CR LF string.
     */
    protected static final String CRLF = "\r\n"; //$NON-NLS-1$

    /**
     * The serialize options.
     */
    private Map<Object, Object> options = new HashMap<Object, Object>();

    /**
     * Test the serialization of an object containing Resource as attribute.
     */
    @Test
    public void testSerializingObjectContainingResourceAsAttributes() {

        ResourceSetImpl resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(JsonResourceFactoryImpl.EXTENSION, new JsonResourceFactoryImpl());

        Resource resource = resourceSet.createResource(URI.createURI("tata.json")); //$NON-NLS-1$

        EcoreFactory ecoreFactory = EcorePackage.eINSTANCE.getEcoreFactory();

        EPackage ePackage = ecoreFactory.createEPackage();
        ePackage.setName("test"); //$NON-NLS-1$
        ePackage.setNsPrefix("nsPrefix"); //$NON-NLS-1$
        ePackage.setNsURI("nsURI"); //$NON-NLS-1$

        resource.getContents().add(ePackage);

        AnObject object = new AnObject(resource);
        object.setName("tata"); //$NON-NLS-1$

        TypeToken<Resource> typeToken = new TypeToken<Resource>() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;
            //
        };

        this.options.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);
        this.options.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, JsonResource.INDENT_2_SPACES);
        GsonResourceSerializer serializer = new GsonResourceSerializer(this.options);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.registerTypeAdapter(typeToken.getType(), serializer).disableHtmlEscaping().setPrettyPrinting().create();

        String json = gson.toJson(object);
        String expectedResult = ""; //$NON-NLS-1$
        URL resourceURL = SerializeCommonObjectWithResourceAsAttribute.class.getResource("/integration/examples/TestCommonObjectWithResourceAsAttribute.json"); //$NON-NLS-1$
        InputStream inputStream = null;
        InputStreamReader reader = null;
        try {
            inputStream = resourceURL.openStream();
            reader = new InputStreamReader(inputStream, Charset.forName("utf-8")); //$NON-NLS-1$
            expectedResult = CharStreams.toString(reader);
        } catch (IOException e) {
            e.printStackTrace();
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
        Assert.assertEquals(expectedResult.replaceAll(CRLF, LF), json.replaceAll(CRLF, LF));
    }

    /**
     * An Internal class containing a resource as Attribute.
     *
     * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
     */
    public class AnObject {

        /**
         * A name.
         */
        private String name;

        /**
         * A Resource.
         */
        private Resource resource;

        /**
         * The constructor.
         *
         * @param resource
         *            The Resource
         */
        public AnObject(Resource resource) {
            this.setResource(resource);
        }

        /**
         * Returns the name.
         *
         * @return The name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Sets the name.
         *
         * @param name
         *            The name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns the resource.
         *
         * @return The resource
         */
        public Resource getResource() {
            return this.resource;
        }

        /**
         * Sets the resource.
         *
         * @param resource
         *            The resource to set
         */
        public void setResource(Resource resource) {
            this.resource = resource;
        }
    }

}
