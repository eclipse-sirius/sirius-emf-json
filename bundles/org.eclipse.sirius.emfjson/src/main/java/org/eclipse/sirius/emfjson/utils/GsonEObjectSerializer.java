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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sirius.emfjson.resource.IDManager;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResource.EStructuralFeaturesFilter;
import org.eclipse.sirius.emfjson.resource.JsonResource.IEObjectHandler;
import org.eclipse.sirius.emfjson.resource.JsonResource.ResourceEntityHandler;
import org.eclipse.sirius.emfjson.resource.exception.DanglingHREFException;

/**
 * The Gson serializer is responsible for the serialization of EObjects.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class GsonEObjectSerializer implements JsonSerializer<List<EObject>> {

    /**
     * In case of serialize non containment references, where the reference is not found.
     */
    private static final int SKIP = 0;

    /**
     * In case of serialize non containment references, where the reference is in the same document.
     */
    private static final int SAME_DOC = 1;

    /**
     * In case of serialize non containment references, where the reference is in an other document.
     */
    private static final int CROSS_DOC = 2;

    /**
     * the helper.
     */
    private JsonHelper helper;

    /**
     * If the schemaLocation should be serialized/deserialzed.
     */
    private boolean declareSchemaLocation;

    /**
     * The serialization options.
     */
    private Map<?, ?> options;

    /**
     * The support of extended meta data.
     */
    private ExtendedMetaData extendedMetaData;

    /**
     * The support for resource entity.
     */
    private JsonResource.ResourceEntityHandler resourceEntityHandler;

    /**
     * The IEObject Handler.
     *
     * @see org.eclipse.sirius.emfjson.resource.JsonResource.IEObjectHandler
     */
    private IEObjectHandler eObjectHandler;

    /**
     * The constructor.
     *
     * @param resource
     *            the resource to serialize
     * @param options
     *            the serialization options
     */
    public GsonEObjectSerializer(Resource resource, Map<?, ?> options) {
        super();
        Map<?, ?> serializedOptions = options;

        this.resourceEntityHandler = (ResourceEntityHandler) serializedOptions.get(JsonResource.OPTION_RESOURCE_ENTITY_HANDLER);
        if (this.resourceEntityHandler instanceof JsonResource.URIHandler && !serializedOptions.containsKey(JsonResource.OPTION_URI_HANDLER)) {
            Map<Object, Object> newOptions = new LinkedHashMap<Object, Object>(serializedOptions);
            newOptions.put(JsonResource.OPTION_URI_HANDLER, this.resourceEntityHandler);
            serializedOptions = newOptions;
        }

        this.helper = (JsonHelper) serializedOptions.get(JsonResource.OPTION_CUSTOM_HELPER);
        if (this.helper == null) {
            this.helper = new JsonHelper(resource);
        }
        Object metaData = serializedOptions.get(JsonResource.OPTION_EXTENDED_META_DATA);
        if (metaData instanceof Boolean) {
            if (metaData.equals(Boolean.TRUE)) {
                if (resource == null || resource.getResourceSet() == null) {
                    this.extendedMetaData = ExtendedMetaData.INSTANCE;
                } else {
                    this.extendedMetaData = new BasicExtendedMetaData(resource.getResourceSet().getPackageRegistry());
                }
            }
        } else {
            this.extendedMetaData = (ExtendedMetaData) serializedOptions.get(JsonResource.OPTION_EXTENDED_META_DATA);
        }
        if (this.extendedMetaData != null) {
            this.helper.setExtendedMetaData(this.extendedMetaData);
        }

        this.eObjectHandler = (JsonResource.IEObjectHandler) serializedOptions.get(JsonResource.OPTION_EOBJECT_HANDLER);

        this.declareSchemaLocation = Boolean.TRUE.equals(options.get(JsonResource.OPTION_SCHEMA_LOCATION));

        this.helper.setOptions(serializedOptions);
        this.options = serializedOptions;

    }

    /**
     * {@inheritDoc}
     *
     * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type,
     *      com.google.gson.JsonSerializationContext)
     */
    @Override
    public JsonElement serialize(List<EObject> eObjects, Type type, JsonSerializationContext context) {
        JsonArray data = new JsonArray();
        for (EObject eObject : eObjects) {
            data.add(this.createData(eObject));
        }

        JsonElement jsonHeader = this.createJsonHeader();
        JsonElement nsHeader = this.createNsHeader();
        JsonElement schemaLocationHeader = this.createSchemaLocationHeader();

        JsonObject jsonObject = new JsonObject();
        jsonObject.add(IGsonConstants.JSON, jsonHeader);
        jsonObject.add(IGsonConstants.NS, nsHeader);
        if (schemaLocationHeader != null) {
            jsonObject.add(IGsonConstants.SCHEMA_LOCATION, schemaLocationHeader);
        }
        jsonObject.add(IGsonConstants.CONTENT, data);

        return jsonObject;
    }

    /**
     * Return data field.
     *
     * @param eObject
     *            the eObject to serialize
     * @return data field.
     */
    private JsonElement createData(EObject eObject) {
        JsonElement jsonElement = null;
        if (eObject instanceof EAnnotation) {
            jsonElement = this.serializeEAnnotation((EAnnotation) eObject);
        } else if (eObject instanceof EOperation) {
            jsonElement = this.serializeEOperation((EOperation) eObject);
        } else if (eObject instanceof EParameter) {
            jsonElement = this.serializeEParameter((EParameter) eObject);
        } else if (eObject instanceof EGenericType) {
            jsonElement = this.serializeEGenericType((EGenericType) eObject);
        } else if (eObject instanceof ETypeParameter) {
            jsonElement = this.serializeETypeParameter((ETypeParameter) eObject);
        } else if (eObject instanceof EEnumLiteral) {
            jsonElement = this.serializeEEnumLiteral((EEnumLiteral) eObject);
        } else {
            if (eObject instanceof EPackage && ((EPackage) eObject).getESuperPackage() != null) {
                jsonElement = this.serializeESubPackage((EPackage) eObject);
            } else {
                jsonElement = this.serializationEClass(eObject);
            }
        }

        if (this.eObjectHandler != null) {
            this.eObjectHandler.processSerializedContent(jsonElement, eObject);
        }

        return jsonElement;
    }

    /**
     * Serialize the given subPackage to a jsonObject.
     *
     * @param ePackage
     *            the EPackage to serialize
     * @return the json representation of subPackages
     */
    private JsonElement serializeESubPackage(EPackage ePackage) {
        JsonObject jsonObject = new JsonObject();
        JsonObject structualFeaturesSerialized = this.serializeEAllStructuralFeatures(ePackage);
        if (structualFeaturesSerialized.entrySet().size() != 0) {
            jsonObject.add(IGsonConstants.DATA, structualFeaturesSerialized);
        }
        return jsonObject;
    }

    /**
     * Serialize the given EClass of the given EObject.
     *
     * @param eObject
     *            the given EObject
     * @return the representation of the EClass
     */
    private JsonElement serializationEClass(EObject eObject) {
        JsonObject jsonObject = new JsonObject();
        EClass eClass = eObject.eClass();

        // Handle eObject id
        Object supplierObject = this.options.get(JsonResource.OPTION_ID_MANAGER);
        if (supplierObject instanceof IDManager) {
            IDManager idManager = (IDManager) supplierObject;
            String id = idManager.getOrCreateId(eObject);
            jsonObject.add(IGsonConstants.ID, new JsonPrimitive(id));
        }

        // element Handler here

        String name = this.helper.getQName(eClass);

        jsonObject.add(IGsonConstants.ECLASS, new JsonPrimitive(name));

        JsonObject structualFeaturesSerialized = this.serializeEAllStructuralFeatures(eObject);
        if (structualFeaturesSerialized.entrySet().size() != 0) {
            jsonObject.add(IGsonConstants.DATA, structualFeaturesSerialized);
        }
        return jsonObject;
    }

    /**
     * Serialize the given EEnumLiteral to a JsonObject.
     *
     * @param literal
     *            the EEnumLiteral to serialize
     * @return the Json representation of an EEnumLiteral
     */
    private JsonElement serializeEEnumLiteral(EEnumLiteral literal) {
        JsonObject jsonObject = new JsonObject();

        // Annotations
        EReference eenumEliterals = EcorePackage.Literals.EMODEL_ELEMENT__EANNOTATIONS;
        JsonElement element = this.serializeEReference(literal, eenumEliterals);
        if (element.isJsonArray() && ((JsonArray) element).size() > 0) {
            jsonObject.add(eenumEliterals.getName(), element);
        }

        // Name
        jsonObject.add(IGsonConstants.NAME, new JsonPrimitive(literal.getName()));

        // Value
        if (literal.getValue() != 0) {
            jsonObject.add(IGsonConstants.VALUE, new JsonPrimitive(Integer.valueOf(literal.getValue())));
        }

        // Literal
        boolean eIsSet = literal.eIsSet(EcorePackage.Literals.EENUM_LITERAL__LITERAL);
        if (eIsSet) {
            jsonObject.add(IGsonConstants.LITERAL, new JsonPrimitive(literal.getLiteral()));
        }

        return jsonObject;
    }

    /**
     * Private method used in serialization of ETypeParameter and ETypeArguments.
     *
     * @param eGenericTypes
     *            the list of EGenericType to serialize
     * @return a jsonArray representing the serialization of the given EGeneicType list
     */
    private JsonArray serializationJsonArray(EList<EGenericType> eGenericTypes) {
        JsonArray array = new JsonArray();
        for (EGenericType eGenericType : eGenericTypes) {
            JsonObject jsonObject = new JsonObject();
            ETypeParameter typeParameter = eGenericType.getETypeParameter();
            EClassifier classifier = eGenericType.getEClassifier();
            if (typeParameter != null) {
                // extends an ETypeParameter
                String typeParameterReferenceBound = EcoreUtil.getURI(typeParameter).fragment();
                jsonObject.add(IGsonConstants.ETYPEPARAMETER, new JsonPrimitive(typeParameterReferenceBound));
            } else if (classifier != null) {
                // extends an EClassifier
                String classifierJsonValue = this.removeFragmentSeparator(this.helper.deresolve(EcoreUtil.getURI(classifier)).toString());
                jsonObject.add(IGsonConstants.ECLASSIFIER, new JsonPrimitive(classifierJsonValue));
                JsonElement element = this.serializationETypeArguments(eGenericType);
                if (element != null) {
                    jsonObject.add(IGsonConstants.ETYPEARGUMENTS, element);
                }
            }
            array.add(jsonObject);
        }
        return array;
    }

    /**
     * Serialize EParameterType of the given object to a JsonObject.
     *
     * @param eTypeParameter
     *            the ETypeParameter to serialize
     * @return the JsonElement representing the EParameterType
     */
    private JsonElement serializeETypeParameter(ETypeParameter eTypeParameter) {
        JsonObject object = new JsonObject();

        // ETypeParameter name
        String eTypeParameterName = eTypeParameter.getName();
        object.add(IGsonConstants.NAME, new JsonPrimitive(eTypeParameterName));

        // EAnnotation
        EReference eReference = EcorePackage.Literals.EMODEL_ELEMENT__EANNOTATIONS;
        JsonElement element = this.serializeEReference(eTypeParameter, eReference);
        if (element.isJsonArray() && element.getAsJsonArray().size() > 0) {
            object.add(eReference.getName(), element);
        }

        // EBounds
        EList<EGenericType> eGenericTypes = eTypeParameter.getEBounds();

        if (eGenericTypes.size() > 0) {
            JsonArray array = this.serializationJsonArray(eGenericTypes);
            object.add(IGsonConstants.EBOUNDS, array);
        }

        return object;
    }

    /**
     * Return a JsonElement that representing an array of ETypeArgument of a given EGenerictype.
     *
     * @param genericType
     *            the given genericType
     * @return the JsonElement that representing an array of ETypeArgument
     */
    private JsonElement serializationETypeArguments(EGenericType genericType) {
        JsonElement jsonElement = null;

        EList<EGenericType> eGenericTypes = genericType.getETypeArguments();
        if (eGenericTypes.size() > 0) {
            JsonArray array = this.serializationJsonArray(eGenericTypes);
            jsonElement = array;
        }
        return jsonElement;
    }

    /**
     * Serialize EGenericType of the give object to a JsonObject. Can return <code>null</code> if the EGenericType does
     * not contain any eTypeArgument
     *
     * @param eGenericType
     *            The EGenericType to serialize
     * @return the JsonElement representing the EGenericType
     */
    private JsonElement serializeEGenericType(EGenericType eGenericType) {
        JsonElement element = null;
        JsonObject jsonObject = new JsonObject();

        if (eGenericType.getETypeParameter() != null) {
            // if eGenericType is a ETypeParameter
            String typeParameter = this.getReferenceUri(EcoreUtil.getURI(eGenericType.getETypeParameter()));
            jsonObject.add(IGsonConstants.ETYPEPARAMETER, new JsonPrimitive(typeParameter));
        } else if (eGenericType.getEClassifier() instanceof EClass) {

            String stringClassifier = ""; //$NON-NLS-1$
            stringClassifier += this.helper.deresolve(EcoreUtil.getURI(eGenericType.getEClassifier()));

            jsonObject.add(IGsonConstants.ECLASSIFIER, new JsonPrimitive(this.removeFragmentSeparator(stringClassifier)));
            jsonObject.add(IGsonConstants.ETYPEARGUMENTS, this.serializationETypeArguments(eGenericType));
        } else {
            // EMAP - ELIST
            EClassifier classifier = eGenericType.getEClassifier();
            String classifierJsonValue = this.helper.deresolve(EcoreUtil.getURI(classifier)).toString();

            jsonObject.add(IGsonConstants.ECLASSIFIER, new JsonPrimitive(this.removeFragmentSeparator(classifierJsonValue)));
            jsonObject.add(IGsonConstants.ETYPEARGUMENTS, this.serializationETypeArguments(eGenericType));
        }
        element = jsonObject;

        return element;
    }

    /**
     * Serialize EParameter of the given object to a JsonObject.
     *
     * @param eParameter
     *            The EParameter to serialize
     * @return the JsonElement representing the EAnnotation
     */
    private JsonElement serializeEParameter(EParameter eParameter) {
        JsonObject object = new JsonObject();

        // Parameter Name
        String parameterName = eParameter.getName();
        object.add(IGsonConstants.NAME, new JsonPrimitive(parameterName));

        // Parameter Type
        object = this.serializationETypedElementType(object, eParameter);

        // EAnnotation
        EReference eReference = EcorePackage.Literals.EMODEL_ELEMENT__EANNOTATIONS;
        JsonElement element = this.serializeEReference(eParameter, eReference);
        if (element.isJsonArray() && element.getAsJsonArray().size() > 0) {
            object.add(eReference.getName(), element);
        }

        return object;
    }

    /**
     * Private method use to serialize ETypedElement (common part of EOperation and EParameter serialization).
     *
     * @param object
     *            an object containing some json
     * @param eTypedElement
     *            the ETypedElement to serialize (EOperation/EParameter)
     * @return a jsonObject containing ETypedElement data
     */
    private JsonObject serializationETypedElementType(JsonObject object, ETypedElement eTypedElement) {
        JsonObject jsonObject = object;

        EGenericType eGenericType = eTypedElement.getEGenericType();

        JsonObject genericTypeJson = null;
        JsonElement element = null;
        if (eGenericType != null) {
            EClassifier genericEClassifier = eGenericType.getEClassifier();
            ETypeParameter genericETypeParameter = eGenericType.getETypeParameter();
            if (genericEClassifier != null) {
                if (genericEClassifier.getETypeParameters() != null && genericEClassifier.getETypeParameters().size() > 0) {
                    // EMap - EList
                    element = this.serializeEGenericType(eGenericType);
                }
            } else if (genericETypeParameter != null) {
                element = this.serializeEGenericType(eGenericType);
            }
        }

        if (element != null && element.isJsonObject()) {
            // type is generic
            genericTypeJson = element.getAsJsonObject();
            jsonObject.add(IGsonConstants.EGENERICTYPE, genericTypeJson);
        } else if (eTypedElement.getEType() != null) {
            // type is not generic
            EClassifier operationClassifier = eTypedElement.getEType();
            URI operationURI = EcoreUtil.getURI(operationClassifier);
            URI eTypeURI = this.helper.deresolve(operationURI);
            String classifierJsonValue = this.removeFragmentSeparator(eTypeURI.toString());
            jsonObject.add(IGsonConstants.ETYPE, new JsonPrimitive(classifierJsonValue));
        }

        return jsonObject;
    }

    /**
     * Serialize EOperation of the given object to a JsonObject.
     *
     * @param eOperation
     *            the EOpeation to serialize
     * @return the JsonElement representing the EAnnotation
     */
    private JsonElement serializeEOperation(EOperation eOperation) {
        JsonObject object = new JsonObject();
        // Operation Name
        String operationName = eOperation.getName();
        object.add(IGsonConstants.NAME, new JsonPrimitive(operationName));

        // Annotations
        EReference eReference = EcorePackage.Literals.EMODEL_ELEMENT__EANNOTATIONS;
        JsonElement element = this.serializeEReference(eOperation, eReference);
        if (element.isJsonArray() && element.getAsJsonArray().size() > 0) {
            object.add(eReference.getName(), element);
        }

        // Return Type
        object = this.serializationETypedElementType(object, eOperation);

        // TypeParameter
        EList<ETypeParameter> eTypeParameters = eOperation.getETypeParameters();
        JsonArray typeParameterArray = new JsonArray();
        if (eTypeParameters.size() > 0) {
            for (ETypeParameter eTypeParameter : eTypeParameters) {
                typeParameterArray.add(this.serializeETypeParameter(eTypeParameter));
            }
            object.add(IGsonConstants.ETYPEPARAMETER_ARRAY, typeParameterArray);
        }

        // Parameters
        EList<EParameter> eParameters = eOperation.getEParameters();
        if (eParameters.size() != 0) {
            JsonArray array = new JsonArray();
            for (EParameter eParameter : eParameters) {
                array.add(this.serializeEParameter(eParameter));
            }
            object.add(IGsonConstants.EPARAMETERS, array);
        }

        // Exceptions
        JsonArray jsonArray = new JsonArray();
        EList<EGenericType> eGenericExceptions = eOperation.getEGenericExceptions();
        boolean hasGenericException = false;
        if (eGenericExceptions.size() > 0) {
            int genericExceptionSize = eGenericExceptions.size();
            int index = 0;
            while (!hasGenericException && index < genericExceptionSize) {
                EGenericType exception = eGenericExceptions.get(index);
                if (exception.getEClassifier() != null) {
                    if (exception.getETypeArguments() != null && exception.getETypeArguments().size() > 0) {
                        hasGenericException = true;
                    }
                } else if (exception.getETypeParameter() != null) {
                    // ETypeParameter
                    hasGenericException = true;
                } else {
                    // expression = <?>
                    // should not happen because <?> expression is consider as a java.lang.Object
                    hasGenericException = true;
                }
                index++;
            }
        }
        EList<EClassifier> eClassifiers = eOperation.getEExceptions();
        if (hasGenericException) {
            // at least one exception is generic
            JsonArray array = new JsonArray();
            for (EGenericType eGenericException : eGenericExceptions) {
                JsonElement jsonElement = null;
                if (eGenericException.getEClassifier() != null) {
                    if (eGenericException.getETypeArguments() != null && eGenericException.getETypeArguments().size() > 0) {
                        jsonElement = this.serializeEGenericType(eGenericException);
                    } else {
                        EClassifier eClassifier = eGenericException.getEClassifier();
                        URI eClassifierURI = EcoreUtil.getURI(eClassifier);
                        URI eExceptionURI = this.helper.deresolve(eClassifierURI);
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.add(IGsonConstants.ECLASSIFIER, new JsonPrimitive(this.removeFragmentSeparator(eExceptionURI.toString())));
                        jsonElement = jsonObject;
                    }
                } else if (eGenericException.getETypeArguments() != null) {
                    jsonElement = this.serializeEGenericType(eGenericException);
                } else {
                    // expression = <?>
                    // should not happen because <?> expression is consider as a java.lang.Object
                    jsonElement = this.serializeEGenericType(eGenericException);
                }
                array.add(jsonElement);
            }
            object.add(IGsonConstants.EGENERICEXCEPTION, array);

        } else if (eClassifiers.size() > 0) {
            for (EClassifier eClassifier : eClassifiers) {
                URI eClassifierURI = EcoreUtil.getURI(eClassifier);
                URI eExceptionURI = this.helper.deresolve(eClassifierURI);
                jsonArray.add(new JsonPrimitive(this.removeFragmentSeparator(eExceptionURI.toString())));
            }
            object.add(IGsonConstants.EEXCEPTIONS, jsonArray);
        }

        return object;
    }

    /**
     * Serialize EAnnotation of the given object to a JsonObject.
     *
     * @param eAnnotation
     *            The EAnnotation to serialize
     * @return the JsonElement representing the EAnnotation
     */
    private JsonElement serializeEAnnotation(EAnnotation eAnnotation) {
        JsonObject object = new JsonObject();

        // EAnnotation source
        String annotationSource = eAnnotation.getSource();
        if (annotationSource != null) {
            object.add(IGsonConstants.SOURCE, new JsonPrimitive(annotationSource));
        }

        // EAnnotation references
        JsonArray array = new JsonArray();
        EList<EObject> references = eAnnotation.getReferences();
        if (references.size() > 0) {
            for (EObject eObject : references) {
                array.add(new JsonPrimitive(this.removeFragmentSeparator(this.helper.deresolve(EcoreUtil.getURI(eObject)).toString())));
            }
            object.add(IGsonConstants.REFERENCES, array);
        }

        // EAnnotation details
        JsonArray jsonArray = new JsonArray();
        EMap<String, String> annotationDetails = eAnnotation.getDetails();
        if (annotationDetails.size() > 0) {
            for (Entry<String, String> entry : annotationDetails) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add(IGsonConstants.KEY, new JsonPrimitive(entry.getKey()));
                jsonObject.add(IGsonConstants.VALUE, new JsonPrimitive(entry.getValue()));
                jsonArray.add(jsonObject);
            }
            object.add(IGsonConstants.DETAILS, jsonArray);
        }

        return object;
    }

    /**
     * Return the schema location header field if needed.
     *
     * @return the schema location header field if needed
     */
    private JsonElement createSchemaLocationHeader() {
        JsonObject jsonObject = null;
        EPackage noNamespacePackage = this.helper.getNoNamespacePackage();
        EPackage[] packages = this.helper.packages();
        StringBuffer xsiSchemaLocation = new StringBuffer();
        String xsiNoNamespaceSchemaLocation = null;
        if (this.declareSchemaLocation) {
            Map<String, String> handleBySchemaLocationMap = Collections.emptyMap();
            // NOTE: there is a case on xsd file : look at XMLSaveimpl#

            for (EPackage ePackage : packages) {

                if (noNamespacePackage == ePackage) {
                    if (ePackage.eResource() != null && !handleBySchemaLocationMap.containsKey(null)) {
                        xsiNoNamespaceSchemaLocation = this.getNoNamespaceSchemaLocation(ePackage);
                    }
                } else {
                    Resource resource = ePackage.eResource();
                    if (resource != null) {
                        jsonObject = this.getJsonObjectFromResource(ePackage, handleBySchemaLocationMap, resource, xsiSchemaLocation);
                    }
                }
            }
        }

        if (xsiNoNamespaceSchemaLocation != null) {
            if (jsonObject == null) {
                jsonObject = new JsonObject();
            }
            // NOTE: this is wrong, need to check this when test case handle this.
            jsonObject.add(xsiNoNamespaceSchemaLocation, new JsonPrimitive("")); //$NON-NLS-1$
        }

        return jsonObject;
    }

    /**
     * Return JsonObject representation of schemLocation xml Attribute from given elements.
     *
     * @param ePackage
     *            the EPackage to gateNamespace
     * @param handleBySchemaLocationMap
     *            if
     * @param resource
     *            the resource to get URI
     * @param xsiSchemaLocation
     *            the xsiSchemaLocation content
     * @return JsonObject representation of schemLocation xml Attribute from given elements
     */
    private JsonObject getJsonObjectFromResource(EPackage ePackage, Map<String, String> handleBySchemaLocationMap, Resource resource, StringBuffer xsiSchemaLocation) {
        JsonObject jsonObject = null;
        String nsURI = null;
        if (this.extendedMetaData == null) {
            nsURI = ePackage.getNsURI();
        } else {
            nsURI = this.extendedMetaData.getNamespace(ePackage);
        }
        // NOTE: handleBySchemaLocationMap useless now, but will be usefull when we will support the xsd
        // mapping
        if (!handleBySchemaLocationMap.containsKey(nsURI)) {
            URI uri = resource.getURI();
            boolean bAndNotC = uri == null && nsURI != null;
            boolean notBAndD = uri != null && !uri.toString().equals(nsURI);
            if (bAndNotC || notBAndD) {
                jsonObject = this.getJsonObjectFromURI(ePackage, xsiSchemaLocation, nsURI, uri);
            }
        }
        return jsonObject;
    }

    /**
     * Return JsonObject representation of schemLocation xml Attribute from given elements.
     *
     * @param ePackage
     *            the EPackage to get an HREF
     * @param xsiSchemaLocation
     *            the xsiSchemaLocation content
     * @param nsURI
     *            the nsURI
     * @param uri
     *            the URI
     * @return JsonObject representation of schemLocation xml Attribute from given elements
     */
    private JsonObject getJsonObjectFromURI(EPackage ePackage, StringBuffer xsiSchemaLocation, String nsURI, URI uri) {

        if (xsiSchemaLocation.length() > 0) {
            xsiSchemaLocation.append(' ');
        }
        xsiSchemaLocation.append(nsURI);
        String location = this.helper.getHREF(ePackage);
        location = this.convertURI(location);
        if (location.endsWith("#/")) { //$NON-NLS-1$
            location = location.substring(0, location.length() - 2);
            if (uri != null && uri.hasFragment()) {
                location += IGsonConstants.FRAGMENT_SEPARATOR + uri.fragment();
            }
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(xsiSchemaLocation.toString(), new JsonPrimitive(location));
        return jsonObject;
    }

    /**
     * Return the noNamespaceSchemalocation value.
     *
     * @param ePackage
     *            the EPackage to get HREF
     * @return the noNamespaceSchemalocation value
     */
    private String getNoNamespaceSchemaLocation(EPackage ePackage) {
        String xsiNoNamespaceSchemaLocation = this.helper.getHREF(ePackage);
        if (xsiNoNamespaceSchemaLocation != null && xsiNoNamespaceSchemaLocation.endsWith("#/")) { //$NON-NLS-1$
            xsiNoNamespaceSchemaLocation = xsiNoNamespaceSchemaLocation.substring(0, xsiNoNamespaceSchemaLocation.length() - 2);
        }
        return xsiNoNamespaceSchemaLocation;
    }

    /**
     * Return the converted URI.
     *
     * @param uri
     *            the URI
     * @return the converted URI
     */
    private String convertURI(String uri) {
        String convertedURI = uri;
        if (this.resourceEntityHandler != null) {
            int index = uri.indexOf(IGsonConstants.FRAGMENT_SEPARATOR);
            if (index > 0) {
                String baseURI = uri.substring(0, index);
                String fragment = uri.substring(index + 1);
                String entityName = this.resourceEntityHandler.getEntityName(baseURI);
                if (entityName != null) {
                    convertedURI = "&" + entityName + ";#" + fragment; //$NON-NLS-1$//$NON-NLS-2$
                }
            }
        }
        return convertedURI;
    }

    /**
     * Return the ns header field with prefixes.
     *
     * @return the ns header field with prefixes
     */
    private JsonElement createNsHeader() {
        JsonObject jsonObject = new JsonObject();
        EPackage noNamespacePackage = this.helper.getNoNamespacePackage();
        EPackage[] packages = this.helper.packages();

        for (EPackage ePackage : packages) {
            if (ePackage != noNamespacePackage) {
                String nsURI = ePackage.getNsURI();
                if (this.extendedMetaData != null) {
                    nsURI = this.extendedMetaData.getNamespace(ePackage);
                }
                if (nsURI != null) {
                    List<String> nsPrefixes = this.helper.getPrefixes(ePackage);
                    for (String nsPrefix : nsPrefixes) {
                        jsonObject.add(nsPrefix, new JsonPrimitive(nsURI));
                    }
                }
            }
        }
        return jsonObject;
    }

    /**
     * Return the Json header field with version and encoding.
     *
     * @return the Json header field
     */
    private JsonElement createJsonHeader() {
        JsonObject jsonObject = new JsonObject();

        Object versionNumber = this.options.get(JsonResource.OPTION_VERSION);
        if (versionNumber == null) {
            versionNumber = JsonResource.VERSION_1_0;
        }
        jsonObject.add(JsonResource.OPTION_VERSION, new JsonPrimitive(versionNumber.toString()));

        Object encoding = this.options.get(JsonResource.OPTION_ENCODING);
        if (encoding == null) {
            encoding = JsonResource.ENCODING_UTF_8;
        }

        jsonObject.add(JsonResource.OPTION_ENCODING, new JsonPrimitive(encoding.toString()));

        return jsonObject;
    }

    /**
     * Serialize all the structural features of the given object in a JsonObject.
     *
     * @param eObject
     *            The EObject to serialize
     * @return A JsonObject containing all the properties of the given object
     */
    private JsonObject serializeEAllStructuralFeatures(EObject eObject) {
        JsonObject properties = new JsonObject();
        EClass eClass = eObject.eClass();
        EList<EStructuralFeature> eAllStructuralFeatures = eClass.getEAllStructuralFeatures();

        for (EStructuralFeature eStructuralFeature : eAllStructuralFeatures) {
            if (this.shouldSerialize(eObject, eStructuralFeature)) {
                JsonElement value = null;
                if (eStructuralFeature instanceof EAttribute) {
                    EAttribute eAttribute = (EAttribute) eStructuralFeature;
                    value = this.serializeEAttribute(eObject, eAttribute);
                } else if (eStructuralFeature instanceof EReference) {
                    EReference eReference = (EReference) eStructuralFeature;
                    value = this.serializeEReference(eObject, eReference);
                }

                if (value != null) {
                    String featureName = this.helper.getQName(eStructuralFeature);
                    properties.add(featureName, value);
                }
            }
        }

        return properties;
    }

    /**
     * Determine if the structural feature should be serialized.
     *
     * @param eObject
     *            the EObject
     * @param eStructuralFeature
     *            the EStructuralFeature to serialize
     * @return if the structural feature will be serialized
     */
    private boolean shouldSerialize(EObject eObject, EStructuralFeature eStructuralFeature) {
        boolean shouldGenerate = true;

        // isTransient
        Object saveTransientFeatures = this.options.get(JsonResource.OPTION_SAVE_TRANSIENT_FEATURES);
        if (shouldGenerate && eStructuralFeature.isTransient() && !(saveTransientFeatures instanceof Boolean && ((Boolean) saveTransientFeatures).booleanValue())) {
            shouldGenerate = false;
        }

        // isDerived
        Object saveDerivedFeatures = this.options.get(JsonResource.OPTION_SAVE_DERIVED_FEATURES);
        if (shouldGenerate && eStructuralFeature.isDerived() && !(saveDerivedFeatures instanceof Boolean && ((Boolean) saveDerivedFeatures).booleanValue())) {
            shouldGenerate = false;
        }

        // EIsSet
        Object saveUnsettedFeatures = this.options.get(JsonResource.OPTION_SAVE_UNSETTED_FEATURES);
        if (shouldGenerate && !eObject.eIsSet(eStructuralFeature) && !(saveUnsettedFeatures instanceof Boolean && ((Boolean) saveUnsettedFeatures).booleanValue())) {
            shouldGenerate = false;
        }

        // isOpposite
        if (shouldGenerate && eStructuralFeature instanceof EReference) {
            EReference eReference = (EReference) eStructuralFeature;
            EReference oppositeEReference = eReference.getEOpposite();
            if (oppositeEReference != null && oppositeEReference.isContainment()) {
                shouldGenerate = false;
            }
        }

        Object eStructuralFeatureFilter = this.options.get(JsonResource.OPTION_ESTRUCTURAL_FEATURES_FILTER);
        if (eStructuralFeatureFilter instanceof JsonResource.EStructuralFeaturesFilter) {
            JsonResource.EStructuralFeaturesFilter filter = (EStructuralFeaturesFilter) eStructuralFeatureFilter;
            shouldGenerate = filter.shouldSave(eObject, eStructuralFeature);
        }

        return shouldGenerate;
    }

    /**
     * Serialize an EAttribute into a JsonElement.
     *
     * @param eObject
     *            The EObject
     * @param eAttribute
     *            The EAttribute to serialize
     * @return The JsonElement representing the EAttribute
     */
    private JsonElement serializeEAttribute(EObject eObject, EAttribute eAttribute) {
        JsonElement value = null;
        EClassifier eType = eAttribute.getEType();

        if (EcorePackage.eINSTANCE.getEString().equals(eType)) {
            value = this.serializeEStringEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEBoolean().equals(eType)) {
            value = this.serializeEBooleanEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEBooleanObject().equals(eType)) {
            value = this.serializeEBooleanEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEInt().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEIntegerObject().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEBigDecimal().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEBigInteger().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEByte().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEByteArray().equals(eType)) {
            value = this.serializeEByteArrayEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEByteObject().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEChar().equals(eType)) {
            value = this.serializeEStringEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getECharacterObject().equals(eType)) {
            value = this.serializeEStringEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEDate().equals(eType)) {
            value = this.serializeEDateEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEDouble().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEDoubleObject().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEFloat().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEFloatObject().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getELong().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getELongObject().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEShort().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (EcorePackage.eINSTANCE.getEShortObject().equals(eType)) {
            value = this.serializeENumberEAttribute(eObject, eAttribute);
        } else if (eType instanceof EEnum) {
            value = this.serializeEEnumEAttribute(eObject, eAttribute);
        } else if (eType instanceof EDataType) {
            value = this.serializeEDataType(eObject, eAttribute);
        } else {
            throw new InvalidParameterException();
        }

        return value;
    }

    /**
     * Returns the JsonElement representing an EDataType.
     *
     * @param eObject
     *            The EObject
     * @param eAttribute
     *            The EDataType
     * @return The JsonElement representing an EDataType
     */
    private JsonElement serializeEDataType(EObject eObject, EAttribute eAttribute) {
        JsonElement jsonElement = null;

        Object value = this.helper.getValue(eObject, eAttribute);
        if (eAttribute.isMany()) {
            JsonArray jsonArray = new JsonArray();
            if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                EFactory eFactoryInstance = eObject.eClass().getEPackage().getEFactoryInstance();
                for (Object object : collection) {
                    jsonArray.add(new JsonPrimitive(eFactoryInstance.convertToString(eAttribute.getEAttributeType(), object)));
                }
            }
            jsonElement = jsonArray;
        } else {
            EFactory eFactoryInstance = eAttribute.getEType().getEPackage().getEFactoryInstance();
            String stringValue = eFactoryInstance.convertToString(eAttribute.getEAttributeType(), value);

            if (stringValue == null) {
                stringValue = ""; //$NON-NLS-1$
            }
            jsonElement = new JsonPrimitive(stringValue);
        }

        return jsonElement;
    }

    /**
     * Returns the JsonElement representing an EByteArray.
     *
     * @param eObject
     *            The EObject
     * @param eAttribute
     *            The EByteArray
     * @return The JsonElement representing an EByteArray
     */
    private JsonElement serializeEByteArrayEAttribute(EObject eObject, EAttribute eAttribute) {
        JsonElement jsonElement = null;

        Object value = this.helper.getValue(eObject, eAttribute);
        if (eAttribute.isMany()) {
            JsonArray jsonArray = new JsonArray();
            if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                for (Object object : collection) {
                    if (object instanceof byte[]) {
                        jsonArray.add(new JsonPrimitive(DatatypeConverter.printHexBinary((byte[]) object)));
                    }
                }
            }
            jsonElement = jsonArray;
        } else {
            if (value instanceof byte[]) {
                jsonElement = new JsonPrimitive(DatatypeConverter.printHexBinary((byte[]) value));
            }
        }

        return jsonElement;
    }

    /**
     * Returns the JsonElement representing an EDate.
     *
     * @param eObject
     *            The EObject
     * @param eAttribute
     *            The EDate EAttribute
     * @return The JsonElement representing an EDate
     */
    private JsonElement serializeEDateEAttribute(EObject eObject, EAttribute eAttribute) {
        JsonElement jsonElement = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"); //$NON-NLS-1$
        Object value = this.helper.getValue(eObject, eAttribute);
        if (eAttribute.isMany()) {
            JsonArray jsonArray = new JsonArray();
            if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                for (Object object : collection) {
                    if (object instanceof Date) {
                        jsonArray.add(new JsonPrimitive(formatter.format((Date) object)));
                    }
                }
            }
            jsonElement = jsonArray;
        } else {
            if (value instanceof Date) {
                jsonElement = new JsonPrimitive(formatter.format((Date) value));
            }
        }

        return jsonElement;
    }

    /**
     * Returns the JsonElement representing an EEnum.
     *
     * @param eObject
     *            The EObject
     * @param eAttribute
     *            The EEnum
     * @return the JsonElement representing an EEnum
     */
    private JsonElement serializeEEnumEAttribute(EObject eObject, EAttribute eAttribute) {
        JsonElement jsonElement = null;

        Object value = this.helper.getValue(eObject, eAttribute);
        if (eAttribute.isMany()) {
            JsonArray jsonArray = new JsonArray();
            if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                for (Object object : collection) {
                    if (object instanceof EEnumLiteral) {
                        EEnumLiteral enumeration = (EEnumLiteral) object;
                        jsonArray.add(new JsonPrimitive(enumeration.toString()));
                    }
                }
            }
            jsonElement = jsonArray;
        } else {
            if (value instanceof EEnumLiteral) {
                EEnumLiteral enumeration = (EEnumLiteral) value;
                jsonElement = new JsonPrimitive(enumeration.toString());
            } else {
                jsonElement = this.serializeEDataType(eObject, eAttribute);
            }
        }
        return jsonElement;
    }

    /**
     * Returns the JsonElement representing an EString.
     *
     * @param eObject
     *            The EObject
     * @param eAttribute
     *            The EString EAttribute
     * @return The JsonPrimitive representing an EString
     */
    private JsonElement serializeEStringEAttribute(EObject eObject, EAttribute eAttribute) {
        JsonElement jsonElement = null;

        Object value = this.helper.getValue(eObject, eAttribute);
        if (eAttribute.isMany()) {
            JsonArray jsonArray = new JsonArray();
            if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                for (Object object : collection) {
                    if (object instanceof String) {
                        jsonArray.add(new JsonPrimitive((String) object));
                    } else if (object instanceof Character) {
                        jsonArray.add(new JsonPrimitive(object.toString()));
                    }
                }
            }
            jsonElement = jsonArray;
        } else {
            if (value instanceof String) {
                jsonElement = new JsonPrimitive((String) value);
            } else if (value instanceof Character) {
                jsonElement = new JsonPrimitive(value.toString());
            }
        }

        return jsonElement;
    }

    /**
     * Returns the JsonElement representing an EBoolean.
     *
     * @param eObject
     *            The EObject
     * @param eAttribute
     *            The EString EAttribute
     * @return The JsonPrimitive representing an EBoolean
     */
    private JsonElement serializeEBooleanEAttribute(EObject eObject, EAttribute eAttribute) {
        JsonElement jsonElement = null;

        Object value = this.helper.getValue(eObject, eAttribute);
        if (eAttribute.isMany()) {
            JsonArray jsonArray = new JsonArray();
            if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                for (Object object : collection) {
                    if (object instanceof Boolean) {
                        jsonArray.add(new JsonPrimitive((Boolean) object));
                    }
                }
            }
            jsonElement = jsonArray;
        } else {
            if (value instanceof Boolean) {
                jsonElement = new JsonPrimitive((Boolean) value);
            }
        }
        return jsonElement;
    }

    /**
     * Returns the JsonElement representing an ENumber.
     *
     * @param eObject
     *            The EObject
     * @param eAttribute
     *            The EString EAttribute
     * @return The JsonPrimitive representing an ENumber
     */
    private JsonElement serializeENumberEAttribute(EObject eObject, EAttribute eAttribute) {
        JsonElement jsonElement = null;

        Object value = this.helper.getValue(eObject, eAttribute);
        if (eAttribute.isMany()) {
            JsonArray jsonArray = new JsonArray();
            if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                for (Object object : collection) {
                    if (object instanceof Number) {
                        jsonArray.add(new JsonPrimitive((Number) object));
                    }
                }
                jsonElement = jsonArray;
            }
        } else {
            if (value instanceof Number) {
                jsonElement = new JsonPrimitive((Number) value);
            }
        }

        return jsonElement;
    }

    /**
     * Serialize an EReference into a JsonElement.
     *
     * @param eObject
     *            The EObject
     * @param eReference
     *            The EReference to serialize
     * @return The JsonElement representing the EReference
     */
    private JsonElement serializeEReference(EObject eObject, EReference eReference) {
        JsonElement value = null;
        if (eReference.isContainment()) {
            if (eReference.isMany()) {
                value = this.serializeMultipleContainmentEReference(eObject, eReference);
            } else {
                value = this.serializeSingleContainmentEReference(eObject, eReference);
            }
        } else {
            if (eReference.isMany()) {
                value = this.serializeMultipleNonContainmentEReference(eObject, eReference);
            } else {
                value = this.serializeSingleNonContainmentEReference(eObject, eReference);
            }
        }
        return value;
    }

    /**
     * Return a JsonElement representing a single containment reference value.
     *
     * @param eObject
     *            The EObject
     * @param eReference
     *            The EReference
     * @return A JsonElement with the property IGsonConstants.ECLASS and the serialized EObject as a value
     */
    private JsonElement serializeSingleContainmentEReference(EObject eObject, EReference eReference) {
        JsonElement jsonElement = null;

        Object referenceValue = this.helper.getValue(eObject, eReference);
        if (referenceValue instanceof EObject) {
            if (referenceValue instanceof BasicEObjectImpl && ((BasicEObjectImpl) referenceValue).eDirectResource() != null) {
                jsonElement = new JsonPrimitive(this.removeFragmentSeparator(this.helper.deresolve(EcoreUtil.getURI((EObject) referenceValue)).toString()));
            } else if (((EObject) referenceValue).eResource() != eObject.eResource()) {
                jsonElement = new JsonPrimitive(this.removeFragmentSeparator(this.helper.deresolve(EcoreUtil.getURI((EObject) referenceValue)).toString()));
            } else {
                jsonElement = this.createData((EObject) referenceValue);
            }
        }
        return jsonElement;
    }

    /**
     * Return a JsonElement representing a multiple containment reference value.
     *
     * @param eObject
     *            The EObject
     * @param eReference
     *            The EReference
     * @return A JsonArray containing several JsonObjects with the property IGsonConstants.ECLASS and the serialized
     *         EObject as a value.
     */
    private JsonElement serializeMultipleContainmentEReference(EObject eObject, EReference eReference) {
        JsonElement jsonElement = null;
        Object referenceValue = this.helper.getValue(eObject, eReference);
        if (referenceValue instanceof Iterable<?>) {
            JsonArray jsonArray = new JsonArray();
            Iterable<?> iterable = (Iterable<?>) referenceValue;
            for (Object object : iterable) {
                if (object instanceof EObject) {
                    jsonArray.add(this.createData((EObject) object));
                }
            }
            jsonElement = jsonArray;
        }
        return jsonElement;
    }

    /**
     * Returns a JsonElement representing a multiple non containment reference value.
     *
     * @param eObject
     *            The EObject
     * @param eReference
     *            The EReference
     * @return A JsonArray containing several JsonObjects with the property IGsonConstants.REF and as the value, the URI
     *         of the object.
     */
    @SuppressWarnings("unchecked")
    private JsonElement serializeMultipleNonContainmentEReference(EObject eObject, EReference eReference) {
        JsonElement jsonElement = null;
        JsonArray jsonArray = new JsonArray();
        Object referenceValue = this.helper.getValue(eObject, eReference);
        for (EObject value : (InternalEList<? extends EObject>) referenceValue) {
            switch (this.docKindMany(eObject, eReference)) {
            case SAME_DOC:
                String id = this.helper.getIDREF(value);
                id = this.removeFragmentSeparator(id);
                if (!id.isEmpty()) {
                    jsonArray.add(new JsonPrimitive(id));
                }
                break;
            case CROSS_DOC:
                if (value != null) {
                    jsonArray.add(new JsonPrimitive(this.saveHref(value, null)));
                }
                break;
            default:
            }
        }

        jsonElement = jsonArray;
        return jsonElement;
    }

    /**
     * Return where given ERference of the given EObject are. If at least one reference is in an other resource, all
     * references are treated as references to other resources.
     *
     * @param eObject
     *            the given EObject
     * @param eReference
     *            the given ERference
     * @return where given ERference of the given EObject are
     */
    private int docKindMany(EObject eObject, EReference eReference) {
        int referenceType = GsonEObjectSerializer.SAME_DOC;
        @SuppressWarnings("unchecked")
        InternalEList<? extends InternalEObject> internalEList = (InternalEList<? extends InternalEObject>) this.helper.getValue(eObject, eReference);

        if (internalEList.isEmpty()) {
            referenceType = GsonEObjectSerializer.SKIP;
        }

        Iterator<? extends InternalEObject> it = internalEList.iterator();
        while (referenceType != GsonEObjectSerializer.SKIP && referenceType != GsonEObjectSerializer.CROSS_DOC && it.hasNext()) {
            InternalEObject internalEObject = it.next();
            if (internalEObject.eIsProxy()) {
                referenceType = GsonEObjectSerializer.CROSS_DOC;
            } else {
                Resource resource = internalEObject.eResource();
                if (resource != this.helper.getResource() && resource != null) {
                    referenceType = GsonEObjectSerializer.CROSS_DOC;
                }
            }
        }
        return referenceType;
    }

    /**
     * Returns a JsonElement representing a single non containment reference value.
     *
     * @param eObject
     *            The EObject
     * @param eReference
     *            The EReference
     * @return A JsonObject with the property IGsonConstants.REF and as the value, the URI of the object.
     */
    private JsonElement serializeSingleNonContainmentEReference(EObject eObject, EReference eReference) {
        JsonElement jsonElement = null;
        String value = ""; //$NON-NLS-1$
        Object referenceValue = this.helper.getValue(eObject, eReference);
        switch (this.docKindSingle(eObject, eReference)) {
        case SAME_DOC:
            // same document
            value = this.helper.getIDREF((EObject) referenceValue);
            value = this.removeFragmentSeparator(value);
            break;
        case CROSS_DOC:
            // cross document
            EObject object = (EObject) this.helper.getValue(eObject, eReference);
            if (object != null) {
                value = this.saveHref(object, eReference);
            }
            break;
        default:
        }
        jsonElement = new JsonPrimitive(value);

        return jsonElement;
    }

    /**
     * Removes the fragment separator if the given URI is an URI fragment starting with it.
     *
     * @param uri
     *            the given URI
     * @return the cleaned URI
     */
    private String removeFragmentSeparator(String uri) {
        String value = uri;
        if (value.startsWith(IGsonConstants.FRAGMENT_SEPARATOR)) {
            value = value.substring(1);
        }
        return value;
    }

    /**
     * Return the given non containment reference URI of the given object.
     *
     * @param object
     *            the given EObject
     * @param eReference
     *            the given EReference
     * @return the given non containment reference URI of the given object
     */
    private String saveHref(EObject object, EReference eReference) {
        String value = ""; //$NON-NLS-1$
        String href = this.helper.getHREF(object);
        if (href != null) {
            href = this.convertURI(href);
            EClass eClass = object.eClass();
            if (this.shouldSaveType(eClass)) {
                value += this.helper.getQName(eClass) + " "; //$NON-NLS-1$
            }
            // TODO: element Handler if statement : look at XMLSaveImpl line 2308
            value += href;
        }
        return value;
    }

    /**
     * Test if we should save the type of the given EClass.
     *
     * @param eClass
     *            the EClass
     * @return <code>true</code> if we should save the type of the EClass, <code>false</code> otherwise
     */
    private boolean shouldSaveType(EClass eClass) {
        // TODO: look at XMLSaveImpl line 2301 for other meaning to saveType
        Boolean shouldSaveType = (Boolean) this.options.get(JsonResource.OPTION_DISPLAY_DYNAMIC_INSTANCES);
        if (shouldSaveType == null) {
            shouldSaveType = Boolean.FALSE;
        }
        return shouldSaveType.booleanValue();
    }

    /**
     * Return where the given ERference of the given EObject is.
     *
     * @param eObject
     *            the given EObject
     * @param eReference
     *            the given EReference
     * @return where the given ERference of the given EObject is
     */
    private int docKindSingle(EObject eObject, EReference eReference) {
        InternalEObject value = (InternalEObject) this.helper.getValue(eObject, eReference);
        int typeOfDocument = GsonEObjectSerializer.SKIP;
        if (value != null && value.eIsProxy()) {
            typeOfDocument = GsonEObjectSerializer.CROSS_DOC;
        } else if (value != null) {
            Resource res = value.eResource();
            if (res == this.helper.getResource() || res == null) {
                typeOfDocument = GsonEObjectSerializer.SAME_DOC;
            } else {
                typeOfDocument = GsonEObjectSerializer.CROSS_DOC;
            }
        }
        return typeOfDocument;
    }

    /**
     * Return the the URI fragment if the pointed resource URI schema is the same of the Resource URI schema. Return the
     * all URI otherwise.
     *
     * @param pointedResourceUri
     *            the pointed resource URI
     * @return the right URI
     */
    private String getReferenceUri(URI pointedResourceUri) {
        String referenceUriString = pointedResourceUri.toString();
        int indexReference = referenceUriString.indexOf(IGsonConstants.FRAGMENT_SEPARATOR);
        if (indexReference > 0) {
            String baseUri = referenceUriString.substring(0, indexReference);
            String fragment = pointedResourceUri.fragment();
            if (baseUri.equals(this.helper.getResourceURI().toString())) {
                referenceUriString = fragment;
            }
        }
        return referenceUriString;
    }

    /**
     * Returns the danglingHREFException.
     *
     * @return The danglingHREFException
     */
    public DanglingHREFException getDanglingHREFException() {
        return this.helper.getDanglingHREFException();
    }

}
