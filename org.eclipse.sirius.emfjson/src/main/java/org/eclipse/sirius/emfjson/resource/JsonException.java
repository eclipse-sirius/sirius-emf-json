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

import org.eclipse.emf.ecore.resource.Resource;

/**
 * Manage all Exception that can be reported during serialization and deserialization.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class JsonException extends Exception implements Resource.Diagnostic {

    /**
     * .
     */
    private static final long serialVersionUID = 1L;

    /**
     * the location Exception.
     */
    protected final String location;

    /**
     * The constructor.
     *
     * @param message
     *            the message
     */
    public JsonException(String message) {
        super(message);
        this.location = ""; //$NON-NLS-1$
    }

    /**
     * The constructor.
     *
     * @param message
     *            the message
     * @param location
     *            the location
     */
    public JsonException(String message, String location) {
        super(message);
        this.location = location;
    }

    /**
     * Returns the location.
     *
     * @return The location
     */
    @Override
    public String getLocation() {
        return this.location;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.emf.ecore.resource.Resource.Diagnostic#getLine()
     */
    @Override
    public int getLine() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.emf.ecore.resource.Resource.Diagnostic#getColumn()
     */
    @Override
    public int getColumn() {
        return 0;
    }

}
