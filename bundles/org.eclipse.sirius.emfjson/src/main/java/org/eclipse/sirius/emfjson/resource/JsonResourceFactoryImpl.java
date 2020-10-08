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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * The resource factory is used to register the creation of Json resource in a resource set.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class JsonResourceFactoryImpl implements Resource.Factory {

    /**
     * The Json extension.
     */
    public static final String EXTENSION = "json"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.emf.ecore.resource.Resource.Factory#createResource(org.eclipse.emf.common.util.URI)
     */
    @Override
    public Resource createResource(URI uri) {
        return new JsonResourceImpl(uri, null);
    }

}
