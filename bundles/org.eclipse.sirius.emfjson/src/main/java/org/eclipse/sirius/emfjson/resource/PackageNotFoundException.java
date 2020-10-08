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

/**
 * This exception is throw when a package is not found.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class PackageNotFoundException extends JsonException {

    /**
     * .
     */
    private static final long serialVersionUID = 1L;

    /**
     * The uri that correspond to the missing package.
     */
    protected final String uri;

    /**
     * The constructor.
     *
     * @param uri
     *            the URI
     * @param location
     *            the location
     */
    public PackageNotFoundException(String uri, String location) {
        super("Package with uri '" + uri + "' not found.", location); //$NON-NLS-1$//$NON-NLS-2$
        this.uri = uri;
    }

    /**
     * Returns the uri.
     *
     * @return The uri
     */
    public String getUri() {
        return this.uri;
    }

}
