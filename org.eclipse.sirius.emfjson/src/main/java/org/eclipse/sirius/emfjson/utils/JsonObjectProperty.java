/*******************************************************************************
 * Copyright (c) 2022 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.emfjson.utils;

import com.google.gson.JsonElement;

import java.util.Objects;

/**
 * Use to store both the key and value of a json object property.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class JsonObjectProperty {
    private final String key;

    private final JsonElement element;

    public JsonObjectProperty(String key, JsonElement element) {
        this.key = Objects.requireNonNull(key);
        this.element = Objects.requireNonNull(element);
    }

    /**
     * Returns the key.
     *
     * @return The key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Returns the element.
     *
     * @return The element
     */
    public JsonElement getElement() {
        return this.element;
    }
}
