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
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Arrays;
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
 * Verifies that cached feature selection honors save options, custom metadata, and callback mutations.
 */
public class FrozenFeaturesSerializationTests {
    @Test
    public void customEClassAvoidsIndexedFeatureAccessWhetherFrozenOrMutable() {
        for (boolean freezeMetamodel : new boolean[] {false, true}) {
            var indexedFeatureReadCount = new AtomicInteger();
            EClass customEClass = this.createEClassWithCountedIndexedReads(indexedFeatureReadCount);
            EClass standardEClass = this.createNodeEClass();
            customEClass.setName("CustomNode");
            standardEClass.getEPackage().getEClassifiers().add(customEClass);
            customEClass.getEStructuralFeatures().addAll(List.copyOf(standardEClass.getEStructuralFeatures()));
            EObject object = this.createObjectWithAttributeNames(customEClass);
            if (freezeMetamodel) {
                EcoreUtil.freeze(customEClass.getEPackage());
            }
            indexedFeatureReadCount.set(0);

            JsonObject serializedObject = this.rootData(this.serializeObjects(Map.of(), object), 0);

            assertThat(serializedObject.keySet()).containsExactly("persistentValue");
            assertThat(serializedObject.asMap()).containsEntry("persistentValue", new JsonPrimitive("persistentValue"));
            assertThat(indexedFeatureReadCount.get()).as("indexed reads with frozen metamodel=%s", freezeMetamodel).isZero();
        }
    }

    @Test
    public void customAttributeTypeIsReadForEachObjectEvenWhenItsEClassIsFrozen() {
        var attributeTypeReadCount = new AtomicInteger();
        var nodeEClass = this.createNodeEClass();
        var customAttribute = new EAttributeImpl() {
            @Override
            public EClassifier getEType() {
                attributeTypeReadCount.incrementAndGet();
                return super.getEType();
            }
        };
        customAttribute.setName("custom");
        customAttribute.setEType(EcorePackage.Literals.ESTRING);
        nodeEClass.getEStructuralFeatures().add(customAttribute);
        EObject firstObject = this.createObjectWithAttributeNames(nodeEClass);
        EObject secondObject = this.createObjectWithAttributeNames(nodeEClass);
        EcoreUtil.freeze(nodeEClass.getEPackage());
        attributeTypeReadCount.set(0);
        var typeReadCountsAfterEachObject = new ArrayList<Integer>();
        var listener = new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onObjectSerialized(EObject object, JsonElement json) {
                typeReadCountsAfterEachObject.add(attributeTypeReadCount.get());
            }
        };

        JsonObject serializedResource = this.serializeObjects(
                Map.of(JsonResource.OPTION_SERIALIZATION_LISTENER, listener), firstObject, secondObject);

