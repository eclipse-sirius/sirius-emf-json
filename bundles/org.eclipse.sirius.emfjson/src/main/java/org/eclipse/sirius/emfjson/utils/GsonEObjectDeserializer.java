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
package org.eclipse.sirius.emfjson.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResource.IEObjectHandler;
import org.eclipse.sirius.emfjson.resource.JsonResource.URIHandler;
import org.eclipse.sirius.emfjson.resource.PackageNotFoundException;

/**
 * The Gson deserializer is responsible for the deserialization of EObjects.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class GsonEObjectDeserializer implements JsonDeserializer<List<EObject>> {

    /**
     * The JsonHelper.
     */
    private JsonHelper helper;

    /**
     * a resource set.
     */
    private ResourceSet resourceSet;

    /**
     * The resource
     */
    private JsonResource resource;

    /**
     * The deserialization options.
     */
    private Map<?, ?> options;

    /**
     * Data Structure that associate an NsURI to a prefix.
     */
    private Map<String, String> prefixToNsURi = new HashMap<String, String>();

    /**
     * The list of All root elements.
     */
    private List<EObject> rootElements = new ArrayList<EObject>();

    /**
     * The list of Reference to object that are not deserialized yet.
     */
    private List<SingleReference> forwardSingleReferences;

    /**
     * The support of extended meta data.
     */
    private ExtendedMetaData extendedMetaData;

    /**
     * The package registry.
     */
    private Registry packageRegistry;

    /**
     * The support of URI Handler.
     */
    private JsonResource.URIHandler uriHandler;

    /**
     * The support for Resource Entity.
     */
    private JsonResource.ResourceEntityHandler resourceEntityHandler;

    /**
     * The resource URI.
     */
    private URI resourceURI;

    /**
     * The missing package handler that allow to locate a package when all other mechanisms have failed.
     */
    private JsonResource.MissingPackageHandler missingPackageHandler;

    /**
     * The IEObjectHandler.
     */
    private IEObjectHandler eObjectHandler;

    /**
     * The constructor.
     *
     * @param resource
     *            the resource to deserialize
     * @param options
     *            the deserialization options
     */
    public GsonEObjectDeserializer(Resource resource, Map<?, ?> options) {
        super();
        if (options == null) {
            this.options = Collections.emptyMap();
        } else {
            this.options = options;
        }

        this.helper = (JsonHelper) this.options.get(JsonResource.OPTION_CUSTOM_HELPER);
        if (this.helper == null) {
            this.helper = new JsonHelper(resource);
        }
        if (resource instanceof JsonResource) {
            this.resource = (JsonResource) resource;
        }
        this.resourceSet = resource.getResourceSet();
        if (this.resourceSet == null) {
            this.packageRegistry = EPackage.Registry.INSTANCE;
        } else {
            this.packageRegistry = this.resourceSet.getPackageRegistry();
        }
        Object metaData = this.options.get(JsonResource.OPTION_EXTENDED_META_DATA);
        if (metaData instanceof Boolean) {
            if (metaData.equals(Boolean.TRUE)) {
                if (resource.getResourceSet() == null) {
                    this.extendedMetaData = ExtendedMetaData.INSTANCE;
                } else {
                    this.extendedMetaData = new BasicExtendedMetaData(resource.getResourceSet().getPackageRegistry());
                }
            }
        } else {
            this.extendedMetaData = (ExtendedMetaData) this.options.get(JsonResource.OPTION_EXTENDED_META_DATA);
        }
        if (this.extendedMetaData != null) {
            this.helper.setExtendedMetaData(this.extendedMetaData);
        }

        this.resourceURI = resource.getURI();

        // this.resolve = resourceURI != null && resourceURI.isHierarchical() && resourceURI.isRelative();

        this.uriHandler = (JsonResource.URIHandler) this.options.get(JsonResource.OPTION_URI_HANDLER);
        this.resourceEntityHandler = (JsonResource.ResourceEntityHandler) this.options.get(JsonResource.OPTION_RESOURCE_ENTITY_HANDLER);
        if (this.resourceEntityHandler != null) {
            this.resourceEntityHandler.reset();
            if (this.uriHandler == null && this.resourceEntityHandler instanceof JsonResource.URIHandler) {
                this.uriHandler = (URIHandler) this.resourceEntityHandler;
                this.uriHandler.setBaseURI(this.resourceURI);
            }
        }

        this.eObjectHandler = (JsonResource.IEObjectHandler) this.options.get(JsonResource.OPTION_EOBJECT_HANDLER);

        this.forwardSingleReferences = new ArrayList<GsonEObjectDeserializer.SingleReference>();

        this.helper.setOptions(this.options);

        this.missingPackageHandler = (JsonResource.MissingPackageHandler) this.options.get(JsonResource.OPTION_MISSING_PACKAGE_HANDLER);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type,
     *      com.google.gson.JsonDeserializationContext)
     */
    @Override
    public List<EObject> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonRoot = jsonElement.getAsJsonObject();

        // namespace
        JsonObject jsonNameSpace = jsonRoot.getAsJsonObject(IGsonConstants.NS);
        if (jsonNameSpace != null) {
            Set<Entry<String, JsonElement>> namespacesSet = jsonNameSpace.entrySet();
            for (Entry<String, JsonElement> entry : namespacesSet) {
                this.prefixToNsURi.put(entry.getKey(), entry.getValue().getAsString());
            }
        }

        // schema location
        JsonObject jsonSchemaLocation = jsonRoot.getAsJsonObject(IGsonConstants.SCHEMA_LOCATION);
        if (jsonSchemaLocation != null) {
            Set<Entry<String, JsonElement>> schemaLocationSet = jsonSchemaLocation.entrySet();
            for (Entry<String, JsonElement> entry : schemaLocationSet) {
                String schemaLocation = entry.getValue().getAsString();

                if (this.resourceSet != null && schemaLocation != null && schemaLocation.length() > 0) {
                    URI uri = URI.createURI(schemaLocation);
                    if (this.uriHandler != null) {
                        uri = this.uriHandler.resolve(uri);
                    }
                    // TODO: in condition, add a test for extendedMetaData (see line 2898 of
                    // XMLHandler.class)
                    uri = this.helper.resolve(uri, this.resourceURI);
                    Resource ePackageResource = this.resourceSet.getResource(uri, true);
                    TreeIterator<EObject> iterator = ePackageResource.getAllContents();
                    while (iterator.hasNext()) {
                        EObject eObject = iterator.next();
                        if (eObject instanceof EPackage) {
                            EPackage ePackage = (EPackage) eObject;
                            this.resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
                        }
                    }
                }
            }
        }

        // json content
        this.deserializeContent(jsonRoot);

        return this.rootElements;
    }

    /**
     * Deserialize the data field. Add the created EObject to the list of EObject that contain root elements.
     *
     * @param jsonObject
     *            the JsonObject
     */
    private void deserializeContent(JsonObject jsonObject) {
        JsonElement content = jsonObject.get(IGsonConstants.CONTENT);
        if (content != null) {
            JsonArray jsonData = content.getAsJsonArray();
            for (JsonElement element : jsonData) {
                JsonObject object = element.getAsJsonObject();
                this.loadObject(object, true);
            }
        }

        // end documents
        this.handleForwardReference();
    }

    /**
     * Check if the values of the forward references have been set. If not, set them.
     */
    private void handleForwardReference() {
        for (SingleReference ref : this.forwardSingleReferences) {
            EObject obj = this.helper.getResource().getEObject((String) ref.getValue());

            if (obj != null) {
                EStructuralFeature feature = ref.getFeature();
                this.setFeatureValue(ref.getObject(), feature, obj, ref.getPosition());

            }
        }
    }

    /**
     * Set the feature of the given object to the given value.
     *
     * @param object
     *            the object
     * @param feature
     *            the feature
     * @param value
     *            the value
     * @param position
     *            the position
     */
    private void setFeatureValue(EObject object, EStructuralFeature feature, Object value, int position) {

        EClassifier eClassifier = feature.getEType();
        if (eClassifier instanceof EDataType) {
            EDataType eDataType = (EDataType) eClassifier;
            EFactory eFactoryInstance = eDataType.getEPackage().getEFactoryInstance();
            if (value == null) {
                this.helper.setValue(object, feature, null);
            } else {
                this.helper.setValue(object, feature, this.createFromString(eFactoryInstance, eDataType, (String) value));
            }
        } else {
            // should not set a value that already set!!!!!
            // for opposite references there are some references already set
            if (feature.isMany()) {
                Object eGet = this.helper.getValue(object, feature);
                if (eGet instanceof InternalEList<?>) {
                    @SuppressWarnings("unchecked")
                    InternalEList<Object> list = (InternalEList<Object>) eGet;
                    if (!list.contains(value)) {
                        list.addUnique(position, value);
                    }
                }
            } else {
                this.helper.setValue(object, feature, value);
            }
        }

    }

    /**
     * Do the same as {@link EcoreUtil#createFromString(EDataType, String)}.
     *
     * @param eFactoryInstance
     *            the facoryInstance
     * @param eDataType
     *            the datatType
     * @param value
     *            the value
     * @return an object created from a given String
     */
    private Object createFromString(EFactory eFactoryInstance, EDataType eDataType, String value) {
        Object obj = eFactoryInstance.createFromString(eDataType, value);
        // TODO: extended MetaData field
        return obj;
    }

    /**
     * Load an object from a JsonObject.
     *
     * @param object
     *            the JsonObject
     * @param isTopObject
     *            if the given JsonObject is a top element
     * @return an EObject that represent the JsonObject
     */
    private EObject loadObject(JsonObject object, boolean isTopObject) {
        EObject eObject = null;

        JsonElement eClassJsonElement = object.get(IGsonConstants.ECLASS);
        if (!eClassJsonElement.isJsonNull()) {
            EClassifier eClassifier = this.getEClass(object, eClassJsonElement);
            eObject = this.deserializeEClassifier(eClassifier, object, isTopObject);
        }

        if (this.eObjectHandler != null) {
            this.eObjectHandler.processDeserializedContent(eObject, object);
        }

        JsonElement idJsonElement = object.get(IGsonConstants.ID);
        if (idJsonElement != null && this.resource != null) {
            this.resource.setID(eObject, idJsonElement.getAsString());
        }

        return eObject;
    }

    /**
     * Deserialize the given json object to an EObject with the given EClassifier as EType.
     *
     * @param eClassifier
     *            the EClassifier
     * @param jsonObject
     *            the Json representation of the EClassifier
     * @param addToContent
     *            By default, it is set to <code>true</code>, but if we deserialize containment references it is set to
     *            false
     * @return the EObject newly created by the deserialization.
     */
    private EObject deserializeEClassifier(EClassifier eClassifier, JsonObject jsonObject, boolean addToContent) {
        EObject eObject = null;
        if (eClassifier instanceof EClass) {
            EClass eClass = (EClass) eClassifier;
            eObject = EcoreUtil.create(eClass);
            if (addToContent) {
                this.addToContent(eObject);
            }
            JsonObject properties = jsonObject.getAsJsonObject(IGsonConstants.DATA);

            this.deserializeData(properties, eClass, eObject);
        }
        return eObject;
    }

    /**
     * Deserialize data field.
     *
     * @param properties
     *            data properties
     * @param eClass
     *            the EClass of the EObject
     * @param eObject
     *            the EObject container of data content
     */
    private void deserializeData(JsonObject properties, EClass eClass, EObject eObject) {
        if (properties != null) {
            Set<Entry<String, JsonElement>> entrySet = properties.entrySet();
            for (Entry<String, JsonElement> entry : entrySet) {
                EStructuralFeature eStructuralFeature = eClass.getEStructuralFeature(entry.getKey());

                if (eStructuralFeature instanceof EAttribute) {
                    this.deserializeEAttribute((EAttribute) eStructuralFeature, entry.getValue(), eObject);
                } else if (eStructuralFeature instanceof EReference) {
                    this.deserializeEReference((EReference) eStructuralFeature, entry.getValue(), eObject);
                }
            }
        }
    }

    /**
     * Deserialize and associate the given value at the given EReference to the Give Object.
     *
     * @param eReference
     *            the EReference
     * @param value
     *            the value to set into the EReference
     * @param eObject
     *            the EObject
     */
    private void deserializeEReference(EReference eReference, JsonElement value, EObject eObject) {
        if (eReference.isContainment()) {
            if (eReference.isMany()) {
                this.deserializeMultipleContainmentEReference(eReference, value, eObject);
            } else {
                this.deserialziseSingleContainmentEReference(eReference, value, eObject);
            }
        } else {
            if (eReference.isMany()) {
                this.deserializeMultipleNonContainmentEReference(eReference, value, eObject);
            } else {
                this.deserializeSingleNonContainmentEReference(eReference, value, eObject);
            }
        }
    }

    /**
     * Associate the given reference (in the JsonElement) to the given non-containment EReference of the given EObject.
     *
     * @param eReference
     *            the EReference
     * @param value
     *            the referenced Object uri
     * @param eObject
     *            the EObject
     */
    private void deserializeSingleNonContainmentEReference(EReference eReference, JsonElement value, EObject eObject) {

        String id = value.getAsString();

        if (id.startsWith("&") && this.resourceEntityHandler != null) { //$NON-NLS-1$
            id = this.handleResourceEntity(id).toString();
        }

        int index = id.indexOf(IGsonConstants.FRAGMENT_SEPARATOR, 0);
        if (index != -1) {
            String fragmentEMF = id.substring(index + 1, id.length()); // Where I can find the EObject in the
            // Resource
            if (index == 0) {
                // Fragments in Ecore resource
                EObject object = this.helper.getResource().getEObject(fragmentEMF);
                if (object == null) {
                    SingleReference singleReference = new SingleReference(eObject, eReference, fragmentEMF, 0);
                    this.forwardSingleReferences.add(singleReference);
                } else {
                    this.helper.setValue(eObject, eReference, object);
                }
            } else {
                String resourceURIPath = id.substring(0, index); // The URI of the resource where I can find
                // the EObject
                EPackage ePackage = this.getPackageForURI(resourceURIPath); // return an EPackage only if the
                // URI represent an EPackage
                Resource packageResource = null;
                EObject object = null;
                if (ePackage != null) {
                    packageResource = ePackage.eResource();
                    object = packageResource.getEObject(fragmentEMF);
                } else {
                    InternalEObject internalEObject = (InternalEObject) EcoreFactory.eINSTANCE.createEClass(); // Can I
                                                                                                               // have
                                                                                                               // other
                                                                                                               // type
                                                                                                               // of
                                                                                                               // EObject
                                                                                                               // here?
                    URI toResolveURI = URI.createURI(id);

                    toResolveURI = toResolveURI.resolve(this.resourceURI);
                    internalEObject.eSetProxyURI(toResolveURI);
                    object = EcoreUtil.resolve(internalEObject, eObject);
                }
                this.helper.setValue(eObject, eReference, object);
            }
        } else {
            // When Fragments in non Ecore Resource
            EObject resolvedEObject = this.helper.getResource().getEObject(id);
            if (resolvedEObject != null) {
                this.helper.setValue(eObject, eReference, resolvedEObject);
            } else {
                SingleReference singleReference = new SingleReference(eObject, eReference, id, 0);
                this.forwardSingleReferences.add(singleReference);
            }
        }
    }

    /**
     * Convert the uriString that contain Entity to a valid URI.
     *
     * @param uriString
     *            the uri String that contain entity
     * @return the converted URI
     */
    private URI handleResourceEntity(String uriString) {
        URI uri = null;
        int index = uriString.indexOf(';');
        if (index > 0) {
            String fragment = uriString.substring(index + 1);
            String entityName = uriString.substring(1, index);
            Map<String, String> nameToValueMap = this.resourceEntityHandler.getNameToValueMap();
            String entityValue = nameToValueMap.get(entityName);
            if (entityValue != null) {
                uri = URI.createURI(entityValue + fragment);
            }

        }
        return uri;
    }

    /**
     * Associate the given reference (in the JsonElement) to the given non-containment EReference of the given EObject.
     * The value is considered as an array.
     *
     * @param eReference
     *            the EReference
     * @param value
     *            referenced Objects uri
     * @param eObject
     *            the EObject
     */
    private void deserializeMultipleNonContainmentEReference(EReference eReference, JsonElement value, EObject eObject) {
        JsonArray array = value.getAsJsonArray();
        for (int i = 0; i < array.size(); ++i) {

            String id = array.get(i).getAsString();

            if (id.startsWith("&") && this.resourceEntityHandler != null) { //$NON-NLS-1$
                id = this.handleResourceEntity(id).toString();
            }

            int index = id.indexOf(IGsonConstants.FRAGMENT_SEPARATOR);
            if (index != -1) {
                String fragmentEMF = id.substring(index + 1, id.length());
                if (index == 0) {
                    EObject object = this.helper.getResource().getEObject(fragmentEMF);
                    if (object == null) {
                        // The reference may not be in the resource yet
                        SingleReference singleReference = new SingleReference(eObject, eReference, fragmentEMF, i);
                        this.forwardSingleReferences.add(singleReference);
                    } else {
                        this.helper.setUniqueValue(eObject, eReference, object);
                    }
                } else {
                    String resourceURIPath = id.substring(0, index);
                    EPackage ePackage = this.getPackageForURI(resourceURIPath);
                    Resource packageResource = null;
                    EObject object = null;
                    if (ePackage != null) {
                        packageResource = ePackage.eResource();
                        object = packageResource.getEObject(fragmentEMF);
                    } else {
                        InternalEObject internalEObject = (InternalEObject) EcoreFactory.eINSTANCE.createEClass(); // I
                                                                                                                   // don't
                                                                                                                   // think
                                                                                                                   // we
                                                                                                                   // can
                                                                                                                   // have
                                                                                                                   // an
                                                                                                                   // other
                                                                                                                   // Class
                        URI toResolveURI = URI.createURI(id);

                        toResolveURI = toResolveURI.resolve(this.resourceURI);
                        internalEObject.eSetProxyURI(toResolveURI);
                        object = EcoreUtil.resolve(internalEObject, eObject);
                    }
                    this.helper.setUniqueValue(eObject, eReference, object);
                }
            } else {
                EObject resolvedEObject = this.helper.getResource().getEObject(id);
                if (resolvedEObject != null) {
                    this.helper.setUniqueValue(eObject, eReference, resolvedEObject);
                } else {
                    SingleReference singleReference = new SingleReference(eObject, eReference, id, i);
                    this.forwardSingleReferences.add(singleReference);
                }
            }
        }
    }

    /**
     * Return an EPackage from a URI String.
     *
     * @param uriString
     *            the URI String representation
     * @return the EPackage corresponding to the given URI String
     */
    private EPackage getPackageForURI(String uriString) {
        if (uriString == null) {
            return null;
        }

        EPackage ePackage = null;

        if (this.extendedMetaData == null) {
            ePackage = this.packageRegistry.getEPackage(uriString);
        } else {
            ePackage = this.extendedMetaData.getPackage(uriString);
        }

        if (ePackage != null && ePackage.eIsProxy()) {
            ePackage = null;
        }

        URI uri = URI.createURI(uriString);
        if (ePackage == null) {
            if (uri.scheme() == null) {
                // This only works for old globally registered things.

                Iterator<Entry<String, Object>> iterator = this.packageRegistry.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> entry = iterator.next();
                    String nsURI = entry.getKey();
                    if (nsURI != null && nsURI.length() > uriString.length() && nsURI.endsWith(uriString) && nsURI.charAt(nsURI.length() - uriString.length() - 1) == '/') {
                        // oldStyleProxyURIs = true;
                        ePackage = (EPackage) entry.getValue();
                    }
                }
            }
        }

        if (ePackage == null) {

            // URI nsURI = uri;

            String fragment = uri.fragment();
            Resource packageResource = null;

            // be careful on adding some code : add a test for a null resource
            if (this.resourceSet != null) {
                URI trimmedURI = uri.trimFragment();
                if (this.uriHandler != null) {
                    trimmedURI = this.uriHandler.resolve(trimmedURI);
                }
                trimmedURI = this.helper.resolve(trimmedURI, this.resourceURI);

                packageResource = this.resourceSet.getResource(trimmedURI, false);
                if (packageResource != null && !packageResource.isLoaded()) {
                    try {
                        packageResource.load(this.resourceSet.getLoadOptions());
                    } catch (IOException e) {
                        // continue with an other approach.
                    }
                }
            }

            if (packageResource != null) {
                Object content = null;
                List<EObject> contents = packageResource.getContents();
                if (!contents.isEmpty()) {
                    content = contents.get(0);
                }
                if (!(content instanceof EPackage) && fragment != null) {
                    content = packageResource.getEObject(fragment);
                }

                if (content instanceof EPackage) {
                    ePackage = (EPackage) content;
                    this.helper.addPackage(ePackage);
                }
            }
        }

        if (ePackage == null) {
            ePackage = this.handleMissingPackage(uriString);
        }

        if (ePackage == null) {
            this.helper.getResource().getErrors().add(new PackageNotFoundException(uriString, this.helper.getResourceURI().toString()));
        }

        return ePackage;
    }

    /**
     * Associate the given value to the given containment EReference of the given EObject. The value is considered as an
     * array.
     *
     * @param eReference
     *            the EReference
     * @param value
     *            the value to set into the EReference
     * @param eObject
     *            the EObject
     */
    private void deserializeMultipleContainmentEReference(EReference eReference, JsonElement value, EObject eObject) {
        EObject eReferenceValue = null;
        String referenceName = eReference.getName();
        JsonArray jsonArray = value.getAsJsonArray();

        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // NOTE: these section must have to be improved
            if (referenceName.equals(IGsonConstants.EANNOTATIONS)) {
                eReferenceValue = this.loadReferences(EcorePackage.Literals.EANNOTATION, jsonObject);
            } else if (referenceName.equals(IGsonConstants.ELITERALS)) {
                eReferenceValue = this.loadReferences(EcorePackage.Literals.EENUM_LITERAL, jsonObject);
            } else if (referenceName.equals(IGsonConstants.EOPERATIONS)) {
                eReferenceValue = this.loadReferences(EcorePackage.Literals.EOPERATION, jsonObject);
            } else if (referenceName.equals(IGsonConstants.EPARAMETERS)) {
                eReferenceValue = this.loadReferences(EcorePackage.Literals.EPARAMETER, jsonObject);
            } else if (referenceName.equals(IGsonConstants.ETYPEPARAMETER_ARRAY)) {
                eReferenceValue = this.loadReferences(EcorePackage.Literals.ETYPE_PARAMETER, jsonObject);
            } else if (referenceName.equals(IGsonConstants.ETYPEARGUMENTS)) {
                eReferenceValue = this.loadReferences(EcorePackage.Literals.EGENERIC_TYPE, jsonObject);
            } else if (referenceName.equals(IGsonConstants.EGENERICEXCEPTION)) {
                eReferenceValue = this.loadReferences(EcorePackage.Literals.EGENERIC_TYPE, jsonObject);
            } else if (referenceName.equals(IGsonConstants.ETYPEPARAMETER)) {
                eReferenceValue = this.loadReferences(EcorePackage.Literals.EGENERIC_TYPE, jsonObject);
            } else if (referenceName.equals(IGsonConstants.EGENERICSUPERTYPES)) {
                eReferenceValue = this.loadReferences(EcorePackage.Literals.EGENERIC_TYPE, jsonObject);
            } else if (referenceName.equals(IGsonConstants.EBOUNDS)) {
                eReferenceValue = this.loadReferences(EcorePackage.Literals.EGENERIC_TYPE, jsonObject);
            } else if (referenceName.equals(IGsonConstants.DETAILS)) {
                eReferenceValue = this.loadReferences(EcorePackage.Literals.ESTRING_TO_STRING_MAP_ENTRY, jsonObject);
            } else if (referenceName.equals(IGsonConstants.ESUBPACKAGES)) {
                JsonObject properties = jsonObject.getAsJsonObject(IGsonConstants.DATA);
                eReferenceValue = this.loadReferences(EcorePackage.Literals.EPACKAGE, properties);
                EPackage ePackage = (EPackage) eReferenceValue;
                this.resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
            } else {
                eReferenceValue = this.loadObject(jsonObject, false);
            }
            Object eGet = this.helper.getValue(eObject, eReference);
            if (eGet instanceof Collection<?>) {
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) eGet;
                collection.add(eReferenceValue);
            }
        }
    }

    /**
     * Deserialize json representation of the given EClass.
     *
     * @param eClass
     *            the EClass to deserialize
     * @param jsonObject
     *            the json representation of given EClass
     * @return the reference to the instantiate from EClass EObject from given json data
     */
    private EObject loadReferences(EClass eClass, JsonObject jsonObject) {
        EObject eObject = EcoreUtil.create(eClass);
        this.deserializeData(jsonObject, eClass, eObject);
        return eObject;
    }

    /**
     * Associate the given value to the given containment ERference of the given EObject.
     *
     * @param eReference
     *            the ERference
     * @param value
     *            the value to set into the ERefrence
     * @param eObject
     *            the EObject
     */
    private void deserialziseSingleContainmentEReference(EReference eReference, JsonElement value, EObject eObject) {
        JsonObject jsonObject = value.getAsJsonObject();
        EObject eReferenceValue = null;
        String referenceName = eReference.getName();
        if (referenceName.equals(IGsonConstants.EGENERICTYPE)) {
            eReferenceValue = this.loadEGenericType(jsonObject);
        } else {
            eReferenceValue = this.loadObject(jsonObject, false);
        }
        this.helper.setValue(eObject, eReference, eReferenceValue);
    }

    /**
     * Deserialize json representation of EGenericType.
     *
     * @param jsonObject
     *            the json representation of an EGenericType
     * @return the reference to the EGenereicType
     */
    private EObject loadEGenericType(JsonObject jsonObject) {
        EClass eClass = EcorePackage.Literals.EGENERIC_TYPE;
        EObject eGenericType = EcoreUtil.create(eClass);
        this.deserializeData(jsonObject, eClass, eGenericType);
        return eGenericType;
    }

    /**
     * Deserialize the given EAttribute and add the given value to the given EObject.
     *
     * @param eAttribute
     *            the given eAttribute
     * @param jsonElement
     *            the given value
     * @param eObject
     *            the given EObject
     */
    private void deserializeEAttribute(EAttribute eAttribute, JsonElement jsonElement, EObject eObject) {
        EDataType dataType = eAttribute.getEAttributeType();
        if (!eAttribute.isMany()) {
            String newValue = jsonElement.getAsString();
            Object value = EcoreUtil.createFromString(dataType, newValue);
            this.helper.setValue(eObject, eAttribute, value);
        } else {
            JsonArray asJsonArray = jsonElement.getAsJsonArray();
            Object eGet = this.helper.getValue(eObject, eAttribute);
            if (eGet instanceof Collection<?>) {
                for (JsonElement jElement : asJsonArray) {
                    Object value = EcoreUtil.createFromString(dataType, jElement.getAsString());
                    @SuppressWarnings("unchecked")
                    Collection<Object> collection = (Collection<Object>) eGet;
                    collection.add(value);
                }
            }
        }
    }

    /**
     * Return the EClassifier from the given jsonObject if it exist, otherwise return <code>null</code>.
     *
     * @param object
     *            the given JsonObject
     * @param eClassJsonElement
     * @return the EClassifier from the given jsonObject if it exist, otherwise return <code>null</code>
     */
    private EClassifier getEClass(JsonObject object, JsonElement eClassJsonElement) {
        String jsonEClass = eClassJsonElement.getAsString();

        String nsPrefix = jsonEClass;
        String className = jsonEClass;

        int index = jsonEClass.indexOf(':');
        if (index > 0) {
            nsPrefix = jsonEClass.substring(0, index);
            className = jsonEClass.substring(index + 1, jsonEClass.length());
        }

        String nsUri = this.prefixToNsURi.get(nsPrefix);

        EPackage ePackage = null;
        if (this.resourceSet != null) {
            ePackage = this.resourceSet.getPackageRegistry().getEPackage(nsUri);
        }

        if (ePackage == null) {
            ePackage = this.getPackageForURI(nsUri);
        }

        if (ePackage != null) {
            return ePackage.getEClassifier(className);
        }

        return null;
    }

    /**
     * Class that represent a forward Single Reference.
     */
    protected static class SingleReference {

        /**
         * An EObject.
         */
        private EObject object;

        /**
         * A Feature.
         */
        private EStructuralFeature feature;

        /**
         * A Value.
         */
        private Object value;

        /**
         * a position.
         */
        private int position;

        /**
         * The constructor.
         *
         * @param object
         *            the object
         * @param feature
         *            the feature
         * @param value
         *            the value
         * @param position
         *            the position
         */
        public SingleReference(EObject object, EStructuralFeature feature, Object value, int position) {
            super();
            this.object = object;
            this.feature = feature;
            this.value = value;
            this.position = position;
        }

        /**
         * Returns the object.
         *
         * @return The object
         */
        public EObject getObject() {
            return this.object;
        }

        /**
         * Returns the feature.
         *
         * @return The feature
         */
        public EStructuralFeature getFeature() {
            return this.feature;
        }

        /**
         * Returns the value.
         *
         * @return The value
         */
        public Object getValue() {
            return this.value;
        }

        /**
         * Returns the position.
         *
         * @return The position
         */
        public int getPosition() {
            return this.position;
        }

    }

    /**
     * Add the given EObject to the Resource and the list of content.
     *
     * @param eObject
     *            the given EObject
     */
    private void addToContent(EObject eObject) {
        this.helper.getResource().getContents().add(eObject);
        this.rootElements.add(eObject);
    }

    /**
     * Locate and return from the nsURI, the EPackage when normal behavior fail to found the EPackage. Return
     * <code>null</code> if there is no corresponding nsURI.
     *
     * @param nsURI
     *            the
     * @return the found EPackage. <code>null</code> otherwise
     */
    private EPackage handleMissingPackage(String nsURI) {
        EPackage foundPackage = null;
        // Do something here when extendedMata data will be supported on deserialization.
        if (this.missingPackageHandler != null) {
            foundPackage = this.missingPackageHandler.getEPackage(nsURI);
        }
        return foundPackage;
    }

}
