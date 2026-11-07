/*******************************************************************************
 * Copyright (c) 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.sirius.emfjson.tests.internal.options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.emfjson.resource.IDManager;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.junit.Test;

/**
 * Tests the optional flat EObject layout and loading both layouts.
 */
public class FlatEObjectFormatTests {

    @Test
    public void saveAndLoadBothLayouts() throws IOException {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("test");
        ePackage.setNsPrefix("test");
        ePackage.setNsURI("http://example.org/flat-eobject-test");
        EClass node = EcoreFactory.eINSTANCE.createEClass();
        node.setName("Node");
        ePackage.getEClassifiers().add(node);
        for (String name : new String[] { "id", "eClass", "data" }) {
            EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
            attribute.setName(name);
            attribute.setEType(EcorePackage.Literals.ESTRING);
            node.getEStructuralFeatures().add(attribute);
        }
        EReference children = EcoreFactory.eINSTANCE.createEReference();
        children.setName("children");
        children.setEType(node);
        children.setContainment(true);
        children.setUpperBound(-1);
        node.getEStructuralFeatures().add(children);

        TestIDManager idManager = new TestIDManager();
        Map<Object, Object> resourceOptions = Map.of(JsonResource.OPTION_ID_MANAGER, idManager);
        JsonResource source = this.createResource(ePackage, resourceOptions);
        EObject root = ePackage.getEFactoryInstance().create(node);
        EObject child = ePackage.getEFactoryInstance().create(node);
        root.eSet(node.getEStructuralFeature("id"), "feature-id");
        root.eSet(node.getEStructuralFeature("eClass"), "feature-class");
        root.eSet(node.getEStructuralFeature("data"), "feature-data");
        child.eSet(node.getEStructuralFeature("data"), "child-data");
        @SuppressWarnings("unchecked")
        java.util.List<EObject> childValues = (java.util.List<EObject>) root.eGet(children);
        childValues.add(child);
        source.getContents().add(root);
        idManager.setId(root, "root-id");
        idManager.setId(child, "child-id");

        JsonObject legacy = this.save(source, Map.of());
        JsonObject legacyRoot = legacy.getAsJsonArray("content").get(0).getAsJsonObject();
        assertEquals("root-id", legacyRoot.get("id").getAsString());
        assertTrue(legacyRoot.has("eClass"));
        assertTrue(legacyRoot.getAsJsonObject("data").has("children"));
        this.assertLoaded(ePackage, node, legacy, resourceOptions);

        JsonObject flat = this.save(source, Map.of(JsonResource.OPTION_SAVE_FLAT_EOBJECTS, Boolean.TRUE));
        JsonObject flatRoot = flat.getAsJsonArray("content").get(0).getAsJsonObject();
        assertEquals("root-id", flatRoot.get("@id").getAsString());
        assertEquals("feature-id", flatRoot.get("id").getAsString());
        assertEquals("feature-class", flatRoot.get("eClass").getAsString());
        assertEquals("feature-data", flatRoot.get("data").getAsString());
        assertTrue(flatRoot.has("@eClass"));
        JsonObject flatChild = flatRoot.getAsJsonArray("children").get(0).getAsJsonObject();
        assertEquals("child-id", flatChild.get("@id").getAsString());
        assertFalse(flatChild.has("data") && flatChild.get("data").isJsonObject());
        this.assertLoaded(ePackage, node, flat, resourceOptions);

        JsonObject mixed = JsonParser.parseString(flat.toString()).getAsJsonObject();
        JsonObject mixedRoot = mixed.getAsJsonArray("content").get(0).getAsJsonObject();
        JsonObject mixedChild = mixedRoot.getAsJsonArray("children").get(0).getAsJsonObject();
        JsonObject legacyChild = new JsonObject();
        legacyChild.add("id", mixedChild.remove("@id"));
        legacyChild.add("eClass", mixedChild.remove("@eClass"));
        legacyChild.add("data", mixedChild);
        mixedRoot.getAsJsonArray("children").set(0, legacyChild);
        this.assertLoaded(ePackage, node, mixed, resourceOptions);
    }

    @Test
    public void flatLayoutLoadsEcoreSubPackages() throws IOException {
        EPackage parent = EcoreFactory.eINSTANCE.createEPackage();
        parent.setName("parent");
        parent.setNsPrefix("parent");
        parent.setNsURI("http://example.org/parent");
        EPackage child = EcoreFactory.eINSTANCE.createEPackage();
        child.setName("child");
        child.setNsPrefix("child");
        child.setNsURI("http://example.org/child");
        parent.getESubpackages().add(child);

        JsonResource source = this.createResource(EcorePackage.eINSTANCE, Map.of());
        source.getContents().add(parent);
        JsonObject flat = this.save(source, Map.of(JsonResource.OPTION_SAVE_FLAT_EOBJECTS, Boolean.TRUE));
        JsonObject subPackage = flat.getAsJsonArray("content").get(0).getAsJsonObject().getAsJsonArray("eSubpackages").get(0).getAsJsonObject();
        assertEquals("child", subPackage.get("name").getAsString());
        assertFalse(subPackage.has("data"));

        JsonResource loaded = this.createResource(EcorePackage.eINSTANCE, Map.of());
        loaded.load(new ByteArrayInputStream(flat.toString().getBytes(StandardCharsets.UTF_8)), Map.of());
        EPackage loadedParent = (EPackage) loaded.getContents().get(0);
        assertEquals("child", loadedParent.getESubpackages().get(0).getName());
    }

    private JsonResource createResource(EPackage ePackage, Map<Object, Object> options) {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
        JsonResource resource = new JsonResourceImpl(URI.createURI("memory:/test.json"), options);
        resourceSet.getResources().add(resource);
        return resource;
    }

    private JsonObject save(JsonResource resource, Map<Object, Object> options) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resource.save(output, options);
        return JsonParser.parseString(output.toString(StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private void assertLoaded(EPackage ePackage, EClass node, JsonObject json, Map<Object, Object> options) throws IOException {
        JsonResource loaded = this.createResource(ePackage, options);
        loaded.load(new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8)), Map.of());
        assertEquals(1, loaded.getContents().size());
        EObject root = loaded.getContents().get(0);
        assertEquals("feature-id", root.eGet(node.getEStructuralFeature("id")));
        assertEquals("feature-class", root.eGet(node.getEStructuralFeature("eClass")));
        assertEquals("feature-data", root.eGet(node.getEStructuralFeature("data")));
        assertEquals(root, loaded.getEObject("root-id"));
        @SuppressWarnings("unchecked")
        java.util.List<EObject> children = (java.util.List<EObject>) root.eGet(node.getEStructuralFeature("children"));
        assertEquals(1, children.size());
        assertEquals("child-data", children.get(0).eGet(node.getEStructuralFeature("data")));
        assertEquals(children.get(0), loaded.getEObject("child-id"));
    }

    private static final class TestIDManager implements IDManager {
        private final Map<EObject, String> ids = new IdentityHashMap<>();

        @Override
        public String getOrCreateId(EObject eObject) {
            return this.ids.computeIfAbsent(eObject, object -> Integer.toString(this.ids.size()));
        }

        @Override
        public Optional<String> findId(EObject eObject) {
            return Optional.ofNullable(this.ids.get(eObject));
        }

        @Override
        public void clearId(EObject eObject) {
            this.ids.remove(eObject);
        }

        @Override
        public String setId(EObject eObject, String id) {
            return this.ids.put(eObject, id);
        }
    }
}
