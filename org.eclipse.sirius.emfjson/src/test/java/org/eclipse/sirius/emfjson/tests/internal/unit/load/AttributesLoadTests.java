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
 * Test the deserialization of EAttributes.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class AttributesLoadTests extends AbstractEMFJsonTests {

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
     * Test the deserialization of EString EAttribute.
     */
    @Test
    public void testLoadEString() {
        this.testLoad("EAttributeEString.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EString EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEStringMany() {
        this.testLoad("EAttributeEString--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EBoolean EAttribute.
     */
    @Test
    public void testLoadEBoolean() {
        this.testLoad("EAttributeEBoolean.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EBoolean EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEBooleanMany() {
        this.testLoad("EAttributeEBoolean--Many.xmi"); //$NON-NLS-1$

    }

    /**
     * Test the deserialization of EBigDecimal EAttribute.
     */
    @Test
    public void testLoadEBigDecimal() {
        this.testLoad("EAttributeEBigDecimal.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EBigDecimal EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEBigDecimalMany() {
        this.testLoad("EAttributeEBigDecimal--Many.xmi"); //$NON-NLS-1$

    }

    /**
     * Test the deserialization of EBigInteger EAttribute.
     */
    @Test
    public void testLoadEBigInteger() {
        this.testLoad("EAttributeEBigInteger.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EBigInteger EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEBigIntegerMany() {
        this.testLoad("EAttributeEBigInteger--Many.xmi"); //$NON-NLS-1$

    }

    /**
     * Test the deserialization of EBooleanObject EAttribute.
     */
    @Test
    public void testLoadEBooleanObject() {
        this.testLoad("EAttributeEBooleanObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EBooleanObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEBooleanObjectMany() {
        this.testLoad("EAttributeEBooleanObject--Many.xmi"); //$NON-NLS-1$

    }

    /**
     * Test the deserialization of EByte EAttribute.
     */
    @Test
    public void testLoadEByte() {
        this.testLoad("EAttributeEByte.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EByte EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEByteMany() {
        this.testLoad("EAttributeEByte--Many.xmi"); //$NON-NLS-1$

    }

    /**
     * Test the deserialization of EByteArray EAttribute.
     */
    @Test
    public void testLoadEByteArray() {
        this.testLoad("EAttributeEByteArray.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EByteArray EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEByteArrayMany() {
        this.testLoad("EAttributeEByteArray--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EByteObject EAttribute.
     */
    @Test
    public void testLoadEByteObject() {
        this.testLoad("EAttributeEByteObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EByteObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEByteObjectMany() {
        this.testLoad("EAttributeEByteObject--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EChar EAttribute.
     */
    @Test
    public void testLoadEChar() {
        this.testLoad("EAttributeEChar.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EChar EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadECharMany() {
        this.testLoad("EAttributeEChar--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EChararcter EAttribute.
     */
    @Test
    public void testLoadECharacter() {
        this.testLoad("EAttributeECharacter.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EChararcter EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadECharacterMany() {
        this.testLoad("EAttributeECharacter--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EDate EAttribute.
     */
    @Test
    public void testLoadEDate() {
        this.testLoad("EAttributeEDate.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EDate EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEDateMany() {
        this.testLoad("EAttributeEDate--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EDouble EAttribute.
     */
    @Test
    public void testLoadEDouble() {
        this.testLoad("EAttributeEDouble.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EDouble EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEDoubleMany() {
        this.testLoad("EAttributeEDouble--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EDoubleObject EAttribute.
     */
    @Test
    public void testLoadEDoubleObject() {
        this.testLoad("EAttributeEDoubleObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EDoubleObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEDoubleObjectMany() {
        this.testLoad("EAttributeEDoubleObject--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EFloat EAttribute.
     */
    @Test
    public void testLoadEFloat() {
        this.testLoad("EAttributeEFloat.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EFloat EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEFloatMany() {
        this.testLoad("EAttributeEFloat--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EFloatObject EAttribute.
     */
    @Test
    public void testLoadEFloatObject() {
        this.testLoad("EAttributeEFloatObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EFloatObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEFloatObjectMany() {
        this.testLoad("EAttributeEFloatObject--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EInt EAttribute.
     */
    @Test
    public void testLoadEInt() {
        this.testLoad("EAttributeEInt.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EInt EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEIntMany() {
        this.testLoad("EAttributeEInt--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EInteger EAttribute.
     */
    @Test
    public void testLoadEInteger() {
        this.testLoad("EAttributeEInteger.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EInteger EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEIntegerMany() {
        this.testLoad("EAttributeEInteger--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of ELong EAttribute.
     */
    @Test
    public void testLoadELong() {
        this.testLoad("EAttributeELong.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of ELong EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadELongMany() {
        this.testLoad("EAttributeELong--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of ELongObject EAttribute.
     */
    @Test
    public void testLoadELongObject() {
        this.testLoad("EAttributeELongObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of ELongObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadELongObjectMany() {
        this.testLoad("EAttributeELongObject--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EShort EAttribute.
     */
    @Test
    public void testLoadEShort() {
        this.testLoad("EAttributeEShort.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EShort EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEShortMany() {
        this.testLoad("EAttributeEShort--Many.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EShortObject EAttribute.
     */
    @Test
    public void testLoadEShortObject() {
        this.testLoad("EAttributeEShortObject.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of EShortObject EAttribute with -1 as the upper bound.
     */
    @Test
    public void testLoadEShortObjectMany() {
        this.testLoad("EAttributeEShortObject--Many.xmi"); //$NON-NLS-1$
    }
}
