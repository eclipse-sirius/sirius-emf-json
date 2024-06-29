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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;

/**
 * This object will be used to deserialized a resource.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class GsonResourceDeserializer implements JsonDeserializer<Resource> {
    /**
     * The options.
     */
    private Map<?, ?> options;

    /**
     * The constructor.
     *
     * @param options
     *            the options
     */
    public GsonResourceDeserializer(Map<?, ?> options) {
        this.options = options;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type,
     *      com.google.gson.JsonDeserializationContext)
     */
    @Override
    public Resource deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Resource resource = new JsonResourceImpl(URI.createURI(""), this.options); //$NON-NLS-1$
        GsonEObjectDeserializer deserializer = new GsonEObjectDeserializer(resource, this.options);
        deserializer.deserialize(json, typeOfT, context);
        return resource;
    }

}
