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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.eclipse.sirius.emfjson.utils.GsonEObjectSerializer;
import org.eclipse.sirius.emfjson.utils.JsonHelper;
import org.junit.Test;

/**
 * Checks reference encoding and custom hooks with stable reference lists during each save.
 * These tests do not establish support for hooks that mutate reference lists or target resource/proxy membership
 * while the list is being serialized.
 */
public class SerializeManyReferencesTests {

    @Test
    public void localReferencesUseBareFragmentsInOrderEvenWithAListener() {
        for (int referenceCount : new int[] { 20, 200 }) {
            var resource = this.createResource("local"); //$NON-NLS-1$
            var nodeEClass = this.createNodeEClass();
            var sourceObject = new DynamicEObjectImpl(nodeEClass);
            resource.getContents().add(sourceObject);
            for (int referenceIndex = 0; referenceIndex < referenceCount; referenceIndex++) {
                var target = this.createObjectWithId(nodeEClass, resource, "target" + referenceIndex); //$NON-NLS-1$
                this.getReferenceTargets(sourceObject).add(target);
            }
            // Sirius Web installs a listener even when its cross-reference callback is empty.
            var serializedReferences = this.serializeReferenceValues(resource, sourceObject,
                    Map.of(JsonResource.OPTION_SERIALIZATION_LISTENER, new JsonResource.ISerializationListener.NoOp()));

            assertThat(serializedReferences.size()).isEqualTo(referenceCount);
            for (int referenceIndex = 0; referenceIndex < referenceCount; referenceIndex++) {
                assertThat(serializedReferences.get(referenceIndex).getAsString()).isEqualTo("target" + referenceIndex); //$NON-NLS-1$
            }
        }
    }

    @Test
    public void customResourceFragmentsAreUsedForLocalReferences() {
        var nodeEClass = this.createNodeEClass();
        var resource = new JsonResourceImpl(URI.createURI("sirius:///local"), Map.of()) { //$NON-NLS-1$
            @Override
            public String getURIFragment(EObject object) {
                return "custom-" + super.getURIFragment(object); //$NON-NLS-1$
            }
        };
        var sourceObject = this.createObjectWithId(nodeEClass, resource, "source"); //$NON-NLS-1$
        var firstTarget = this.createObjectWithId(nodeEClass, resource, "first"); //$NON-NLS-1$
        var secondTarget = this.createObjectWithId(nodeEClass, resource, "second"); //$NON-NLS-1$
        this.getReferenceTargets(sourceObject).addAll(List.of(firstTarget, secondTarget));

        var serializedReferences = this.serializeReferenceValues(resource, sourceObject, Map.of());
        assertThat(serializedReferences.asList()).extracting(JsonElement::getAsString)
                .containsExactly("custom-first", "custom-second"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void mixedLocalAndExternalReferencesUseUrisAndNotifyListenersInReferenceOrder() {
        var resource = this.createResource("local"); //$NON-NLS-1$
        var externalResource = this.createResource("external"); //$NON-NLS-1$
        var nodeEClass = this.createNodeEClass();
        var sourceObject = this.createObjectWithId(nodeEClass, resource, "source"); //$NON-NLS-1$
        var localTarget = this.createObjectWithId(nodeEClass, resource, "first"); //$NON-NLS-1$
        var externalTarget = this.createObjectWithId(nodeEClass, externalResource, "second"); //$NON-NLS-1$
        this.getReferenceTargets(sourceObject).addAll(List.of(localTarget, externalTarget));
        var callbackTargets = new ArrayList<EObject>();
        var callbackURIs = new ArrayList<String>();
        var listener = new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onCrossReferenceURICreated(EObject object, EReference reference, String uri) {
                callbackTargets.add(object);
                callbackURIs.add(uri);
            }
        };

        var serializedReferences = this.serializeReferenceValues(resource, sourceObject, Map.of(JsonResource.OPTION_SERIALIZATION_LISTENER, listener));
        assertThat(serializedReferences.asList()).extracting(JsonElement::getAsString)
                .containsExactly("#first", "external#second"); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(callbackTargets).containsExactly(localTarget, externalTarget);
        assertThat(callbackURIs).containsExactly("#first", "external#second"); //$NON-NLS-1$ //$NON-NLS-2$

    }

    @Test
    public void movingExternalTargetIntoSourceResourceSwitchesToBareFragmentsOnNextSave() {
        var resource = this.createResource("local"); //$NON-NLS-1$
        var externalResource = this.createResource("external"); //$NON-NLS-1$
        var nodeEClass = this.createNodeEClass();
        var sourceObject = this.createObjectWithId(nodeEClass, resource, "source"); //$NON-NLS-1$
        var localTarget = this.createObjectWithId(nodeEClass, resource, "first"); //$NON-NLS-1$
        var externalTarget = this.createObjectWithId(nodeEClass, externalResource, "second"); //$NON-NLS-1$
        this.getReferenceTargets(sourceObject).addAll(List.of(localTarget, externalTarget));
        var callbackURIs = new ArrayList<String>();
        var listener = new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onCrossReferenceURICreated(EObject object, EReference reference, String uri) {
                callbackURIs.add(uri);
            }
        };
        var options = Map.of(JsonResource.OPTION_SERIALIZATION_LISTENER, listener);

        var referencesBeforeMove = this.serializeReferenceValues(resource, sourceObject, options);
        assertThat(referencesBeforeMove.asList()).extracting(JsonElement::getAsString)
                .containsExactly("#first", "external#second"); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(callbackURIs).containsExactly("#first", "external#second"); //$NON-NLS-1$ //$NON-NLS-2$

        resource.getContents().add(externalTarget);
        callbackURIs.clear();

        var referencesAfterMove = this.serializeReferenceValues(resource, sourceObject, options);
        assertThat(referencesAfterMove.asList()).extracting(JsonElement::getAsString)
                .containsExactly("first", "second"); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(callbackURIs).isEmpty();
    }

