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

package org.eclipse.sirius.emfjson.tests.internal.unit.save;

import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Test;

/**
 * This class is used in order to test the ETypeParameter serializations.
 *
 * @author <a href="mailto:guillaume.Coutable@obeo.fr">Guillaume Coutable/a>
 */
public class TypeParametersSaveTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/attributes/typeParameters/"; //$NON-NLS-1$
    }

    /**
     * Test the serialization of class ETypeParameter.
     */
    @Test
    public void testSerializationOfTypeParameter() {
        this.testSave("TypeParameter.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of class ETypeParameter with many EClassifier as bound.
     */
    @Test
    public void testTypeParameterWithEClassifierAsBoundSerialization() {
        this.testSave("TypeParameterWithEClassifierAsBound.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of class ETypeParameter with many other ETypeParameter as bound.
     */
    @Test
    public void testTypeParameterWithTypeParameterAsBoundSerialization() {
        this.testSave("TypeParameterWithETypeParameterAsBound.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of class ETypeParameter withGeneric EClassifier.
     */
    @Test
    public void testTypeParameterWithGenericEClassifierSerialization() {
        this.testSave("GenericTypeParameter.ecore"); //$NON-NLS-1$
    }

}
