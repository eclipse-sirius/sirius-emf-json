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

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.emfjson.resource.IDManager;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.eclipse.sirius.emfjson.utils.GsonEObjectSerializer;
import org.eclipse.sirius.emfjson.utils.JsonHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Regression coverage for linear reference-list classification and whole-list encoding.
 */
public class ReferenceListClassificationTests {

    @Test
    public void resourceChecksGrowLinearlyWithCustomHelper() {
        int checksAt64 = 0;
        for (int size : new int[] { 0, 1, 64, 256 }) {
            var resource = this.resource("local"); //$NON-NLS-1$
            var type = this.type();
            var source = this.object(type, resource, "source"); //$NON-NLS-1$
            var resourceChecks = new AtomicInteger();
            for (int index = 0; index < size; index++) {
                var target = new DynamicEObjectImpl(type) {
                    @Override
                    public Resource eResource() {
                        resourceChecks.incrementAndGet();
                        return super.eResource();
                    }
                };
                target.eSet(type.getEStructuralFeature("id"), "target" + index); //$NON-NLS-1$ //$NON-NLS-2$
                resource.getContents().add(target);
                this.references(source).add(target);
            }
            var helper = new JsonHelper(resource) {
                @Override
                public String getIDREF(EObject object) {
                    return "custom-" + super.getIDREF(object); //$NON-NLS-1$
                }
            };
            resourceChecks.set(0);
            var values = this.serialize(resource, source, Map.of(JsonResource.OPTION_CUSTOM_HELPER, helper,
                    JsonResource.OPTION_SAVE_UNSETTED_FEATURES, true));
            Assert.assertEquals(size, values.size());
            for (int index = 0; index < size; index++) {
                Assert.assertEquals("custom-target" + index, values.get(index).getAsString()); //$NON-NLS-1$
            }
            if (size == 64) {
                checksAt64 = resourceChecks.get();
            } else if (size == 256) {
                Assert.assertTrue("Resource checks must grow near-linearly: " + resourceChecks.get(), //$NON-NLS-1$
                        resourceChecks.get() <= 8 * Math.max(1, checksAt64));
            }
        }
    }

    @Test
    public void mixedListsPreserveCallbacksAndRefreshBetweenSaves() {
        var resource = this.resource("local"); //$NON-NLS-1$
        var external = this.resource("external"); //$NON-NLS-1$
        var type = this.type();
        var source = this.object(type, resource, "source"); //$NON-NLS-1$
        var first = this.object(type, resource, "first"); //$NON-NLS-1$
        var second = this.object(type, external, "second"); //$NON-NLS-1$
        this.references(source).addAll(List.of(first, second));
        var callbackTargets = new ArrayList<EObject>();
        var callbackURIs = new ArrayList<String>();
        var listener = new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onCrossReferenceURICreated(EObject object, EReference reference, String uri) {
                callbackTargets.add(object);
                callbackURIs.add(uri);
            }
        };
        var options = Map.of(JsonResource.OPTION_SERIALIZATION_LISTENER, listener);
        var values = this.serialize(resource, source, options);
        Assert.assertEquals("#first", values.get(0).getAsString()); //$NON-NLS-1$
        Assert.assertEquals("external#second", values.get(1).getAsString()); //$NON-NLS-1$
        Assert.assertTrue(callbackTargets.contains(second));
        Assert.assertTrue(callbackURIs.contains("external#second")); //$NON-NLS-1$