        assertThat(this.rootData(serializedResource, 0).asMap()).containsEntry("custom", new JsonPrimitive("custom"));
        assertThat(this.rootData(serializedResource, 1).asMap()).containsEntry("custom", new JsonPrimitive("custom"));
        assertThat(typeReadCountsAfterEachObject).as("cumulative custom getEType() calls after each object").containsExactly(1, 2);
    }

    @Test
    public void frozenSubclassObservesInheritedTransientFlagChangesBetweenObjects() {
        var basePackage = EcoreFactory.eINSTANCE.createEPackage();
        basePackage.setName("base");
        basePackage.setNsPrefix("base");
        basePackage.setNsURI("urn:base");
        var baseEClass = EcoreFactory.eINSTANCE.createEClass();
        baseEClass.setName("Base");
        basePackage.getEClassifiers().add(baseEClass);
        var inheritedAttribute = EcoreFactory.eINSTANCE.createEAttribute();
        inheritedAttribute.setName("inherited");
        inheritedAttribute.setEType(EcorePackage.Literals.ESTRING);
        inheritedAttribute.setTransient(true);
        baseEClass.getEStructuralFeatures().add(inheritedAttribute);

        EClass nodeEClass = this.createNodeEClass();
        nodeEClass.getESuperTypes().add(baseEClass);
        EObject firstObject = this.createObjectWithAttributeNames(nodeEClass);
        EObject secondObject = this.createObjectWithAttributeNames(nodeEClass);
        firstObject.eSet(inheritedAttribute, "first");
        secondObject.eSet(inheritedAttribute, "second");
        EcoreUtil.freeze(nodeEClass.getEPackage());
        assertThat(((EClassImpl) nodeEClass).isFrozen()).isTrue();
        assertThat(((EClassImpl) baseEClass).isFrozen()).isFalse();
        var options = Map.of(JsonResource.OPTION_SERIALIZATION_LISTENER, new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onObjectSerialized(EObject object, JsonElement json) {
                if (object == firstObject) {
                    inheritedAttribute.setTransient(false);
                }
            }
        });

        JsonObject serializedResource = this.serializeObjects(options, firstObject, secondObject);

        assertThat(this.rootData(serializedResource, 0).keySet()).doesNotContain("inherited");
        assertThat(this.rootData(serializedResource, 1).asMap()).containsEntry("inherited", new JsonPrimitive("second"));
    }

    @Test
    public void defaultOptionsOmitTransientAndDerivedFeaturesBeforeAndAfterFreezing() {
        this.assertFeatureSelectionBeforeAndAfterFreezing(Map.of(), "persistentValue");
    }

    @Test
    public void saveTransientOptionIncludesTransientFeatureBeforeAndAfterFreezing() {
        this.assertFeatureSelectionBeforeAndAfterFreezing(
                Map.of(JsonResource.OPTION_SAVE_TRANSIENT_FEATURES, true), "transientValue", "persistentValue");
    }

    @Test
    public void saveDerivedOptionIncludesDerivedFeatureBeforeAndAfterFreezing() {
        this.assertFeatureSelectionBeforeAndAfterFreezing(
                Map.of(JsonResource.OPTION_SAVE_DERIVED_FEATURES, true), "persistentValue", "derivedValue");
    }

    @Test
    public void customFilterCanIncludeTransientAndDerivedFeaturesInDeclarationOrder() {
        EClass nodeEClass = this.createNodeEClass();
        EcoreUtil.freeze(nodeEClass.getEPackage());
        var filteredFeatureNames = new ArrayList<String>();
        var filter = new JsonResource.EStructuralFeaturesFilter() {
            @Override
            public boolean shouldSave(EObject object, EStructuralFeature feature) {
                filteredFeatureNames.add(feature.getName());
                return true;
            }

            @Override
            public boolean shouldLoad(EObject object, EStructuralFeature feature) {
                return true;
            }
        };
        JsonObject serializedObject = this.rootData(this.serializeObjects(
                Map.of(JsonResource.OPTION_ESTRUCTURAL_FEATURES_FILTER, filter), this.createObjectWithAttributeNames(nodeEClass)), 0);

        assertThat(filteredFeatureNames).containsExactly("transientValue", "persistentValue", "derivedValue");
        assertThat(serializedObject.keySet()).containsExactly("transientValue", "persistentValue", "derivedValue");
        assertThat(serializedObject.asMap())
                .containsEntry("transientValue", new JsonPrimitive("transientValue"))
                .containsEntry("derivedValue", new JsonPrimitive("derivedValue"));
    }

    @Test
    public void comparatorReceivesTransientAndDerivedFeaturesEvenWhenTheyAreNotSaved() {
        EClass nodeEClass = this.createNodeEClass();
        EObject object = this.createObjectWithAttributeNames(nodeEClass);
        var comparisonArguments = new ArrayList<String>();
        Comparator<EStructuralFeature> comparator = (left, right) -> {
            comparisonArguments.add(left.getName());
            comparisonArguments.add(right.getName());
            return right.getName().compareTo(left.getName());
        };
        var options = Map.of(JsonResource.OPTION_SAVE_FEATURES_ORDER_COMPARATOR, comparator);
        String mutableMetamodelJson = this.serializeObjects(options, object).toString();
        var expectedComparisonArguments = List.copyOf(comparisonArguments);
        comparisonArguments.clear();
        EcoreUtil.freeze(nodeEClass.getEPackage());

        JsonObject frozenSerialization = this.serializeObjects(options, object);

        assertThat(this.rootData(frozenSerialization, 0).keySet()).containsExactly("persistentValue");
        assertThat(frozenSerialization.toString()).isEqualTo(mutableMetamodelJson);
        assertThat(comparisonArguments).containsExactlyElementsOf(expectedComparisonArguments)
                .contains("transientValue", "persistentValue", "derivedValue");
    }

    @Test
    public void clearingTransientFlagAfterFirstObjectIncludesTheFeatureInTheSecondObject() {
        EClass nodeEClass = this.createNodeEClass();
        EObject firstObject = this.createObjectWithAttributeNames(nodeEClass);
        EObject secondObject = this.createObjectWithAttributeNames(nodeEClass);
        var options = new HashMap<String, Object>();
        options.put(JsonResource.OPTION_SERIALIZATION_LISTENER, new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onObjectSerialized(EObject object, JsonElement json) {
                if (object == firstObject) {
                    nodeEClass.getEStructuralFeature("transientValue").setTransient(false);
                }
            }
        });

        JsonObject serializedResource = this.serializeObjects(options, firstObject, secondObject);

        assertThat(this.rootData(serializedResource, 0).keySet()).doesNotContain("transientValue");
        assertThat(this.rootData(serializedResource, 1).asMap()).containsEntry("transientValue", new JsonPrimitive("transientValue"));
    }

    @Test
    public void childCallbackEnablesTransientFeatureForParentButNotTheAlreadySerializedChild() {
        EClass nodeEClass = this.createNodeEClass();
        var childFeature = EcoreFactory.eINSTANCE.createEReference();
        childFeature.setName("child");
        childFeature.setEType(nodeEClass);
        childFeature.setContainment(true);
        // Serialize the child before the parent's transient feature so its callback can change the save option.
        nodeEClass.getEStructuralFeatures().add(0, childFeature);
        EcoreUtil.freeze(nodeEClass.getEPackage());
        EObject parent = this.createObjectWithAttributeNames(nodeEClass);
        EObject child = this.createObjectWithAttributeNames(nodeEClass);
        parent.eSet(childFeature, child);
        parent.eSet(nodeEClass.getEStructuralFeature("transientValue"), "parent value");
        child.eSet(nodeEClass.getEStructuralFeature("transientValue"), "child value");
        var options = new HashMap<String, Object>();
        options.put(JsonResource.OPTION_SERIALIZATION_LISTENER, new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onObjectSerialized(EObject object, JsonElement json) {
                if (object == child) {
                    options.put(JsonResource.OPTION_SAVE_TRANSIENT_FEATURES, Boolean.TRUE);
                }
            }
        });

        JsonObject parentData = this.rootData(this.serializeObjects(options, parent), 0);
        JsonObject childData = parentData.getAsJsonObject("child").getAsJsonObject("data");

        assertThat(childData.keySet()).doesNotContain("transientValue");
        assertThat(parentData.asMap()).containsEntry("transientValue", new JsonPrimitive("parent value"));
    }

    private void assertFeatureSelectionBeforeAndAfterFreezing(Map<?, ?> options, String... expectedFeatures) {
        EClass nodeEClass = this.createNodeEClass();
        EObject object = this.createObjectWithAttributeNames(nodeEClass);
        JsonObject beforeFreezing = this.serializeObjects(options, object);

        EcoreUtil.freeze(nodeEClass.getEPackage());
        JsonObject afterFreezing = this.serializeObjects(options, object);

        assertThat(this.rootData(beforeFreezing, 0).keySet()).as("features before freezing").containsExactly(expectedFeatures);
        assertThat(this.rootData(afterFreezing, 0).keySet()).as("features after freezing").containsExactly(expectedFeatures);
        for (String feature : expectedFeatures) {
            assertThat(this.rootData(afterFreezing, 0).asMap()).containsEntry(feature, new JsonPrimitive(feature));
        }
        assertThat(afterFreezing.toString()).as("JSON including property order").isEqualTo(beforeFreezing.toString());
    }

    private JsonObject rootData(JsonObject serializedResource, int rootIndex) {
        return serializedResource.getAsJsonArray("content").get(rootIndex).getAsJsonObject().getAsJsonObject("data");
    }

    private EClass createEClassWithCountedIndexedReads(AtomicInteger indexedFeatureReadCount) {
        return new EClassImpl() {
            @Override
            public EList<EStructuralFeature> getEAllStructuralFeatures() {
                EList<EStructuralFeature> originalFeatures = super.getEAllStructuralFeatures();
                return new BasicEList<EStructuralFeature>(originalFeatures) {
                    @Override
                    public EStructuralFeature get(int index) {
                        indexedFeatureReadCount.incrementAndGet();
                        return super.get(index);
                    }

                    @Override
                    public Iterator<EStructuralFeature> iterator() {
                        // Iteration must bypass get(int) so the test distinguishes the two access paths.
                        return originalFeatures.iterator();
                    }
                };
            }
        };
    }

    private EClass createNodeEClass() {
        var ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("model");
        ePackage.setNsPrefix("model");
        ePackage.setNsURI("urn:model");
        var nodeEClass = EcoreFactory.eINSTANCE.createEClass();
        nodeEClass.setName("Node");
        ePackage.getEClassifiers().add(nodeEClass);
        for (String name : List.of("transientValue", "persistentValue", "derivedValue")) {
            var attribute = EcoreFactory.eINSTANCE.createEAttribute();
            attribute.setName(name);
            attribute.setEType(EcorePackage.Literals.ESTRING);
            attribute.setTransient(name.equals("transientValue"));
            attribute.setDerived(name.equals("derivedValue"));
            nodeEClass.getEStructuralFeatures().add(attribute);
        }
        return nodeEClass;
    }

    private EObject createObjectWithAttributeNames(EClass nodeEClass) {
        EObject object = EcoreUtil.create(nodeEClass);
        for (var attribute : nodeEClass.getEAllAttributes()) {
            object.eSet(attribute, attribute.getName());
        }
        return object;
    }

    private JsonObject serializeObjects(Map<?, ?> options, EObject... rootObjects) {
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        resource.getContents().addAll(Arrays.asList(rootObjects));
        // Direct serializer use keeps the same options map, so callbacks can change it during serialization.
        return new GsonEObjectSerializer(resource, options).serialize(resource.getContents(), null, null).getAsJsonObject();
    }
}
