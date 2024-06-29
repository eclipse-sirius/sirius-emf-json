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

package org.eclipse.sirius.emfjson.tests.internal.unit.load;

import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Test;

/**
 * This class is used in order to tests the EOperation dedeserialization.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class OperationLoadTests extends AbstractEMFJsonTests {

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
     * Test the deserialization of EOperation class.
     */
    @Test
    public void testDeserializationOfSimpleOperations() {
        this.testLoad("SimpleOperation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EOperation class with a simple return type.
     */
    @Test
    public void testOperationWithSimpleTypeDeserialization() {
        this.testLoad("OperationWithSimpleType.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EOperation class with simple EParameters.
     */
    @Test
    public void testOperationWithSimpleParametersDeserialization() {
        this.testLoad("OperationWithSimpleParameters.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EOperation class with simple ETypeParameters.
     */
    @Test
    public void testOperationWithSimpleTypeParametersDeserialization() {
        this.testLoad("OperationWithSimpleTypeParameters.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EOperation class with simple EException.
     */
    @Test
    public void testOperationWithSimpleEExceptionDeserialization() {
        this.testLoad("OperationWithSimpleException.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EOperation class with many simple EException.
     */
    @Test
    public void testOperationWithManySimpleEExceptionDeserialization() {
        this.testLoad("OperationWithSimpleException--Many.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EOperation class with a ETypeParameter as a return type.
     */
    @Test
    public void testOperationWithETypeParameterAsReturnTypeDeserialization() {
        this.testLoad("OperationWithTypeParameterAsReturnType.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EOperation class with a ETypeParameter as a return type.
     */
    @Test
    public void testOperationWithEGenericTypeAsReturnTypeTypeDeserialization() {
        this.testLoad("OperationWithGenericTypeAsReturnType.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EOperation class with Generic Parameters.
     */
    @Test
    public void testOperationWithGenericParametersTypeDeserialization() {
        this.testLoad("OperationWithGenericParameters.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EOperation class with Generic Exceptions.
     */
    @Test
    public void testOperationWithGenericExceptionsDeserialization() {
        this.testLoad("OperationWithGenericExceptions.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of parameterized EOperation class.
     */
    @Test
    public void testParameterizedOperationDeserialization() {
        this.testLoad("ParameterizedOperation.ecore"); //$NON-NLS-1$
    }

}
