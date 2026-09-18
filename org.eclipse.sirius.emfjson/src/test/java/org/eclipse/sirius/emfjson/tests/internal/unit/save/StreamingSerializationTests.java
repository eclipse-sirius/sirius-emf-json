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
package org.eclipse.sirius.emfjson.tests.internal.unit.save;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.eclipse.sirius.emfjson.utils.JsonHelper;
import org.junit.Test;

/**
 * Checks streaming through the resource save and load APIs.
 */
public class StreamingSerializationTests {
    @Test
    public void frozenAndMutableGraphsPreserveContentAndRoundTrip() throws IOException {
        for (boolean frozen : new boolean[] {true, false}) {
            var resource = this.createGraph(frozen);
            byte[] expected = this.save(resource, Map.of());
            byte[] streamed = this.save(resource, Map.of(JsonResource.OPTION_STREAMING, true));
            assertThat(this.parse(streamed)).isEqualTo(this.parse(expected));
            assertThat(this.save(resource, Map.of())).isEqualTo(expected);
            assertThat(this.parse(streamed).keySet().iterator().next()).isEqualTo("content");
            var data = this.parse(streamed).getAsJsonArray("content").get(0).getAsJsonObject().getAsJsonObject("data");
            assertThat(data.getAsJsonObject("child").has("data")).isFalse();

            var loaded = new JsonResourceImpl(resource.getURI(), Map.of());
            var resourceSet = new ResourceSetImpl();
            var ePackage = resource.getContents().get(0).eClass().getEPackage();
            resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
            resourceSet.getResources().add(loaded);
            loaded.load(new ByteArrayInputStream(streamed), Map.of());
            assertThat(EcoreUtil.equals(resource.getContents(), loaded.getContents())).isTrue();
            EObject root = loaded.getContents().get(0);
            assertThat(root.eGet(root.eClass().getEStructuralFeature("target"))).isSameAs(root.eGet(root.eClass().getEStructuralFeature("child")));
        }
    }

    @Test
    public void ecoreFallbackPreservesTree() throws IOException {
        var resource = this.createGraph(false);
        var ePackage = resource.getContents().get(0).eClass().getEPackage();
        resource.getContents().clear();
        resource.getContents().add(ePackage);
        assertThat(this.parse(this.save(resource, Map.of(JsonResource.OPTION_STREAMING, true)))).isEqualTo(this.parse(this.save(resource, Map.of())));
        var generic = EcoreFactory.eINSTANCE.createEGenericType();
        generic.setEClassifier(EcorePackage.Literals.ESTRING);
        resource.getContents().clear();
        resource.getContents().add(generic);
        assertThat(this.parse(this.save(resource, Map.of(JsonResource.OPTION_STREAMING, true)))).isEqualTo(this.parse(this.save(resource, Map.of())));
    }

