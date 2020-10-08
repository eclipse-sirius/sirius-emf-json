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
 * Test the serialization of EClass.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class ClassSaveTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/classes/"; //$NON-NLS-1$
    }

    /**
     * Test the serialization of a simple class with just a name.
     */
    @Test
    public void testSaveSimpleClass() {
        this.testSave("SimpleClass.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of an abstract class.
     */
    @Test
    public void testSaveAbstractClass() {
        this.testSave("AbstractClass.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of an interface.
     */
    @Test
    public void testSaveInterface() {
        this.testSave("Interface.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of many classes.
     */
    @Test
    public void testSaveManyClasses() {
        this.testSave("SimpleClass--Many.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of two classes with one extended an other.
     */
    @Test
    public void testSaveExtendedClasses() {
        this.testSave("ExtendedClasses.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of two classes with one extended an other.
     */
    @Test
    public void testSaveClassWithGenericSuperType() {
        this.testSave("ClassWithGenericSuperType.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the serialization of a class which inherit of an other in an external resource.
     */
    @Test
    public void testSaveClassWithExternalGenericSuperType() {
        this.testSave("ClassWithExternalGenericSuperType.ecore"); //$NON-NLS-1$
    }

}
