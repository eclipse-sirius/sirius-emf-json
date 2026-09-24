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

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.eclipse.sirius.emfjson.utils.GsonEObjectSerializer;
import org.junit.Test;

/**
 * Checks feature selection for frozen and mutable metamodels and changing save options.
 */
public class FrozenFeaturesSerializationTests {
    @Test
    public void customMetadataUsesItsIteratorEvenWhenFrozen() {
        for (boolean frozen : new boolean[] {false, true}) {
            var indexedReads = new AtomicInteger();
            var type = new EClassImpl() {
                @Override
                public EList<EStructuralFeature> getEAllStructuralFeatures() {
                    EList<EStructuralFeature> original = super.getEAllStructuralFeatures();
                    return new BasicEList<EStructuralFeature>(original) {
                        @Override
                        public EStructuralFeature get(int index) {
                            indexedReads.incrementAndGet();
                            return super.get(index);
                        }

                        @Override
                        public Iterator<EStructuralFeature> iterator() {
                            return original.iterator();
                        }
                    };
                }
            };
            EClass standardType = this.createType();
            type.setName("CustomNode");
            standardType.getEPackage().getEClassifiers().add(type);
            type.getEStructuralFeatures().addAll(List.copyOf(standardType.getEStructuralFeatures()));
            EObject object = this.createObject(type);
            if (frozen) {
                EcoreUtil.freeze(type.getEPackage());
            }
            indexedReads.set(0);
            assertThat(this.serialize(object, Map.of())).contains("\"ordinary\":\"ordinary\"");
            assertThat(indexedReads.get()).isZero();
        }
    }

    @Test
    public void customFeaturesRemainObservableWhenFrozen() {
        var typeReads = new AtomicInteger();
        var type = this.createType();
        var customAttribute = new EAttributeImpl() {
            @Override
            public EClassifier getEType() {
                typeReads.incrementAndGet();
                return super.getEType();
            }
        };
        customAttribute.setName("custom");
        customAttribute.setEType(EcorePackage.Literals.ESTRING);
        type.getEStructuralFeatures().add(customAttribute);
        EObject first = this.createObject(type);
        EObject second = this.createObject(type);
        EcoreUtil.freeze(type.getEPackage());
        typeReads.set(0);
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        resource.getContents().addAll(List.of(first, second));
        String json = new GsonEObjectSerializer(resource, Map.of()).serialize(resource.getContents(), null, null).toString();
        assertThat(json).contains("\"custom\":\"custom\"");
        assertThat(typeReads.get()).isEqualTo(2);
    }

