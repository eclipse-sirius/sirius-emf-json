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
package org.eclipse.sirius.emfjson.resource.exception;

import org.eclipse.emf.ecore.resource.Resource.Diagnostic;

/**
 * This class is used to throw an exception when the serializer encounter a referenced object. the throwing depend also
 * of the chosen option for OPTION_PROCESS_DANGLING_HREF.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class DanglingHREFException extends Exception implements Diagnostic {

    /**
     * .
     */
    private static final long serialVersionUID = 1L;

    /**
     * The uri location of the dangling reference.
     */
    private final String location;

    /**
     * The constructor.
     */
    public DanglingHREFException() {
        super();
        this.location = ""; //$NON-NLS-1$
    }

    /**
     * The constructor.
     *
     * @param message
     *            the message to throw
     */
    public DanglingHREFException(String message) {
        super(message);
        this.location = ""; //$NON-NLS-1$
    }

    /**
     * The constructor.
     *
     * @param message
     *            the message to throw
     * @param location
     *            the uri location of the dangling reference
     */
    public DanglingHREFException(String message, String location) {
        super(message);
        this.location = location;
    }

    /**
     * Returns the uri location of the dangling reference.
     *
     * @return The location uri location of the dangling reference.
     */
    @Override
    public String getLocation() {
        return this.location;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        return message + "(" + this.location + ")."; //$NON-NLS-1$//$NON-NLS-2$
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
