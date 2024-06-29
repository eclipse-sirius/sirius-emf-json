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
package org.eclipse.sirius.emfjson.tests.internal.options;

import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.sirius.emfjson.resource.IDManager;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Assert;
import org.junit.Test;

/**
 * This tests will check the usage of IDManager.
 *
 * @author fbarbin
 */
public class IDManagerOptionsTests extends AbstractEMFJsonTests {

    /**
     * The resource name.
     */
    private static final String RESOURCE_NAME = "SimpleClassWithID.ecore"; //$NON-NLS-1$

    /**
     * EClass id constant.
     */
    private static final String MY_E_CLASS_ID = "myEClassID"; //$NON-NLS-1$

    /**
     * EPackage id constant.
     */
    private static final String MY_E_PACKAGE_ID = "myEPackageID"; //$NON-NLS-1$

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
     * Test that the class is properly saved with the given ID provided by {@link IDManager}.
     */
    @Test
    public void testSaveClassWithID() {
        TestIDManager idManager = new TestIDManager();
        this.options.put(JsonResource.OPTION_ID_MANAGER, idManager);

        this.testSave(IDManagerOptionsTests.RESOURCE_NAME);
    }

    /**
     * Test that the class is properly loaded with the ID provided by IDManager.
     */
    @Test
    public void testLoadClassWithID() {
        TestIDManager testIDManager = new TestIDManager();

        this.options.put(JsonResource.OPTION_ID_MANAGER, testIDManager);

        this.testLoad(IDManagerOptionsTests.RESOURCE_NAME);

        Assert.assertTrue("The EPackage ID is not the expected one.", testIDManager.isePackageIDRetrieved()); //$NON-NLS-1$
        Assert.assertTrue("The EClass ID is not the expected one.", testIDManager.iseClassIDRetrieved()); //$NON-NLS-1$
    }

    /**
     * {@link IDManager} implementation for these tests.
     *
     * @author gcoutable
     */
    private class TestIDManager implements IDManager {
        /**
         * If ePackage ID has been properly retrieved.
         */
        private boolean ePackageIDRetrieved;

        /**
         * If eClass ID has been properly retrieved.
         */
        private boolean eClassIDRetrieved;

        @Override
        public void setId(EObject eObject, String id) {
            if (EcorePackage.eINSTANCE.getEPackage().equals(eObject.eClass())) {
                this.ePackageIDRetrieved = IDManagerOptionsTests.MY_E_PACKAGE_ID.equals(id);
            } else if (EcorePackage.eINSTANCE.getEClass().equals(eObject.eClass())) {
                this.eClassIDRetrieved = IDManagerOptionsTests.MY_E_CLASS_ID.equals(id);
            }
        }

        @Override
        public String getOrCreateId(EObject eObject) {
            String id;
            if (EcorePackage.eINSTANCE.getEPackage().equals(eObject.eClass())) {
                id = IDManagerOptionsTests.MY_E_PACKAGE_ID;
            } else if (EcorePackage.eINSTANCE.getEClass().equals(eObject.eClass())) {
                id = IDManagerOptionsTests.MY_E_CLASS_ID;
            } else {
                id = "unknown"; //$NON-NLS-1$
            }
            return id;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.eclipse.sirius.emfjson.resource.IDManager#findId(org.eclipse.emf.ecore.EObject)
         */
        @Override
        public Optional<String> findId(EObject eObject) {
            return Optional.ofNullable(this.getOrCreateId(eObject));
        }

        /**
         * {@inheritDoc}
         *
         * @see org.eclipse.sirius.emfjson.resource.IDManager#clearId(org.eclipse.emf.ecore.EObject)
         */
        @Override
        public Optional<String> clearId(EObject eObject) {
            return Optional.empty();
        }

        /**
         * Returns the eClassIDRetrieved.
         *
         * @return The eClassIDRetrieved
         */
        public boolean iseClassIDRetrieved() {
            return this.eClassIDRetrieved;
        }

        /**
         * Returns the ePackageIDRetrieved.
         *
         * @return The ePackageIDRetrieved
         */
        public boolean isePackageIDRetrieved() {
            return this.ePackageIDRetrieved;
        }
    }
}
