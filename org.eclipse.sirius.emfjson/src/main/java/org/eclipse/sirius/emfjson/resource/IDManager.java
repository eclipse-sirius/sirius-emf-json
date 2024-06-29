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

import java.util.Optional;

import org.eclipse.emf.ecore.EObject;

/**
 * Used to help the {@link JsonResource} to maintain its map of ID to EObject.
 *
 * @author gcoutable
 */
public interface IDManager {

    /**
     * Returns the extrinsic ID of the given {@link EObject}. If the ID does not exist, creates it.
     *
     * <p>
     * If the ID has been created, it will not be associated with the eObject yet, {@link #setId(EObject, String)} will.
     * </p>
     *
     * @param eObject
     *            The eObject
     * @return The ID of the EObject
     */
    String getOrCreateId(EObject eObject);

    /**
     * Returns the extrinsic ID of the given EObject or {@link Optional#empty()} if it cannot be found.
     *
     * @param eObject
     *            The eObject
     * @return The ID of the given EObject or {@link Optional#empty()}
     */
    Optional<String> findId(EObject eObject);

    /**
     * Clears the extrinsic ID of the given EObject. Returns the ID if it has been cleared.
     *
     * @param eObject
     *            The eObject
     * @return The cleared ID or {@link Optional#empty()}
     */
    Optional<String> clearId(EObject eObject);

    /**
     * Sets the ID to the {@link EObject}.
     *
     * <p>
     * Once the ID has been set, the {@link JsonResource} associated with the IDManager, will store the entry ID to
     * EObject in its map.
     * </p>
     *
     * @param eObject
     *            The eObject to associate with the ID
     * @param id
     *            The ID to set in the eObject
     */
    void setId(EObject eObject, String id);
}
