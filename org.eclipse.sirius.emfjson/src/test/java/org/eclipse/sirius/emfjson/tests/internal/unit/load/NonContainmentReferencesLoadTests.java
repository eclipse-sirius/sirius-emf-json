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
 * Tests class for EReferences.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class NonContainmentReferencesLoadTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/references/noncontainment/"; //$NON-NLS-1$
    }

    /**
     * Test the deserialization of Single valued EReference.
     */
    @Test
    public void testLoadSingleValuedEReferences() {
        this.testLoad("NodeSingleValueEReference.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of multi-valued EReference.
     */
    @Test
    public void testLoadMultiValuedEReferences() {
        this.testLoad("NodeMultiValuedEReference.xmi"); //$NON-NLS-1$
    }

    /**
     * Tests the deserialization of a sub-library containing a book referencing its owner sub-library. For this test we
     * use an modified extlibrary.ecore meta-model.
     */
    @Test
    public void testLoadLibraryWithBookReferencingItsOwnBranchLibrary() {
        this.testLoad("TestLibraryWithBookReferencingItsOwnBranchLibrary.xmi"); //$NON-NLS-1$
    }

    /**
     * Tests the deserialization of a library containing a book referencing its owner library. For this test we use an
     * modified extlibrary.ecore meta-model.
     */
    @Test
    public void testLoadLibraryWithBookReferencingItsOwnLibrary() {
        this.testLoad("TestLibraryWithBookReferencingItsOwnLibrary.xmi"); //$NON-NLS-1$
    }

    /**
     * Tests the deserialization of a library containing a book referencing many libraries. For this test we use an
     * modified extlibrary.ecore meta-model.
     */
    @Test
    public void testLoadLibraryWithBookReferencingLibraries() {
        this.testLoad("TestLibraryWithBookReferencingLibraries.xmi"); //$NON-NLS-1$
    }

    /**
     * Tests the deserialization of a book referencing an library branch in an external resource. For this test we use
     * an modified extlibrary.ecore meta-model.
     */
    @Test
    public void testLoadLibraryWithExternalBookReferencingBranchLibrary() {
        this.testLoad("TestLibraryWithExternalBookReferencingBranchLibrary_Book.xmi"); //$NON-NLS-1$
    }

    /**
     * Tests the deserialization a book referencing many libraries in an external resource. For this test we use an
     * modified extlibrary.ecore meta-model.
     */
    @Test
    public void testLoadLibraryWithExternalBookReferencingLibraries() {
        this.testLoad("TestLibraryWithExternalBookReferencingLibraries_Book.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialisation of a book referencing a root library in an external resource. For this test we use an
     * modified extlibrary.ecore meta-model.
     */
    @Test
    public void testLoadLibraryWithExternalBookReferencingLibrary() {
        this.testLoad("TestLibraryWithExternalBookReferencingLibrary_Book.xmi"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of a class referencing external eSuperTypes.
     */
    @Test
    public void testLoadExternalMultiNonContainmentReferences() {
        this.testLoad("TestExternalMultiNonContainmentReferences.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of a non containment EReferences in an Ecore resource.
     */
    @Test
    public void testLoadInternalMultiNonContainmentReferenceOnEcoreResource() {
        this.testLoad("TestInternalMultiNonContainmentReference.ecore"); //$NON-NLS-1$
    }

    /**
     * Test the deserialization of a non containment EReference in an Ecore resource.
     */
    @Test
    public void testLoadInternalSingleNonContainmentReferenceOnEcoreresource() {
        this.testSave("TestInternalSingleNonContainmentReference.ecore"); //$NON-NLS-1$
    }

}
