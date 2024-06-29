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
 * This class is used in order to test the EOperation serializations.
 *
 * @author <a href="mailto:guillaume.Coutable@obeo.fr">Guillaume Coutable/a>
 */
public class OperationsSaveTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/attributes/operations/"; //$NON-NLS-1$
    }

    /**
     * Test the serialization of EOperation class.
     */
    @Test
    public void testSerializationOfSimpleOperations() {
        this.testSave("SimpleOperation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EOperation class with a simple return type.
     */
    @Test
    public void testOperationWithSimpleTypeSerialization() {
        this.testSave("OperationWithSimpleType.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EOperation class with simple EParameters.
     */
    @Test
    public void testOperationWithSimpleParametersSerialization() {
        this.testSave("OperationWithSimpleParameters.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EOperation class with simple ETypeParameters.
     */
    @Test
    public void testOperationWithSimpleTypeParametersSerialization() {
        this.testSave("OperationWithSimpleTypeParameters.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EOperation class with simple EException.
     */
    @Test
    public void testOperationWithSimpleEExceptionSerialization() {
        this.testSave("OperationWithSimpleException.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EOperation class with many simple EException.
     */
    @Test
    public void testOperationWithManySimpleEExceptionSerialization() {
        this.testSave("OperationWithSimpleException--Many.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EOperation class with a ETypeParameter as a return type.
     */
    @Test
    public void testOperationWithETypeParameterAsReturnTypeSerialization() {
        this.testSave("OperationWithTypeParameterAsReturnType.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EOperation class with a ETypeParameter as a return type.
     */
    @Test
    public void testOperationWithEGenericTypeAsReturnTypeTypeSerialization() {
        this.testSave("OperationWithGenericTypeAsReturnType.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EOperation class with Generic Parameters.
     */
    @Test
    public void testOperationWithGenericParametersTypeSerialization() {
        this.testSave("OperationWithGenericParameters.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EOperation class with Generic Exceptions.
     */
    @Test
    public void testOperationWithGenericExceptionsSerialization() {
        this.testSave("OperationWithGenericExceptions.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of parameterized EOperation class.
     */
    @Test
    public void testParameterizedOperationSerialization() {
        this.testSave("ParameterizedOperation.ecore"); //$NON-NLS-1$
    }
}
