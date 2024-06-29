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
package org.eclipse.sirius.emfjson.tests.internal.options;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResource.IEObjectHandler;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class test for EObject Handler.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class EObjectHandlerTests extends AbstractEMFJsonTests {

    /**
     * The meta data attribute.
     */
    private static final String INFO = "info"; //$NON-NLS-1$

    /**
     * An URI used to create resources.
     */
    private static final String URI_EXAMPLE = "test.json"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/options/"; //$NON-NLS-1$
    }

    /**
     * A test Adapter.
     */
    private class TestAdapter implements Adapter {
        /**
         * A Notifier.
         */
        private Notifier target;

        /**
         * The meta data we want to store.
         */
        private String info;

        /**
         * The constructor.
         *
         * @param info
         *            the meta data we want to store
         */
        TestAdapter(String info) {
            this.info = info;
        }

        /**
         * Returns the stored meta data.
         *
         * @return the stored meta data
         */
        public String getInfo() {
            return this.info;
        }

        @Override
        public void notifyChanged(Notification notification) {
            // do nothing
        }

        @Override
        public Notifier getTarget() {
            return this.target;
        }

        @Override
        public void setTarget(Notifier newTarget) {
            this.target = newTarget;
        }

        @Override
        public boolean isAdapterForType(Object type) {
            return true;
        }

    }

    /**
     * Test the loading of resource with EObject handler.
     */
    @Test
    public void testLoadEObjectHandler() {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("json", //$NON-NLS-1$
                new JsonResourceFactoryImpl());
        Resource resource = resourceSet.createResource(URI.createURI(EObjectHandlerTests.URI_EXAMPLE));

        EcoreFactory ecoreFactory = EcorePackage.eINSTANCE.getEcoreFactory();
        EPackage ePackage = ecoreFactory.createEPackage();
        ePackage.setName("testpackage"); //$NON-NLS-1$
        ePackage.setNsURI("testnsuri"); //$NON-NLS-1$
        ePackage.setNsPrefix("testnsprefix"); //$NON-NLS-1$

        ePackage.eAdapters().add(new TestAdapter("test")); //$NON-NLS-1$

        resource.getContents().add(ePackage);

        JsonResource.IEObjectHandler eObjectHandler = new JsonResource.IEObjectHandler() {

            @Override
            public EObject processDeserializedContent(EObject eObject, JsonElement jsonElement) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has(EObjectHandlerTests.INFO)) {
                    String value = jsonObject.get(EObjectHandlerTests.INFO).getAsString();
                    eObject.eAdapters().add(new TestAdapter(value));
                }
                return eObject;
            }

            @Override
            public JsonElement processSerializedContent(JsonElement jsonElement, EObject eObject) {
                return null;
            }
        };

        this.options.put(JsonResource.OPTION_EOBJECT_HANDLER, eObjectHandler);

        Resource loadedResource = this.loadResource(this.getRootPath() + "TestEObjectHandler.json", Lists.newArrayList((EPackage) EcorePackage.eINSTANCE)); //$NON-NLS-1$

        loadedResource.setURI(URI.createURI(EObjectHandlerTests.URI_EXAMPLE));
        Assert.assertTrue(this.equals(loadedResource.getContents(), resource.getContents()));
        Assert.assertTrue(this.getAdapterValue(loadedResource.getContents().get(0)).equals(this.getAdapterValue(resource.getContents().get(0))));
    }

    /**
     * Returns the adapter value related to the {@link TestAdapter}.
     *
     * @param eObject
     *            the EObject which hold the adapter
     * @return the value held by the adapter
     */
    private Object getAdapterValue(EObject eObject) {
        Adapter adapter = eObject.eAdapters().get(0);
        if (adapter instanceof TestAdapter) {
            TestAdapter testAdapter = (TestAdapter) adapter;
            return testAdapter.getInfo();
        }

        return null;
    }

    /**
     * Test the serialization of resource with EObjectHandler.
     */
    @Test
    public void testSaveEObjectHandler() {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("json", //$NON-NLS-1$
                new JsonResourceFactoryImpl());
        Resource resource = resourceSet.createResource(URI.createURI(EObjectHandlerTests.URI_EXAMPLE));

        EcoreFactory ecoreFactory = EcorePackage.eINSTANCE.getEcoreFactory();
        EPackage ePackage = ecoreFactory.createEPackage();
        ePackage.setName("testpackage"); //$NON-NLS-1$
        ePackage.setNsURI("testnsuri"); //$NON-NLS-1$
        ePackage.setNsPrefix("testnsprefix"); //$NON-NLS-1$

        ePackage.eAdapters().add(new TestAdapter("test")); //$NON-NLS-1$

        resource.getContents().add(ePackage);

        IEObjectHandler eObjectHandler = new JsonResource.IEObjectHandler() {

            @Override
            public JsonElement processSerializedContent(JsonElement jsonElement, EObject eObject) {
                Adapter adapter = eObject.eAdapters().get(0);
                if (adapter instanceof TestAdapter) {
                    TestAdapter testAdapter = (TestAdapter) adapter;
                    jsonElement.getAsJsonObject().addProperty(EObjectHandlerTests.INFO, testAdapter.getInfo());
                }
                return jsonElement;
            }

            @Override
            public EObject processDeserializedContent(EObject eObject, JsonElement jsonElement) {
                return null;
            }
        };

        this.options.put(JsonResource.OPTION_EOBJECT_HANDLER, eObjectHandler);
        this.options.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, JsonResource.INDENT_2_SPACES);
        this.options.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            resource.save(byteArrayOutputStream, this.options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String json = new String(byteArrayOutputStream.toByteArray());

        String expectedResult = this.readResource("TestEObjectHandler.json"); //$NON-NLS-1$

        Assert.assertEquals(expectedResult.replaceAll(CRLF, LF), json.replaceAll(CRLF, LF));

    }

}
