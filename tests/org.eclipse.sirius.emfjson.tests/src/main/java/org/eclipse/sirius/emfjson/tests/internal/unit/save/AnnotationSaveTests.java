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
 * Class used to test the EAnnotations serialization.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class AnnotationSaveTests extends AbstractEMFJsonTests {

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
     * Test the EAnnotations serialization on Class type.
     */
    @Test
    public void testEAnnotationsSerializationOnClass() {
        this.testSave("TestClassAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations serialization on Operation type.
     */
    @Test
    public void testEAnnotationsSerializationOnOperation() {
        this.testSave("TestOperationAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations serialization on TypeParameter type.
     */
    @Test
    public void testEAnnotationsSerializationOnTypeParameter() {
        this.testSave("TestTypeParameterAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations serialization on Operation Parameters.
     */
    @Test
    public void testEAnnotationsSerializationOnOperationParameter() {
        this.testSave("TestParameterAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations serialization on Attribute.
     */
    @Test
    public void testEAnnotationsSerializationOnAttribute() {
        this.testSave("TestAttributeAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations serialization on Reference.
     */
    @Test
    public void testEAnnotationsSerializationOnReference() {
        this.testSave("TestReferenceAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations serialization on Package.
     */
    @Test
    public void testEAnnotationsSerializationOnPackage() {
        this.testSave("TestPackageAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations serialization on DataType.
     */
    @Test
    public void testEAnnotationsSerializationOnDatatType() {
        this.testSave("TestDataTypeAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations serialization on Enumeration.
     */
    @Test
    public void testEAnnotationsSerializationOnEnumeration() {
        this.testSave("TestEnumerationAnnotation.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the EAnnotations serialization on Literal.
     */
    @Test
    public void testEAnnotationsSerializationOnLiteral() {
        this.testSave("TestLiteralAnnotation.ecore"); //$NON-NLS-1$
    }
}