    @Test
    public void encodedIndentedOutputPreservesCharacters() throws IOException {
        var resource = this.createGraph(true);
        EObject root = resource.getContents().get(0);
        root.eSet(root.eClass().getEStructuralFeature("names"), List.of("<>&é😀", "\"\n\t"));
        var options = new HashMap<Object, Object>();
        options.put(JsonResource.OPTION_ENCODING, "UTF-16LE");
        options.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, "  ");
        var expected = JsonParser.parseString(new String(this.save(resource, options), StandardCharsets.UTF_16LE));
        options.put(JsonResource.OPTION_STREAMING, true);
        String streamed = new String(this.save(resource, options), StandardCharsets.UTF_16LE);
        assertThat(streamed).contains("\n  \"content\"", "<>&é😀");
        assertThat(JsonParser.parseString(streamed)).isEqualTo(expected);
    }

    @Test
    public void collidingFeatureNamesUseTreeOverwriteSemantics() throws IOException {
        var resource = this.createGraph(false);
        EClass type = resource.getContents().get(0).eClass();
        type.getEStructuralFeature("flags").setName("names");
        EcoreUtil.freeze(type.getEPackage());
        byte[] streamed = this.save(resource, Map.of(JsonResource.OPTION_STREAMING, true));
        assertThat(this.parse(streamed)).isEqualTo(this.parse(this.save(resource, Map.of())));
        assertThat(new String(streamed, StandardCharsets.UTF_8)).containsOnlyOnce("\"names\":");
    }

    @Test
    public void unsupportedOptionsFailBeforeWriting() {
        var handler = new JsonResource.IEObjectHandler() {
            @Override
            public JsonElement processSerializedContent(JsonElement json, EObject object) {
                return json;
            }

            @Override
            public EObject processDeserializedContent(EObject object, JsonElement json) {
                return object;
            }
        };
        for (var entry : Map.of(JsonResource.OPTION_EOBJECT_HANDLER, handler, JsonResource.OPTION_CUSTOM_HELPER, new JsonHelper(),
                JsonResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE).entrySet()) {
            var output = new ByteArrayOutputStream();
            var options = Map.of(JsonResource.OPTION_STREAMING, true, entry.getKey(), entry.getValue());
            assertThatThrownBy(() -> this.createGraph(true).save(output, options)).isInstanceOf(IllegalArgumentException.class);
            assertThat(output.size()).isZero();
        }
    }

    @Test
    public void streamingSkipsObjectCallbacksButKeepsNamespaceAndReferenceCallbacks() throws IOException {
        for (boolean frozen : new boolean[] {true, false}) {
            var objects = new AtomicInteger();
            var namespaces = new AtomicInteger();
            var references = new AtomicInteger();
            var listener = new JsonResource.ISerializationListener.NoOp() {
                @Override
                public void onObjectSerialized(EObject object, JsonElement json) {
                    objects.incrementAndGet();
                }

                @Override
                public void onNsHeaderEntryAdded(String prefix, String uri) {
                    namespaces.incrementAndGet();
                }

                @Override
                public void onCrossReferenceURICreated(EObject object, EReference reference, String uri) {
                    references.incrementAndGet();
                }
            };
            var resource = this.createGraph(frozen);
            EObject root = resource.getContents().get(0);
            var external = new JsonResourceImpl(URI.createURI("external.json"), Map.of());
            external.getContents().add(EcoreUtil.create(root.eClass()));
            root.eSet(root.eClass().getEStructuralFeature("target"), external.getContents().get(0));
            this.save(resource, Map.of(JsonResource.OPTION_SERIALIZATION_LISTENER, listener));
            assertThat(objects.getAndSet(0)).isPositive();
            int expectedNamespaces = namespaces.getAndSet(0);
            int expectedReferences = references.getAndSet(0);
            this.save(resource, Map.of(JsonResource.OPTION_STREAMING, true, JsonResource.OPTION_SERIALIZATION_LISTENER, listener));
            assertThat(objects.get()).isZero();
            assertThat(namespaces.get()).isPositive().isEqualTo(expectedNamespaces);
            assertThat(references.get()).isPositive().isEqualTo(expectedReferences);
        }
    }

    @Test
    public void processorHeaderChangesSurviveButItsContentIsReplaced() throws IOException {
        var processor = new JsonResource.IJsonResourceProcessor.NoOp() {
            @Override
            public void postSerialization(JsonResource resource, JsonObject json) {
                assertThat(json.keySet()).containsExactly("json", "ns");
                json.getAsJsonObject("json").addProperty("custom", true);
                json.addProperty("extra", "kept");
                json.addProperty("content", "discarded");
            }
        };
        var resource = this.createGraph(true);
        var options = new HashMap<Object, Object>();
        options.put(JsonResource.OPTION_JSON_RESSOURCE_PROCESSOR, processor);
        JsonObject expected = this.parse(this.save(resource, options));
        options.put(JsonResource.OPTION_STREAMING, true);
        assertThat(this.parse(this.save(resource, options))).isEqualTo(expected);
    }

    private JsonResourceImpl createGraph(boolean frozen) {
        var ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("model");
        ePackage.setNsPrefix("model");
        ePackage.setNsURI("urn:streaming");
        EClass type = EcoreFactory.eINSTANCE.createEClass();
        type.setName("Node");
        ePackage.getEClassifiers().add(type);
        for (String name : List.of("names", "flags")) {
            var attribute = EcoreFactory.eINSTANCE.createEAttribute();
            attribute.setName(name);
            attribute.setEType(name.equals("names") ? EcorePackage.Literals.ESTRING : EcorePackage.Literals.EBOOLEAN);
            attribute.setUpperBound(-1);
            type.getEStructuralFeatures().add(attribute);
        }
        for (String name : List.of("child", "children", "target")) {
            var reference = EcoreFactory.eINSTANCE.createEReference();
            reference.setName(name);
            reference.setEType(type);
            reference.setContainment(!name.equals("target"));
            reference.setUpperBound(name.equals("children") ? -1 : 1);
            type.getEStructuralFeatures().add(reference);
        }
        if (frozen) {
            EcoreUtil.freeze(ePackage);
        }
        EObject root = EcoreUtil.create(type);
        EObject child = EcoreUtil.create(type);
        root.eSet(type.getEStructuralFeature("names"), List.of("first", "quote\"\nlast"));
        root.eSet(type.getEStructuralFeature("flags"), List.of(true, false));
        root.eSet(type.getEStructuralFeature("child"), child);
        root.eSet(type.getEStructuralFeature("children"), List.of(EcoreUtil.create(type), EcoreUtil.create(type)));
        root.eSet(type.getEStructuralFeature("target"), child);
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        resource.getContents().add(root);
        return resource;
    }

    private byte[] save(JsonResource resource, Map<?, ?> options) throws IOException {
        var output = new ByteArrayOutputStream();
        resource.save(output, options);
        return output.toByteArray();
    }

    private JsonObject parse(byte[] bytes) {
        return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    }
}