        resource.getContents().add(second);
        callbackTargets.clear();
        callbackURIs.clear();
        values = this.serialize(resource, source, options);
        Assert.assertEquals("first", values.get(0).getAsString()); //$NON-NLS-1$
        Assert.assertEquals("second", values.get(1).getAsString()); //$NON-NLS-1$
        Assert.assertTrue(callbackTargets.isEmpty());
        Assert.assertTrue(callbackURIs.isEmpty());
    }

    @Test
    public void proxiesKeepWholeListInCrossDocumentForm() {
        var resource = this.resource("local"); //$NON-NLS-1$
        var type = this.type();
        var source = this.object(type, resource, "source"); //$NON-NLS-1$
        var local = this.object(type, resource, "local"); //$NON-NLS-1$
        var proxy = new DynamicEObjectImpl(type);
        ((InternalEObject) proxy).eSetProxyURI(URI.createURI("sirius:///external#proxy")); //$NON-NLS-1$
        this.references(source).addAll(List.of(local, proxy));
        var values = this.serialize(resource, source, Map.of());
        Assert.assertEquals("#local", values.get(0).getAsString()); //$NON-NLS-1$
        Assert.assertEquals("external#proxy", values.get(1).getAsString()); //$NON-NLS-1$
    }

    @Test
    public void customIDManagerProvidesReferenceFragments() {
        var manager = new IDManager() {
            @Override
            public String getOrCreateId(EObject object) {
                return "custom-" + EcoreUtil.getID(object); //$NON-NLS-1$
            }

            @Override
            public Optional<String> findId(EObject object) {
                return Optional.of(this.getOrCreateId(object));
            }

            @Override
            public String setId(EObject object, String id) {
                return null;
            }

            @Override
            public void clearId(EObject object) {
                // IDs are derived from the model's ID attribute.
            }
        };
        var resource = new JsonResourceImpl(URI.createURI("sirius:///local"), Map.of(JsonResource.OPTION_ID_MANAGER, manager)); //$NON-NLS-1$
        var type = this.type();
        var source = this.object(type, resource, "source"); //$NON-NLS-1$
        var first = this.object(type, resource, "first"); //$NON-NLS-1$
        var second = this.object(type, resource, "second"); //$NON-NLS-1$
        this.references(source).addAll(List.of(first, second));
        var values = this.serialize(resource, source, Map.of());
        Assert.assertEquals("custom-first", values.get(0).getAsString()); //$NON-NLS-1$
        Assert.assertEquals("custom-second", values.get(1).getAsString()); //$NON-NLS-1$
    }

    private JsonResourceImpl resource(String name) {
        return new JsonResourceImpl(URI.createURI("sirius:///" + name), Map.of()); //$NON-NLS-1$
    }

    private EClass type() {
        var ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("test"); //$NON-NLS-1$
        ePackage.setNsPrefix("test"); //$NON-NLS-1$
        ePackage.setNsURI("urn:test:classification"); //$NON-NLS-1$
        var type = EcoreFactory.eINSTANCE.createEClass();
        type.setName("Node"); //$NON-NLS-1$
        ePackage.getEClassifiers().add(type);
        var id = EcoreFactory.eINSTANCE.createEAttribute();
        id.setName("id"); //$NON-NLS-1$
        id.setEType(EcorePackage.Literals.ESTRING);
        id.setID(true);
        type.getEStructuralFeatures().add(id);
        var reference = EcoreFactory.eINSTANCE.createEReference();
        reference.setName("references"); //$NON-NLS-1$
        reference.setEType(type);
        reference.setUpperBound(-1);
        type.getEStructuralFeatures().add(reference);
        return type;
    }

    private EObject object(EClass type, Resource resource, String id) {
        var object = new DynamicEObjectImpl(type);
        object.eSet(type.getEStructuralFeature("id"), id); //$NON-NLS-1$
        resource.getContents().add(object);
        return object;
    }

    @SuppressWarnings("unchecked")
    private EList<EObject> references(EObject object) {
        return (EList<EObject>) object.eGet(object.eClass().getEStructuralFeature("references")); //$NON-NLS-1$
    }

    private JsonArray serialize(JsonResource resource, EObject source, Map<?, ?> options) {
        var result = new GsonEObjectSerializer(resource, options).serialize(List.of(source), List.class, null);
        return result.getAsJsonObject().getAsJsonArray("content").get(0).getAsJsonObject().getAsJsonObject("data").getAsJsonArray("references"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