    @Test
    public void customHelperIdReferencesAreUsedForEveryLocalTarget() {
        var resource = this.createResource("local"); //$NON-NLS-1$
        var nodeEClass = this.createNodeEClass();
        var sourceObject = this.createObjectWithId(nodeEClass, resource, "source"); //$NON-NLS-1$
        var firstTarget = this.createObjectWithId(nodeEClass, resource, "first"); //$NON-NLS-1$
        var secondTarget = this.createObjectWithId(nodeEClass, resource, "second"); //$NON-NLS-1$
        this.getReferenceTargets(sourceObject).addAll(List.of(firstTarget, secondTarget));
        var referenceHelper = new JsonHelper(resource) {
            @Override
            public String getIDREF(EObject object) {
                return "custom-" + super.getIDREF(object); //$NON-NLS-1$
            }
        };

        var serializedReferences = this.serializeReferenceValues(resource, sourceObject, Map.of(JsonResource.OPTION_CUSTOM_HELPER, referenceHelper));
        assertThat(serializedReferences.asList()).extracting(JsonElement::getAsString)
                .containsExactly("custom-first", "custom-second"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void proxyReferenceKeepsWholeListInCrossDocumentForm() {
        var resource = this.createResource("local"); //$NON-NLS-1$
        var nodeEClass = this.createNodeEClass();
        var sourceObject = this.createObjectWithId(nodeEClass, resource, "source"); //$NON-NLS-1$
        var localTarget = this.createObjectWithId(nodeEClass, resource, "local"); //$NON-NLS-1$
        var proxy = new DynamicEObjectImpl(nodeEClass);
        proxy.eSetProxyURI(URI.createURI("sirius:///external#proxy")); //$NON-NLS-1$
        this.getReferenceTargets(sourceObject).addAll(List.of(localTarget, proxy));

        var serializedReferences = this.serializeReferenceValues(resource, sourceObject, Map.of());
        assertThat(serializedReferences.asList()).extracting(JsonElement::getAsString)
                .containsExactly("#local", "external#proxy"); //$NON-NLS-1$ //$NON-NLS-2$
        assertThat(proxy.eIsProxy()).isTrue();
    }

    @Test
    public void saveUnsetFeaturesOptionIncludesAnEmptyReferenceArray() {
        var resource = this.createResource("local"); //$NON-NLS-1$
        var sourceObject = this.createObjectWithId(this.createNodeEClass(), resource, "source"); //$NON-NLS-1$

        var serializedReferences = this.serializeReferenceValues(resource, sourceObject, Map.of(JsonResource.OPTION_SAVE_UNSETTED_FEATURES, true));
        assertThat(serializedReferences.size()).isZero();
    }

    private JsonResourceImpl createResource(String name) {
        return new JsonResourceImpl(URI.createURI("sirius:///" + name), Map.of()); //$NON-NLS-1$
    }

    private EClass createNodeEClass() {
        var ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("test"); //$NON-NLS-1$
        ePackage.setNsPrefix("test"); //$NON-NLS-1$
        ePackage.setNsURI("urn:test:references"); //$NON-NLS-1$
        var nodeEClass = EcoreFactory.eINSTANCE.createEClass();
        nodeEClass.setName("Node"); //$NON-NLS-1$
        ePackage.getEClassifiers().add(nodeEClass);
        var id = EcoreFactory.eINSTANCE.createEAttribute();
        id.setName("id"); //$NON-NLS-1$
        id.setEType(EcorePackage.Literals.ESTRING);
        id.setID(true);
        nodeEClass.getEStructuralFeatures().add(id);
        var reference = EcoreFactory.eINSTANCE.createEReference();
        reference.setName("references"); //$NON-NLS-1$
        reference.setEType(nodeEClass);
        reference.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
        nodeEClass.getEStructuralFeatures().add(reference);
        return nodeEClass;
    }

    /**
     * Creates an object with an ID attribute and attaches it as a root of the resource.
     */
    private EObject createObjectWithId(EClass nodeEClass, Resource resource, String id) {
        var object = new DynamicEObjectImpl(nodeEClass);
        object.eSet(nodeEClass.getEStructuralFeature("id"), id); //$NON-NLS-1$
        resource.getContents().add(object);
        return object;
    }

    /**
     * Returns the live reference list; additions modify the source object's model.
     */
    @SuppressWarnings("unchecked")
    private EList<EObject> getReferenceTargets(EObject object) {
        return (EList<EObject>) object.eGet(object.eClass().getEStructuralFeature("references")); //$NON-NLS-1$
    }

    private JsonArray serializeReferenceValues(JsonResource resource, EObject sourceObject, Map<?, ?> options) {
        JsonElement serializedResource = new GsonEObjectSerializer(resource, options).serialize(List.of(sourceObject), List.class, null);
        var serializedSource = serializedResource.getAsJsonObject().getAsJsonArray("content").get(0).getAsJsonObject(); //$NON-NLS-1$
        return serializedSource.getAsJsonObject("data").getAsJsonArray("references"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
