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
 * Test the dedeserialization of EAnnotations.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class AnnotationLoadTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/attributes/annotations/"; //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations deserialization on Class type.
     */
    @Test
    public void testEAnnotationsDeserializationOnClass() {
        this.testLoad("TestClassAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations deserialization on Operation type.
     */
    @Test
    public void testEAnnotationsDeserializationOnOperation() {
        this.testLoad("TestOperationAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations deserialization on TypeParameter type.
     */
    @Test
    public void testEAnnotationsDeserializationOnTypeParameter() {
        this.testLoad("TestTypeParameterAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations deserialization on Operation Parameters.
     */
    @Test
    public void testEAnnotationsDeserializationOnOperationParameter() {
        this.testLoad("TestParameterAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations deserialization on Attribute.
     */
    @Test
    public void testEAnnotationsDeserializationOnAttribute() {
        this.testLoad("TestAttributeAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations deserialization on Reference.
     */
    @Test
    public void testEAnnotationsDeserializationOnReference() {
        this.testLoad("TestReferenceAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations deserialization on Package.
     */
    @Test
    public void testEAnnotationsDeserializationOnPackage() {
        this.testLoad("TestPackageAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations deserialization on DataType.
     */
    @Test
    public void testEAnnotationsDeserializationOnDatatType() {
        this.testLoad("TestDataTypeAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations deserialization on Enumeration.
     */
    @Test
    public void testEAnnotationsDeserializationOnEnumeration() {
        this.testLoad("TestEnumerationAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations deserialization on Literal.
     */
    @Test
    public void testEAnnotationsDeserializationOnLiteral() {
        this.testLoad("TestLiteralAnnotation.ecore"); //$NON-NLS-1$
    }

}
