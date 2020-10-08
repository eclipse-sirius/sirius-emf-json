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
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the serialization of EAttributes.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class AttributesSaveTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/attributes/"; //$NON-NLS-1$
    }

    /**
     * Test the serialization of EString EAttribute.
     */
    @Test
    public void testSaveEString() {
        this.testSave("EAttributeEString.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EString EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEStringMany() {
        this.testSave("EAttributeEString--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EBoolean EAttribute.
     */
    @Test
    public void testSaveEBoolean() {
        this.testSave("EAttributeEBoolean.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EBoolean EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEBooleanMany() {
        this.testSave("EAttributeEBoolean--Many.xmi"); //$NON-NLS-1$

    }

    /**
     * Test the serialization of EBigDecimal EAttribute.
     */
    @Test
    public void testSaveEBigDecimal() {
        this.testSave("EAttributeEBigDecimal.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EBigDecimal EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEBigDecimalMany() {
        this.testSave("EAttributeEBigDecimal--Many.xmi"); //$NON-NLS-1$

    }

    /**
     * Test the serialization of EBigInteger EAttribute.
     */
    @Test
    public void testSaveEBigInteger() {
        this.testSave("EAttributeEBigInteger.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EBigInteger EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEBigIntegerMany() {
        this.testSave("EAttributeEBigInteger--Many.xmi"); //$NON-NLS-1$

    }

    /**
     * Test the serialization of EBooleanObject EAttribute.
     */
    @Test
    public void testSaveEBooleanObject() {
        this.testSave("EAttributeEBooleanObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EBooleanObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEBooleanObjectMany() {
        this.testSave("EAttributeEBooleanObject--Many.xmi"); //$NON-NLS-1$

    }

    /**
     * Test the serialization of EByte EAttribute.
     */
    @Test
    public void testSaveEByte() {
        this.testSave("EAttributeEByte.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EByte EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEByteMany() {
        this.testSave("EAttributeEByte--Many.xmi"); //$NON-NLS-1$

    }

    /**
     * Test the serialization of EByteArray EAttribute.
     */
    @Test
    public void testSaveEByteArray() {
        this.testSave("EAttributeEByteArray.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EByteArray EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEByteArrayMany() {
        this.testSave("EAttributeEByteArray--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EByteObject EAttribute.
     */
    @Test
    public void testSaveEByteObject() {
        this.testSave("EAttributeEByteObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EByteObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEByteObjectMany() {
        this.testSave("EAttributeEByteObject--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EChar EAttribute.
     */
    @Test
    public void testSaveEChar() {
        this.testSave("EAttributeEChar.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EChar EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveECharMany() {
        this.testSave("EAttributeEChar--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EChararcter EAttribute.
     */
    @Test
    public void testSaveECharacter() {
        this.testSave("EAttributeECharacter.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EChararcter EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveECharacterMany() {
        this.testSave("EAttributeECharacter--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EDate EAttribute.
     */
    @Test
    @Ignore("Does not work with a continuous integration system using a different timestamp since dates are converted")
    public void testSaveEDate() {
        this.testSave("EAttributeEDate.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EDate EAttribute with -1 as the upper bound.
     */
    @Test
    @Ignore("Does not work with a continuous integration system using a different timestamp since dates are converted")
    public void testSaveEDateMany() {
        this.testSave("EAttributeEDate--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EDouble EAttribute.
     */
    @Test
    public void testSaveEDouble() {
        this.testSave("EAttributeEDouble.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EDouble EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEDoubleMany() {
        this.testSave("EAttributeEDouble--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EDoubleObject EAttribute.
     */
    @Test
    public void testSaveEDoubleObject() {
        this.testSave("EAttributeEDoubleObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EDoubleObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEDoubleObjectMany() {
        this.testSave("EAttributeEDoubleObject--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EFloat EAttribute.
     */
    @Test
    public void testSaveEFloat() {
        this.testSave("EAttributeEFloat.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EFloat EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEFloatMany() {
        this.testSave("EAttributeEFloat--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EFloatObject EAttribute.
     */
    @Test
    public void testSaveEFloatObject() {
        this.testSave("EAttributeEFloatObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EFloatObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEFloatObjectMany() {
        this.testSave("EAttributeEFloatObject--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EInt EAttribute.
     */
    @Test
    public void testSaveEInt() {
        this.testSave("EAttributeEInt.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EInt EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEIntMany() {
        this.testSave("EAttributeEInt--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EInteger EAttribute.
     */
    @Test
    public void testSaveEInteger() {
        this.testSave("EAttributeEInteger.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EInteger EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEIntegerMany() {
        this.testSave("EAttributeEInteger--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of ELong EAttribute.
     */
    @Test
    public void testSaveELong() {
        this.testSave("EAttributeELong.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of ELong EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveELongMany() {
        this.testSave("EAttributeELong--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of ELongObject EAttribute.
     */
    @Test
    public void testSaveELongObject() {
        this.testSave("EAttributeELongObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of ELongObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveELongObjectMany() {
        this.testSave("EAttributeELongObject--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EShort EAttribute.
     */
    @Test
    public void testSaveEShort() {
        this.testSave("EAttributeEShort.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EShort EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEShortMany() {
        this.testSave("EAttributeEShort--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EShortObject EAttribute.
     */
    @Test
    public void testSaveEShortObject() {
        this.testSave("EAttributeEShortObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of EShortObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testSaveEShortObjectMany() {
        this.testSave("EAttributeEShortObject--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of Generic EAttribute.
     */
    @Test
    public void testSaveGenericEAttribute() {
        this.testSave("EAttributeGeneric.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of Parameterized EAttribute.
     */
    @Test
    public void testSaveTypeParameterizedEAttribute() {
        this.testSave("EAttributeTypeParameterized.ecore"); //$NON-NLS-1$
    }
}
