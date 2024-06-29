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
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * A serializer for a list of resources.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class GsonResourceListSerializer implements JsonSerializer<List<Resource>> {

    /**
     * The serialization options.
     */
    private Map<?, ?> options;

    /**
     * The constructor.
     *
     * @param options
     *            the serialize options
     */
    public GsonResourceListSerializer(Map<?, ?> options) {
        this.options = options;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type,
     *      com.google.gson.JsonSerializationContext)
     */
    @Override
    public JsonElement serialize(List<Resource> resources, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jsonArray = new JsonArray();
        for (Resource resource : resources) {
            GsonResourceSerializer gsonResourceSerializer = new GsonResourceSerializer(this.options);
            jsonArray.add(gsonResourceSerializer.serialize(resource, typeOfSrc, context));
        }
        return jsonArray;
    }

}
