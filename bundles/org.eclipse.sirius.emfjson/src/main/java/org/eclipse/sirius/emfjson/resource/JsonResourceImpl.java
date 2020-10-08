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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.sirius.emfjson.utils.GsonEObjectDeserializer;
import org.eclipse.sirius.emfjson.utils.GsonEObjectSerializer;

/**
 * An implementation of {@link org.eclipse.emf.ecore.resource.Resource} that reads and writes a json.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class JsonResourceImpl extends ResourceImpl implements JsonResource {

    /**
     * The map from id to {@link EObject}. It is used to store IDs when {@link IDManager} are used to handle id when an
     * object is added.
     */
    protected Map<String, EObject> idToEObjectMap = new HashMap<>();

    /**
     * The options.
     */
    private Map<Object, Object> resourceOptions = new HashMap<Object, Object>();

    /**
     * Use to know is an id is used to identified an EObject.
     */
    private boolean useID;

    /**
     * The constructor. <br/>
     * If an instance of {@link IDManager} is found in the resourceOptions, the usage of Id is possible. The ID is
     * provided by {@link IDManager#getOrCreateId(EObject)} and associated with the EObject with
     * {@link IDManager#setId(EObject, String)} every time an eObject is attached. The Id can be overridden with
     * {@link #setID(EObject, String)}. {@link #getEObjectByID(String)} will be based on the Id.
     *
     * @param uri
     *            The URI of the resource
     * @param resourceOptions
     *            the options
     */
    public JsonResourceImpl(URI uri, Map<?, ?> resourceOptions) {
        super(uri);
        if (resourceOptions != null) {
            this.resourceOptions.putAll(resourceOptions);

            Object idManagerObject = this.resourceOptions.get(JsonResource.OPTION_ID_MANAGER);
            if (idManagerObject instanceof IDManager) {
                this.useID = true;
            }
        }
    }

    /**
     * Serializes an EObject into its json representation.
     *
     * @param eObject
     *            the collection of EObject to convert
     * @param options
     *            a map of serialization options
     * @return the json representation of the given EObject
     */
    public static String toJson(EObject eObject, Map<Object, Object> options) {
        return JsonResourceImpl.toJson(Arrays.asList(eObject), options);
    }

    /**
     * Serializes a Resource into its json representation.
     *
     * @param resource
     *            the resource to serialize
     * @param options
     *            a map of serialization options
     * @return the json representation of the given Resource
     */
    public static String toJson(Resource resource, Map<Object, Object> options) {
        Map<Object, Object> loadOptions = new HashMap<Object, Object>();
        if (options != null) {
            loadOptions.putAll(options);
        }

        loadOptions.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, JsonResource.INDENT_2_SPACES);
        loadOptions.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);

        TypeToken<Collection<EObject>> typeToken = new TypeToken<Collection<EObject>>() {
            // do nothing
        };

        String json = ""; //$NON-NLS-1$
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter outputStreamWriter = null;
        JsonWriter jsonWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream, JsonResource.ENCODING_UTF_8);

            jsonWriter = new JsonWriter(outputStreamWriter);
            jsonWriter.setIndent(JsonResource.INDENT_2_SPACES);

            GsonEObjectSerializer serializer = new GsonEObjectSerializer(resource, loadOptions);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.registerTypeAdapter(typeToken.getType(), serializer).disableHtmlEscaping().create();

            gson.toJson(resource.getContents(), typeToken.getType(), jsonWriter);

            try {
                outputStreamWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            json = new String(byteArrayOutputStream.toByteArray(), JsonResource.ENCODING_UTF_8);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (jsonWriter != null) {
                try {
                    jsonWriter.close();
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return json;
    }

    /**
     * Serializes a collection of EObject into its json representation.
     *
     * @param eObjects
     *            the collection of EObject to convert
     * @param options
     *            a map of serialization options
     * @return the json representation of the given collection of EObject
     */
    public static String toJson(Collection<EObject> eObjects, Map<Object, Object> options) {
        Map<Object, Object> loadOptions = new HashMap<Object, Object>();
        if (options != null) {
            loadOptions.putAll(options);
        }

        loadOptions.put(JsonResource.OPTION_PRETTY_PRINTING_INDENT, JsonResource.INDENT_2_SPACES);
        loadOptions.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);

        TypeToken<Collection<EObject>> typeToken = new TypeToken<Collection<EObject>>() {
            // do nothing
        };

        String json = ""; //$NON-NLS-1$
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter outputStreamWriter = null;
        JsonWriter jsonWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream, JsonResource.ENCODING_UTF_8);

            jsonWriter = new JsonWriter(outputStreamWriter);
            jsonWriter.setIndent(JsonResource.INDENT_2_SPACES);

            GsonEObjectSerializer serializer = new GsonEObjectSerializer(null, loadOptions);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.registerTypeAdapter(typeToken.getType(), serializer).disableHtmlEscaping().create();

            gson.toJson(eObjects, typeToken.getType(), jsonWriter);

            try {
                outputStreamWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            json = new String(byteArrayOutputStream.toByteArray(), JsonResource.ENCODING_UTF_8);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (jsonWriter != null) {
                try {
                    jsonWriter.close();
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return json;
    }

    /**
     * Serializes a resource into its JsonTree representation.
     *
     * @param resource
     *            the resource to serialize.
     * @param options
     *            serialization's options
     * @return The Json tree representation of the serialized resource
     */
    public static JsonElement toJsonTree(Resource resource, Map<?, ?> options) {
        Map<Object, Object> loadOptions = new HashMap<Object, Object>();
        if (options != null) {
            loadOptions.putAll(options);
        }

        loadOptions.put(JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);

        TypeToken<Collection<EObject>> typeToken = new TypeToken<Collection<EObject>>() {
            // do nothing
        };

        GsonEObjectSerializer serializer = new GsonEObjectSerializer(resource, loadOptions);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.registerTypeAdapter(typeToken.getType(), serializer).disableHtmlEscaping().create();

        JsonElement jsonElement = gson.toJsonTree(resource.getContents(), typeToken.getType());

        return jsonElement;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.emf.ecore.resource.impl.ResourceImpl#doLoad(java.io.InputStream, java.util.Map)
     */
    @Override
    protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
        // super.doLoad(inputStream, options);
        Map<Object, Object> loadOptions = new HashMap<Object, Object>();
        loadOptions.putAll(this.resourceOptions);
        if (options != null) {
            loadOptions.putAll(options);
        }

        JsonResource.ResourceHandler handler = (JsonResource.ResourceHandler) loadOptions.get(JsonResource.OPTION_RESOURCE_HANDLER);

        Object encoding = loadOptions.get(JsonResource.OPTION_ENCODING);
        if (encoding == null) {
            encoding = JsonResource.ENCODING_UTF_8;
            loadOptions.put(JsonResource.OPTION_ENCODING, encoding);
        }

        if (handler != null) {
            handler.preLoad(this, inputStream, loadOptions);
        }
        TypeToken<EObject> typeToken = new TypeToken<EObject>() {
            // nothing
        };

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(EObject.class, new GsonEObjectDeserializer(this, loadOptions));
        Gson gson = gsonBuilder.disableHtmlEscaping().create();

        // Check if reader care of indent.
        JsonReader reader = null;

        try {
            reader = new JsonReader(new InputStreamReader(inputStream, encoding.toString()));

            gson.fromJson(reader, typeToken.getType());

            if (handler != null) {
                handler.postLoad(this, inputStream, loadOptions);
            }
        } catch (JsonIOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

    }

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.emf.ecore.resource.impl.ResourceImpl#doSave(java.io.OutputStream, java.util.Map)
     */
    @Override
    protected void doSave(OutputStream outputStream, Map<?, ?> options) throws IOException {
        Map<Object, Object> saveOptions = new HashMap<Object, Object>();
        saveOptions.putAll(this.resourceOptions);
        if (options != null) {
            saveOptions.putAll(options);
        }

        JsonResource.ResourceHandler handler = (ResourceHandler) saveOptions.get(JsonResource.OPTION_RESOURCE_HANDLER);

        if (handler != null) {
            handler.preSave(this, outputStream, saveOptions);
        }

        Object encoding = saveOptions.get(JsonResource.OPTION_ENCODING);
        if (encoding == null) {
            encoding = JsonResource.ENCODING_UTF_8;
            saveOptions.put(JsonResource.OPTION_ENCODING, encoding);
        }
        Object prettyPrintingIndent = saveOptions.get(JsonResource.OPTION_PRETTY_PRINTING_INDENT);
        if (prettyPrintingIndent == null) {
            prettyPrintingIndent = ""; //$NON-NLS-1$
        }

        TypeToken<EList<EObject>> typeToken = new TypeToken<EList<EObject>>() {
            // nothing
        };

        GsonEObjectSerializer objectSerializer = new GsonEObjectSerializer(this, saveOptions);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(typeToken.getType(), objectSerializer);
        Gson gson = gsonBuilder.disableHtmlEscaping().create();

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, encoding.toString());
        JsonWriter writer = new JsonWriter(outputStreamWriter);
        if (prettyPrintingIndent instanceof String) {
            writer.setIndent((String) prettyPrintingIndent);
        }
        EList<EObject> resourceContents = this.getContents();

        gson.toJson(resourceContents, typeToken.getType(), writer);

        if (handler != null) {
            handler.postSave(this, outputStream, saveOptions);
        }

        writer.close();
        outputStreamWriter.close();

        if (objectSerializer.getDanglingHREFException() != null) {
            throw new IOWrappedException(objectSerializer.getDanglingHREFException());
        }
    }

    @Override
    protected boolean isAttachedDetachedHelperRequired() {
        return this.useID;
    }

    /**
     * If the usage of ID has been activated, removes the entry from the {@link #idToEObjectMap} thanks
     * {@link #setID(EObject, String)} with a <code>null</code> value as ID.
     */
    @Override
    protected void detachedHelper(EObject eObject) {
        if (this.useID) {
            this.setID(eObject, null);
        }

        super.detachedHelper(eObject);
    }

    /**
     * If the usage of ID has been activated, creates an ID for the given eObject thanks to
     * {@link IDManager#getOrCreateId(EObject)}, then sets the ID thanks to {@link #setID(EObject, String)}.
     *
     * @param eObject
     *            The EObject to attache
     */
    @Override
    protected void attachedHelper(EObject eObject) {
        super.attachedHelper(eObject);

        if (this.useID) {
            Object idManagerObject = this.resourceOptions.get(JsonResource.OPTION_ID_MANAGER);
            if (idManagerObject instanceof IDManager) {
                IDManager idManager = (IDManager) idManagerObject;
                String id = idManager.getOrCreateId(eObject);
                this.setID(eObject, id);
            }
        }
    }

    /**
     * Sets the ID of the object if an {@link IDManager} has been provided in options thanks to
     * {@link JsonResource#OPTION_ID_MANAGER}.
     *
     * <p>
     * This will call {@link IDManager#setId(EObject, String)} and updates the internal map {@link #idToEObjectMap}.
     * </p>
     *
     * @param eObject
     *            the object.
     * @param id
     *            the object's ID.
     */
    @Override
    public void setID(EObject eObject, String id) {
        if (this.useID) {
            Object managerObject = this.resourceOptions.get(JsonResource.OPTION_ID_MANAGER);
            if (managerObject instanceof IDManager) {
                IDManager idManager = (IDManager) managerObject;

                idManager.findId(eObject).ifPresent(this.idToEObjectMap::remove);
                if (id != null) {
                    idManager.setId(eObject, id);
                    this.idToEObjectMap.put(id, eObject);
                }
            }
        }
    }

    @Override
    protected EObject getEObjectByID(String id) {
        if (this.useID) {
            EObject eObject = this.idToEObjectMap.get(id);
            if (eObject != null) {
                return eObject;
            }
        }

        return super.getEObjectByID(id);
    }

    /**
     * Does all the work of unloading the resource. Calls doUnload in ResourceImpl, calls
     * {@link IDManager#clearId(EObject)} for each {@link #idToEObjectMap} value, then clears {@link #idToEObjectMap}.
     */
    @Override
    protected void doUnload() {
        super.doUnload();

        if (this.idToEObjectMap != null) {
            Object managerObject = this.resourceOptions.get(JsonResource.OPTION_ID_MANAGER);
            if (managerObject instanceof IDManager) {
                IDManager idManager = (IDManager) managerObject;
                this.idToEObjectMap.values().stream().forEach(idManager::clearId);
            }
            this.idToEObjectMap.clear();
        }
    }
}
