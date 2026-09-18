/*******************************************************************************
 * Copyright (c) 2026 Obeo.
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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.junit.Test;

/**
 * Tests loading directly from JSON characters.
 */
public class StringLoadingTests {

    private static final String JSON = "{\"json\":{},\"ns\":{\"m\":\"urn:model\"},\"content\":[{\"eClass\":\"m:Node\"}]}";

    @Test
    public void stringLoadingPreservesJsonCallbacksAndResourceLifecycle() throws Exception {
        var resource = this.resource();
        boolean[] callbacks = new boolean[3];
        var handler = new JsonResource.IEObjectHandler() {
            @Override
            public JsonElement processSerializedContent(JsonElement json, EObject object) {
                return json;
            }

            @Override
            public EObject processDeserializedContent(EObject object, JsonElement json) {
                callbacks[1] = true;
                return object;
            }
        };
        var processor = new JsonResource.IJsonResourceProcessor.NoOp() {
            @Override
            public void preDeserialization(JsonResource jsonResource, JsonObject jsonObject) {
                callbacks[0] = true;
            }

            @Override
            public void postObjectLoading(JsonResource jsonResource, EObject object, JsonObject jsonObject, boolean isTopObject) {
                callbacks[2] = true;
            }
        };

        resource.loadFromString(JSON, Map.of(JsonResource.OPTION_EOBJECT_HANDLER, handler,
                JsonResource.OPTION_JSON_RESSOURCE_PROCESSOR, processor));

        assertThat(resource.isLoaded()).isTrue();
        assertThat(callbacks).containsOnly(true);
        assertThat(resource.getContents()).hasSize(1);
    }

    @Test
    public void resourceHandlerRetainsTheInputStreamPath() throws Exception {
        boolean[] callbacks = new boolean[2];
        var handler = new JsonResource.ResourceHandler() {
            @Override
            public void preLoad(JsonResource jsonResource, InputStream inputStream, Map<?, ?> options) {
                callbacks[0] = true;
                assertThat(inputStream).isInstanceOf(ByteArrayInputStream.class);
            }

            @Override
            public void postLoad(JsonResource jsonResource, InputStream inputStream, Map<?, ?> options) {
                callbacks[1] = true;
            }

            @Override
            public void preSave(JsonResource jsonResource, OutputStream outputStream, Map<?, ?> options) {
                // Nothing to do.
            }

            @Override
            public void postSave(JsonResource jsonResource, OutputStream outputStream, Map<?, ?> options) {
                // Nothing to do.
            }
        };
        var resource = this.resource(Map.of(JsonResource.OPTION_RESOURCE_HANDLER, handler));

        resource.loadFromString(JSON, Map.of());

        assertThat(callbacks).containsOnly(true);
        assertThat(resource.getContents()).hasSize(1);
    }

    @Test
    public void nullEncodingUsesUtf8AndAnAlreadyLoadedResourceIsIgnored() throws Exception {
        var resource = this.resource();
        var options = new HashMap<Object, Object>();
        options.put(JsonResource.OPTION_ENCODING, null);

        resource.loadFromString(JSON, options);
        resource.loadFromString(null, null);

        assertThat(resource.getContents()).hasSize(1);
    }

    @Test
    public void consumedPrefixIsNotReplayed() throws Exception {
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of()) {
            @Override
            protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
                assertThat(inputStream.read()).isEqualTo('X');
                super.doLoad(inputStream, options);
            }
        };
        this.addToResourceSet(resource);

        resource.loadFromString("X" + JSON, Map.of());

        assertThat(resource.getContents()).hasSize(1);
    }

    private JsonResourceImpl resource() {
        return this.resource(Map.of());
    }

    private JsonResourceImpl resource(Map<?, ?> options) {
        var resource = new JsonResourceImpl(URI.createURI("test.json"), options);
        this.addToResourceSet(resource);
        return resource;
    }

    private void addToResourceSet(JsonResourceImpl resource) {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setNsURI("urn:model");
        var eClass = EcoreFactory.eINSTANCE.createEClass();
        eClass.setName("Node");
        ePackage.getEClassifiers().add(eClass);
        var resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
        resourceSet.getResources().add(resource);
    }
}
