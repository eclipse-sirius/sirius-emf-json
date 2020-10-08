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

/**
 * The constants used during the serialization and deserialization.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public interface IGsonConstants {
    /**
     * The eClass of the EObject.
     */
    String ECLASS = "eClass"; //$NON-NLS-1$

    /**
     * The URI of the EObject.
     */
    String URI = "uri"; //$NON-NLS-1$

    /**
     * The properties of the EObject.
     */
    String PROPERTIES = "properties"; //$NON-NLS-1$

    /**
     * The reference to an EObject.
     */
    String REF = "$ref"; //$NON-NLS-1$

    /**
     * The json attribute used for header.
     */
    String JSON = "json"; //$NON-NLS-1$

    /**
     * The ns attribute used for header.
     */
    String NS = "ns"; //$NON-NLS-1$

    /**
     * The schemaLocation attribute used for header.
     */
    String SCHEMA_LOCATION = "schemaLocation"; //$NON-NLS-1$

    /**
     * The content attribute.
     */
    String CONTENT = "content"; //$NON-NLS-1$

    /**
     * The data attribute.
     */
    String DATA = "data"; //$NON-NLS-1$

    /**
     * The Json prefix separator.
     */
    String PREFIX_SEPARATOR = ":"; //$NON-NLS-1$

    /**
     * The EAnnotation Reference attribute.
     */
    String REFERENCES = "references"; //$NON-NLS-1$

    /**
     * The key details EAnnotation value.
     */
    String KEY = "key"; //$NON-NLS-1$

    /**
     * The value details EAnnotation value.
     */
    String VALUE = "value"; //$NON-NLS-1$

    /**
     * The EAnnotation attribute source value.
     */
    String SOURCE = "source"; //$NON-NLS-1$

    /**
     * The EAnnotation attribute details value.
     */
    String DETAILS = "details"; //$NON-NLS-1$

    /**
     * The EObject and its subtypes name value.
     */
    String NAME = "name"; //$NON-NLS-1$

    /**
     * The EOperation EParameter values.
     */
    String EPARAMETERS = "eParameters"; //$NON-NLS-1$

    /**
     * The EParameter EType value.
     */
    String ETYPE = "eType"; //$NON-NLS-1$

    /**
     * The EClassifier attribute value.
     */
    String ECLASSIFIER = "eClassifier"; //$NON-NLS-1$

    /**
     * The ETypeArguments attribute value.
     */
    String ETYPEARGUMENTS = "eTypeArguments"; //$NON-NLS-1$

    /**
     * The EGenericType attribute value.
     */
    String EGENERICTYPE = "eGenericType"; //$NON-NLS-1$

    /**
     * The EExceptions attribute value.
     */
    String EEXCEPTIONS = "eExceptions"; //$NON-NLS-1$

    /**
     * The EEXceptions attribute value when at least one exception is generic.
     */
    String EGENERICEXCEPTION = "eGenericExceptions"; //$NON-NLS-1$

    /**
     * The ETypeParameter attribute value.
     */
    String ETYPEPARAMETER = "eTypeParameter"; //$NON-NLS-1$

    /**
     * The ETypeParameter attribute value.
     */
    String ETYPEPARAMETER_ARRAY = "eTypeParameters"; //$NON-NLS-1$

    /**
     * The Bounds ETypeParameter value.
     */
    String EBOUNDS = "eBounds"; //$NON-NLS-1$

    /**
     * The Literal array JSON field.
     */
    String LITERAL_ARRAY = "eLiterals"; //$NON-NLS-1$

    /**
     * The Literal JSON field.
     */
    String LITERAL = "literal"; //$NON-NLS-1$

    /**
     * The URI fragment separator.
     */
    String FRAGMENT_SEPARATOR = "#"; //$NON-NLS-1$

    /**
     * The prefixes separator. Use when there is no prefix and we force its uses. And when the same prefix is used, we
     * use this separator to add a number to the prefix.
     */
    String CONSTANT_SEPARATOR = "_"; //$NON-NLS-1$

    /**
     * The keyword for EAnnotations.
     */
    String EANNOTATIONS = "eAnnotations"; //$NON-NLS-1$

    /**
     * The keyword for EEnum literals.
     */
    String ELITERALS = "eLiterals"; //$NON-NLS-1$

    /**
     * The keyword for EOperations.
     */
    String EOPERATIONS = "eOperations"; //$NON-NLS-1$

    /**
     * The keyword for generic super type.
     */
    String EGENERICSUPERTYPES = "eGenericSuperTypes"; //$NON-NLS-1$

    /**
     * The keyword for sub packages.
     */
    Object ESUBPACKAGES = "eSubpackages"; //$NON-NLS-1$

    /**
     * The ID for the EObject.
     */
    String ID = "id"; //$NON-NLS-1$

}
