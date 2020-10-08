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
package org.eclipse.sirius.emfjson.resource;

import com.google.gson.JsonElement;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * The Json resource.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public interface JsonResource extends Resource {

    /**
     * version 1.0.
     */
    String VERSION_1_0 = "1.0"; //$NON-NLS-1$

    /**
     * The serialization version number.
     */
    String OPTION_VERSION = "version"; //$NON-NLS-1$

    /**
     * The encoding of the resource.
     */
    String OPTION_ENCODING = "encoding"; //$NON-NLS-1$

    /**
     * UTF-8.
     */
    String ENCODING_UTF_8 = "utf-8"; //$NON-NLS-1$

    /**
     * Give number of space indent to activate pretty printing @see {@link JsonResource#INDENT_2_SPACES}
     * {@link JsonResource#INDENT_4_SPACES}. By default, the content serialized will be "compacted" 0 space.
     */
    String OPTION_PRETTY_PRINTING_INDENT = "prettyPrinting"; //$NON-NLS-1$

    /**
     * Defined the number of spaces to 2. @see {@link JsonResource#OPTION_PRETTY_PRINTING_INDENT}
     */
    String INDENT_2_SPACES = "  "; //$NON-NLS-1$

    /**
     * Defined the number of space to 4. @see {@link JsonResource#OPTION_PRETTY_PRINTING_INDENT}
     */
    String INDENT_4_SPACES = "    "; //$NON-NLS-1$

    /**
     * Indicates if the structural features of the EObject currently serialized should be serialized even if they have a
     * default value. <code>true</code> to force the serialization of features with a default value, <code>false</code>
     * otherwise (default: <code>false</code>).
     */
    String OPTION_SAVE_UNSETTED_FEATURES = "saveUnsettedFeatures"; //$NON-NLS-1$

    /**
     * Specify an {@link EStructuralFeaturesFilter} to use during the serialization/deserialization to force the list of
     * the {@link EStructuralFeature} to take into account.
     */
    String OPTION_ESTRUCTURAL_FEATURES_FILTER = "eStructuralFeaturesFilter"; //$NON-NLS-1$

    /**
     * Interface to let users implements and provide their own filter.
     *
     * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
     */
    interface EStructuralFeaturesFilter {
        /**
         * Determine if the structural feature should be saved.
         *
         * @param eObject
         *            the EObject
         * @param eStructuralFeature
         *            the EStructuralFeature to save
         * @return if the structural feature will be saved
         */
        boolean shouldSave(EObject eObject, EStructuralFeature eStructuralFeature);

        /**
         * Determine if the structural feature should be loaded.
         *
         * @param eObject
         *            the EObject
         * @param eStructuralFeature
         *            the EStructuralFeature to load
         * @return if the structural feature will be loaded
         */
        boolean shouldLoad(EObject eObject, EStructuralFeature eStructuralFeature);
    }

    /**
     * Indicates if the structural feature of the EObject currently serialized, should be serialized even if they are
     * derived. <code>true</code> to force the serialization of a derived feature, <code>false</code> otherwise (default
     * : <code>false</code>).
     */
    String OPTION_SAVE_DERIVED_FEATURES = "saveDerivedFeatures"; //$NON-NLS-1$

    /**
     * Indicate if the structural feature of the EObject currently serialized, should be serialized even if they are
     * transient. <code>true</code> to force the serialization of a transient feature, <code>false</code> otherwise
     * (default : <code>false</code>).
     */
    String OPTION_SAVE_TRANSIENT_FEATURES = "saveTransientFeatures"; //$NON-NLS-1$

    /**
     * If this option is set to <code>true</code>, the metaType of the type is added('nsPrefix:metaType'). (default :
     * <code>false</code>).
     */
    String OPTION_DISPLAY_DYNAMIC_INSTANCES = "displayDynamicInstances"; //$NON-NLS-1$

    /**
     * let users to provide a custom helper for serialization/deserialization.
     */
    String OPTION_CUSTOM_HELPER = "customHelper"; //$NON-NLS-1$

    /**
     * Let users to provide a custom ExtendedMetaData behavior by provide a custom class. It is also possible to set the
     * value to <code>true</code>. In this case, the behavior will be the EMF one.
     *
     * @see org.eclipse.emf.ecore.util.BasicExtendedMetaData
     */
    String OPTION_EXTENDED_META_DATA = "extendedMetaData"; //$NON-NLS-1$

    /**
     * This option allow to specify an EClass for the implementation of AnySimpletype.
     */
    String OPTION_ANY_SIMPLE_TYPE = "anySimpleType"; //$NON-NLS-1$

    /**
     * Produce an schemaLocation/noNamespaceSchemaLocation in the saved result to encode the name of the Java interface
     * that declares the eINSTANCE of the EPackage implementation for those cases where OPTION_SCHEMA_LOCATION would not
     * produce a physical location URI.
     */
    String OPTION_SCHEMA_LOCATION_IMPLEMENTATION = "schemaLocationImplementation"; //$NON-NLS-1$

    /**
     * Produce an schemaLocation/noNamespaceSchemaLocation in the saved result.
     */
    String OPTION_SCHEMA_LOCATION = "schemaLocation"; //$NON-NLS-1$

    /**
     * Force to add a prefix in case where it is needed but it is empty. By default this option is disable.
     */
    String OPTION_FORCE_PREFIX_ON_EMPTY_ONE = "forcePrefixOnEmptyOne"; //$NON-NLS-1$

    /**
     * Write only the subtree starting at the specified list of EObjects, which must be objects contained by the
     * resource.
     */
    String OPTION_ROOT_OBJECTS = "optionRootObjects"; //$NON-NLS-1$

    /**
     * Specify a {@link URIHandler} value that will be used to control URIs are {@link URI#resolve(URI) resolved} during
     * load and {@link URI#deresolve(URI) deresolved} during save.
     *
     * @see URI
     * @see URIHandler
     */
    String OPTION_URI_HANDLER = "URI_HANDLER"; //$NON-NLS-1$

    /**
     * An interface for URI Handler that is used to {@link URI#resolve(URI) resolve} and {@link URI#deresolve(URI)
     * deresolve} URIs.
     */
    interface URIHandler {

        /**
         * Sets base URI used by the handler. It will be called before load or save begins.
         *
         * @param uri
         *            the new base URI.
         */
        void setBaseURI(URI uri);

        /**
         * Returns the URI {@link URI#resolve(URI) resolved} against the base URI.
         *
         * @param uri
         *            the URI to resolve
         * @return the URI resolved against the base URI
         * @see URI#resolve(URI)
         */
        URI resolve(URI uri);

        /**
         * Return the URI {@link URI#deresolve(URI) deresolved} against the base URI.
         *
         * @param uri
         *            the URI to deresolve.
         * @return the URI deresolved against the base URI.
         * @see URI#deresolve(URI)
         */
        URI deresolve(URI uri);
    }

    /**
     * A {@link ResourceEntityHandler} value that will be used during load to record entity definitions and during save
     * to encode cross document reference URIs as entities; the default value is null, in which case entities will not
     * be defined at all.
     */
    String OPTION_RESOURCE_ENTITY_HANDLER = "RESOURCE_ENTITY_HANDLER"; //$NON-NLS-1$

    /**
     * An interface for a resource entity handler. It is used during load to record entities and is used during save to
     * assign entity names to values representing cross resource URI references.
     */
    interface ResourceEntityHandler {
        /**
         * Resets the state of the entity handler when a resource is first loaded or is reloaded.
         */
        void reset();

        /**
         * Records the entity name to entity value mapping.
         *
         * @param entityName
         *            the name of the entity.
         * @param entityValue
         *            the associated value of the entity.
         */
        void handleEntity(String entityName, String entityValue);

        /**
         * Returns the name associated with the entity value; a new name will be generated if there is not yet a name
         * associated with the entity value.
         *
         * @param entityValue
         *            the entity value for which a named entity is needed.
         * @return the name associated with the entity value.
         */
        String getEntityName(String entityValue);

        /**
         * Returns the map of entity names to entity values to be recorded in the document during save.
         *
         * @return the map of entity names to entity values to be recorded in the document during save.
         */
        Map<String, String> getNameToValueMap();
    }

    /**
     * A ResourceHandler value that can be registred to receive call backs for loading from an input stream or saving to
     * an outupt stream.
     *
     * @see org.eclipse.sirius.emfjson.resource.JsonResource.ResourceHandler
     */
    String OPTION_RESOURCE_HANDLER = "RESOURCE_HANDLER"; //$NON-NLS-1$

    /**
     * An interface for a resource handler that can be registred to receive call backs for loading from an input stream
     * or for saving to an output stream.
     *
     * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
     */
    interface ResourceHandler {

        /**
         * Called before loading begins.
         *
         * @param resource
         *            the resource being loaded.
         * @param inputStream
         *            the stream being read
         * @param options
         *            the load options
         */
        void preLoad(JsonResource resource, InputStream inputStream, Map<?, ?> options);

        /**
         * Called after loading is done.
         *
         * @param resource
         *            the resource being loaded
         * @param inputStream
         *            the stream being read
         * @param options
         *            the loading options
         */
        void postLoad(JsonResource resource, InputStream inputStream, Map<?, ?> options);

        /**
         * Called before saving begins.
         *
         * @param resource
         *            the resource being saved
         * @param outputStream
         *            the stream being written
         * @param options
         *            the save options
         */
        void preSave(JsonResource resource, OutputStream outputStream, Map<?, ?> options);

        /**
         * Called after saving done.
         *
         * @param resource
         *            the resource being saved
         * @param outputStream
         *            the stream being written
         * @param options
         *            the save options
         */
        void postSave(JsonResource resource, OutputStream outputStream, Map<?, ?> options);

    }

    /**
     * A load option that specifies a {@link MissingPackageHandler missing package handler} instance that is used to as
     * a final opportunity to locate a package when all other mechanisms have failed.
     */
    String OPTION_MISSING_PACKAGE_HANDLER = "missingPackageHandler"; //$NON-NLS-1$

    /**
     * Handles missing packages as a last resort when all other mechanisms have failed.
     *
     * @see #OPTION_MISSING_PACKAGE_HANDLER
     */
    interface MissingPackageHandler {

        /**
         * Returns a package from the given {@link EPackage#getNsURI() nsURI}, <code>null</code> otherwise.
         *
         * @param nsURI
         *            the given nsURI
         * @return the found package or null
         */
        EPackage getEPackage(String nsURI);
    }

    /**
     * This option can be used to select the action to be taken during serialization when a referenced object is not in
     * a resource. One of the three values can be specified: - THROW - RECORD - DISCARD The default value is THROW
     */
    String OPTION_PROCESS_DANGLING_HREF = "PROCESS_DANGLING_HREF"; //$NON-NLS-1$

    /**
     * If this value, the default, is chosen and referenced objects without resources are encountered, the serializer
     * will discard the bad reference, save the rest of the model, and the throw an exception at the end.
     */
    String OPTION_PROCESS_DANGLING_HREF_THROW = "THROW"; //$NON-NLS-1$

    /**
     * For this value, the serializer will do the same, except it will add the exception to the resource's error list,
     * instead of throwing it.
     */
    String OPTION_PROCESS_DANGLING_HREF_RECORD = "RECORD"; //$NON-NLS-1$

    /**
     * For this value, it will silently discard the bad references without informing the application in any way.
     */
    String OPTION_PROCESS_DANGLING_HREF_DISCARD = "DISCARD"; //$NON-NLS-1$

    /**
     * An option to let the serialization and deserialization of meta-datas.
     */
    String OPTION_EOBJECT_HANDLER = "OPTION_EOBJECT_HANDLER"; //$NON-NLS-1$

    /**
     * The interface that can handle meta-data.
     */
    public interface IEObjectHandler {
        /**
         * This operation will be used to manipulate the content saved in the JsonElement after the serialization of the
         * EObject. It can be used to add, remove, modify the JsonElement created for the given EObject.
         *
         * @param jsonElement
         *            The JsonElement created by EMFJson
         * @param eObject
         *            The EObject to save
         * @return The JsonElement representing the given EObject
         */
        JsonElement processSerializedContent(JsonElement jsonElement, EObject eObject);

        /**
         * This operation will be used to manipulate the EObject created from the given JsonElement after its
         * deserialization. It can be used to modify in any way the EObject created by EMFJson.
         *
         * @param eObject
         *            The EObject deserialized by EMFJson
         * @param jsonElement
         *            The JsonElement representing the given EObject
         * @return The EObject deserialized
         */
        EObject processDeserializedContent(EObject eObject, JsonElement jsonElement);
    }

    /**
     * An option to serialize fragment references using the default behavior of XMIResource. For example, with CDO the
     * default value of fragment References is the ID of the InternalCDOObject. With this option set to
     * <code>true</code> the serialization of these fragments will be the same as XMIResource. The default value is
     * <code>false</code>
     */
    String OPTION_FORCE_DEFAULT_REFERENCE_SERIALIZATION = "OPTION_FORCE_DEFAULT_REFERENCE_SERIALIZATION"; //$NON-NLS-1$

    /**
     * An option to provide an {@link IDManager} to handle the ID of an {@link EObject} in a resource.
     */
    String OPTION_ID_MANAGER = "OPTION_ID_MANAGER"; //$NON-NLS-1$

    /**
     * Associate an ID to the {@link EObject}.
     *
     * @param eObject
     *            the object
     * @param id
     *            the id
     */
    void setID(EObject eObject, String id);
}