    @Test
    public void inheritedMutableFeaturesRemainObservableWhenSubclassIsFrozen() {
        var basePackage = EcoreFactory.eINSTANCE.createEPackage();
        basePackage.setName("base");
        basePackage.setNsPrefix("base");
        basePackage.setNsURI("urn:base");
        var baseType = EcoreFactory.eINSTANCE.createEClass();
        baseType.setName("Base");
        basePackage.getEClassifiers().add(baseType);
        var inheritedAttribute = EcoreFactory.eINSTANCE.createEAttribute();
        inheritedAttribute.setName("inherited");
        inheritedAttribute.setEType(EcorePackage.Literals.ESTRING);
        inheritedAttribute.setTransient(true);
        baseType.getEStructuralFeatures().add(inheritedAttribute);

        EClass type = this.createType();
        type.getESuperTypes().add(baseType);
        EObject first = this.createObject(type);
        EObject second = this.createObject(type);
        first.eSet(inheritedAttribute, "first");
        second.eSet(inheritedAttribute, "second");
        EcoreUtil.freeze(type.getEPackage());
        var options = Map.of(JsonResource.OPTION_SERIALIZATION_LISTENER, new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onObjectSerialized(EObject object, JsonElement json) {
                if (object == first) {
                    inheritedAttribute.setTransient(false);
                }
            }
        });
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        resource.getContents().addAll(List.of(first, second));
        var result = new GsonEObjectSerializer(resource, options).serialize(resource.getContents(), null, null).getAsJsonObject().getAsJsonArray("content");
        assertThat(result.get(0).toString()).doesNotContain("inherited");
        assertThat(result.get(1).toString()).contains("inherited", "second");
    }

    @Test
    public void frozenFeaturesPreserveExactOutputAndExplicitOptions() {
        for (String option : List.of("default", JsonResource.OPTION_SAVE_TRANSIENT_FEATURES, JsonResource.OPTION_SAVE_DERIVED_FEATURES)) {
            EClass type = this.createType();
            EObject object = this.createObject(type);
            var options = new HashMap<String, Object>();
            options.put(option, Boolean.TRUE);
            String expected = this.serialize(object, options);
            EcoreUtil.freeze(type.getEPackage());
            assertThat(this.serialize(object, options)).isEqualTo(expected);
        }
        EClass type = this.createType();
        EcoreUtil.freeze(type.getEPackage());
        assertThat(this.serialize(this.createObject(type), Map.of())).contains("\"ordinary\":\"ordinary\"").doesNotContain("\"temporary\"", "\"computed\"");
    }

    @Test
    public void filterCanForceTransientAndDerivedFeatures() {
        EClass type = this.createType();
        EcoreUtil.freeze(type.getEPackage());
        var seen = new ArrayList<String>();
        var filter = new JsonResource.EStructuralFeaturesFilter() {
            @Override
            public boolean shouldSave(EObject object, EStructuralFeature feature) {
                seen.add(feature.getName());
                return true;
            }

            @Override
            public boolean shouldLoad(EObject object, EStructuralFeature feature) {
                return true;
            }
        };
        String json = this.serialize(this.createObject(type), Map.of(JsonResource.OPTION_ESTRUCTURAL_FEATURES_FILTER, filter));
        assertThat(seen).containsExactly("temporary", "ordinary", "computed");
        assertThat(json).contains("\"temporary\":\"temporary\"", "\"computed\":\"computed\"");
    }

    @Test
    public void comparatorStillReceivesExcludedFeatures() {
        EClass type = this.createType();
        EObject object = this.createObject(type);
        var compared = new ArrayList<String>();
        Comparator<EStructuralFeature> comparator = (left, right) -> {
            compared.add(left.getName() + ":" + right.getName());
            return right.getName().compareTo(left.getName());
        };
        var options = Map.of(JsonResource.OPTION_SAVE_FEATURES_ORDER_COMPARATOR, comparator);
        String expected = this.serialize(object, options);
        var expectedCalls = List.copyOf(compared);
        compared.clear();
        EcoreUtil.freeze(type.getEPackage());
        assertThat(this.serialize(object, options)).isEqualTo(expected);
        assertThat(compared).containsExactlyElementsOf(expectedCalls).isNotEmpty();
    }

    @Test
    public void mutableMetadataChangesBetweenObjectsAreObserved() {
        EClass type = this.createType();
        EObject first = this.createObject(type);
        EObject second = this.createObject(type);
        var options = new HashMap<String, Object>();
        options.put(JsonResource.OPTION_SERIALIZATION_LISTENER, new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onObjectSerialized(EObject object, JsonElement json) {
                type.getEStructuralFeature("temporary").setTransient(false);
            }
        });
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        resource.getContents().addAll(List.of(first, second));
        var result = new GsonEObjectSerializer(resource, options).serialize(resource.getContents(), null, null).getAsJsonObject().getAsJsonArray("content");
        assertThat(result.get(0).toString()).doesNotContain("\"temporary\"");
        assertThat(result.get(1).toString()).contains("\"temporary\":\"temporary\"");
    }

    @Test
    public void childCallbackCanEnableTransientFeaturesForRemainingParentFeatures() {
        EClass type = this.createType();
        var childFeature = EcoreFactory.eINSTANCE.createEReference();
        childFeature.setName("child");
        childFeature.setEType(type);
        childFeature.setContainment(true);
        type.getEStructuralFeatures().add(0, childFeature);
        EcoreUtil.freeze(type.getEPackage());
        EObject parent = this.createObject(type);
        EObject child = this.createObject(type);
        parent.eSet(childFeature, child);
        var options = new HashMap<String, Object>();
        options.put(JsonResource.OPTION_SERIALIZATION_LISTENER, new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onObjectSerialized(EObject object, JsonElement json) {
                if (object == child) {
                    options.put(JsonResource.OPTION_SAVE_TRANSIENT_FEATURES, Boolean.TRUE);
                }
            }
        });
        String json = this.serialize(parent, options);
        assertThat(json).containsOnlyOnce("\"temporary\":\"temporary\"");
    }

    private EClass createType() {
        var ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("model");
        ePackage.setNsPrefix("model");
        ePackage.setNsURI("urn:model");
        var type = EcoreFactory.eINSTANCE.createEClass();
        type.setName("Node");
        ePackage.getEClassifiers().add(type);
        for (String name : List.of("temporary", "ordinary", "computed")) {
            var attribute = EcoreFactory.eINSTANCE.createEAttribute();
            attribute.setName(name);
            attribute.setEType(EcorePackage.Literals.ESTRING);
            attribute.setTransient(name.equals("temporary"));
            attribute.setDerived(name.equals("computed"));
            type.getEStructuralFeatures().add(attribute);
        }
        return type;
    }

    private EObject createObject(EClass type) {
        EObject object = EcoreUtil.create(type);
        for (var attribute : type.getEAllAttributes()) {
            object.eSet(attribute, attribute.getName());
        }
        return object;
    }

    private String serialize(EObject object, Map<?, ?> options) {
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        resource.getContents().add(object);
        return new GsonEObjectSerializer(resource, options).serialize(resource.getContents(), null, null).toString();
    }
}
