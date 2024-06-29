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
 * Test the deserialization of EClass.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class ClassLoadTests extends AbstractEMFJsonTests {

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
     * Test the deserialization of a simple class with just a name.
     */
    @Test
    public void testLoadSimpleClass() {
        this.testLoad("SimpleClass.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of an abstract class.
     */
    @Test
    public void testLoadAbstractClass() {
        this.testLoad("AbstractClass.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of an interface.
     */
    @Test
    public void testLoadInterface() {
        this.testLoad("Interface.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of many classes.
     */
    @Test
    public void testLoadManyClasses() {
        this.testLoad("SimpleClass--Many.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of two classes with one extended an other.
     */
    @Test
    public void testLoadExtendedClasses() {
        this.testLoad("ExtendedClasses.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of two classes with one extended an other.
     */
    @Test
    public void testLoadClassWithGenericSuperType() {
        this.testLoad("ClassWithGenericSuperType.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of a class which inherit of an other in an external resource.
     */
    @Test
    public void testLoadClassWithExternalGenericSuperType() {
        this.testLoad("ClassWithExternalGenericSuperType.ecore"); //$NON-NLS-1$
    }

}
