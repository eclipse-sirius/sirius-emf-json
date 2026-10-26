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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.junit.Test;

/**
 * Verifies direct character loading, byte-stream fallbacks, and the Resource load lifecycle.
 */
public class StringLoadingTests {

    private static final String NODE_JSON = """
            {
              "json": {},
              "ns": {"m": "urn:model"},
              "content": [{"eClass": "m:Node", "data": {"name": "é漢字😀"}}]
            }
            """;

    @Test
    public void stringLoadInvokesJsonCallbacksInOrderAndLoadsTheNode() throws Exception {
        var resource = this.createResourceWithRegisteredPackage(Map.of());
        var invokedCallbacks = new ArrayList<String>();
        var objectHandler = new JsonResource.IEObjectHandler() {
            @Override
            public JsonElement processSerializedContent(JsonElement json, EObject object) {
                return json;
            }

            @Override
            public EObject processDeserializedContent(EObject object, JsonElement json) {
                invokedCallbacks.add("processDeserializedContent");
                return object;
            }
        };
        var resourceProcessor = new JsonResource.IJsonResourceProcessor.NoOp() {
            @Override
            public void preDeserialization(JsonResource jsonResource, JsonObject jsonObject) {
                invokedCallbacks.add("preDeserialization");
            }

            @Override
            public void postObjectLoading(JsonResource jsonResource, EObject object, JsonObject jsonObject, boolean isTopObject) {
                invokedCallbacks.add("postObjectLoading");
            }
        };

        resource.loadFromString(NODE_JSON, Map.of(JsonResource.OPTION_EOBJECT_HANDLER, objectHandler,
                JsonResource.OPTION_JSON_RESSOURCE_PROCESSOR, resourceProcessor));

        assertThat(resource.isLoaded()).isTrue();
        assertThat(invokedCallbacks).containsExactly("preDeserialization", "processDeserializedContent", "postObjectLoading");
        this.assertLoadedNodeName(resource, "é漢字😀");
    }

    @Test
    public void resourceHandlerReceivesUtf8BytesAndLoadCallbacksInOrder() throws Exception {
        var invokedCallbacks = new ArrayList<String>();
        var handler = this.createUtf8CheckingResourceHandler(invokedCallbacks);
        var resource = this.createResourceWithRegisteredPackage(Map.of(JsonResource.OPTION_RESOURCE_HANDLER, handler));

        resource.loadFromString(NODE_JSON, Map.of());

        assertThat(invokedCallbacks).containsExactly("preLoad", "postLoad");
        this.assertLoadedNodeName(resource, "é漢字😀");
    }

    @Test
    public void nullEncodingDefaultsToUtf8WhenAResourceHandlerRequiresBytes() throws Exception {
        var invokedCallbacks = new ArrayList<String>();
        var handler = this.createUtf8CheckingResourceHandler(invokedCallbacks);
        var resource = this.createResourceWithRegisteredPackage(Map.of(JsonResource.OPTION_RESOURCE_HANDLER, handler));
        var options = new HashMap<Object, Object>();
        options.put(JsonResource.OPTION_ENCODING, null);

        resource.loadFromString(NODE_JSON, options);

        assertThat(invokedCallbacks).containsExactly("preLoad", "postLoad");
        this.assertLoadedNodeName(resource, "é漢字😀");
    }

    @Test
    public void alreadyLoadedResourceIgnoresNullInputAndKeepsItsExistingObject() throws Exception {
        var resource = this.createResourceWithRegisteredPackage(Map.of());
        resource.loadFromString(NODE_JSON, Map.of());
        this.assertLoadedNodeName(resource, "é漢字😀");
        EObject originalNode = resource.getContents().get(0);

        resource.loadFromString(null, null);

        assertThat(resource.isLoaded()).isTrue();
        assertThat(resource.getContents()).containsExactly(originalNode);
    }

    @Test
    public void byteConsumedByDoLoadOverrideIsNotReplayedDuringJsonParsing() throws Exception {
        var prefixConsumingResource = new JsonResourceImpl(URI.createURI("test.json"), Map.of()) {
            @Override
            protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
                assertThat(inputStream.read()).isEqualTo('X');
                super.doLoad(inputStream, options);
            }
        };
        this.registerModelPackageAndAttachResource(prefixConsumingResource);

        prefixConsumingResource.loadFromString("X" + NODE_JSON, Map.of());

        this.assertLoadedNodeName(prefixConsumingResource, "é漢字😀");
    }

    private JsonResource.ResourceHandler createUtf8CheckingResourceHandler(List<String> invokedCallbacks) {
        return new JsonResource.ResourceHandler() {
            @Override
            public void preLoad(JsonResource resource, InputStream inputStream, Map<?, ?> options) {
                invokedCallbacks.add("preLoad");
                assertThat(options.get(JsonResource.OPTION_ENCODING)).isEqualTo(JsonResource.ENCODING_UTF_8);
                assertThat(inputStream).isInstanceOf(ByteArrayInputStream.class);
                var byteStream = (ByteArrayInputStream) inputStream;
                assertThat(byteStream.readAllBytes()).isEqualTo(NODE_JSON.getBytes(StandardCharsets.UTF_8));
                // Leave the complete document available to the loader after inspecting its encoding.
                byteStream.reset();
            }

            @Override
            public void postLoad(JsonResource resource, InputStream inputStream, Map<?, ?> options) {
                invokedCallbacks.add("postLoad");
            }

            @Override
            public void preSave(JsonResource resource, OutputStream outputStream, Map<?, ?> options) {
            }

            @Override
            public void postSave(JsonResource resource, OutputStream outputStream, Map<?, ?> options) {
            }
        };
    }

    private JsonResourceImpl createResourceWithRegisteredPackage(Map<?, ?> resourceOptions) {
        var resource = new JsonResourceImpl(URI.createURI("test.json"), resourceOptions);
        this.registerModelPackageAndAttachResource(resource);
        return resource;
    }

    private void registerModelPackageAndAttachResource(JsonResourceImpl resource) {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setNsURI("urn:model");
        var eClass = EcoreFactory.eINSTANCE.createEClass();
        eClass.setName("Node");
        var nameAttribute = EcoreFactory.eINSTANCE.createEAttribute();
        nameAttribute.setName("name");
        nameAttribute.setEType(EcorePackage.Literals.ESTRING);
        eClass.getEStructuralFeatures().add(nameAttribute);
        ePackage.getEClassifiers().add(eClass);
        var resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
        resourceSet.getResources().add(resource);
    }

    private void assertLoadedNodeName(JsonResource resource, String expectedName) {
        assertThat(resource.getContents()).hasSize(1);
        EObject node = resource.getContents().get(0);
        assertThat(node.eClass().getName()).isEqualTo("Node");
        assertThat(node.eGet(node.eClass().getEStructuralFeature("name"))).isEqualTo(expectedName);
    }
}
