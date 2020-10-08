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

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * The Gson serializer is responsible for the serialization of Resources.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class GsonResourceSerializer implements JsonSerializer<Resource> {

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
    public GsonResourceSerializer(Map<?, ?> options) {
        this.options = options;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type,
     *      com.google.gson.JsonSerializationContext)
     */
    @Override
    public JsonElement serialize(Resource resourceToSerialize, Type type, JsonSerializationContext context) {
        GsonEObjectSerializer serializer = new GsonEObjectSerializer(resourceToSerialize, this.options);
        return serializer.serialize(resourceToSerialize.getContents(), type, context);
    }
}
