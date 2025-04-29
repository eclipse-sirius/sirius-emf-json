/*******************************************************************************
 * Copyright (c) 2023, 2025 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.emfjson.tests.internal.unit.load;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Test;

/**
 * Tests loading with ExtendedMetaData.
 *
 * @author vrichard
 */
public class ExtendedMetaDataReferencesLoadTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/references/extendedmetadata/"; //$NON-NLS-1$
    }

    /**
     * Change the name of a monovalued EReference.
     */
    @Test
    public void testChangeReferenceNameMono() {
        ExtendedMetaData metaData = new BasicExtendedMetaData() {
            @Override
            public EStructuralFeature getElement(EClass eClass, String namespace, String name) {
                if ("NodeSingleValueReference".equals(eClass.getName()) && "singleValuedReferenceOld".equals(name)) {
                    // $NON-NLS-1$ //$NON-NLS-2$
                    return eClass.getEStructuralFeature("singleValuedReference"); //$NON-NLS-1$
                }
                // return super.getElement(eClass, namespace, name); // Doesn't work because the super implementation in
                // Ecore ends up checking that the string is XMI
                return eClass.getEStructuralFeature(name);
            }
        };

        this.options.put(JsonResource.OPTION_EXTENDED_META_DATA, metaData);
        this.testLoad("TestChangeReferenceNameMono.xmi"); //$NON-NLS-1$
    }

    /**
     * Change the name of a multivalued EReference.
     */
    @Test
    public void testChangeReferenceNameMulti() {
        ExtendedMetaData metaData = new BasicExtendedMetaData() {
            @Override
            public EStructuralFeature getElement(EClass eClass, String namespace, String name) {
                if ("NodeMultiValuedAttribute".equals(eClass.getName()) && "multiStringAttributeOld".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
                    return eClass.getEStructuralFeature("multiStringAttribute"); //$NON-NLS-1$
                }
                // return super.getElement(eClass, namespace, name); // Doesn't work because the super implementation in
                // Ecore ends up checking that the string is XMI
                return eClass.getEStructuralFeature(name);
            }
        };

        this.options.put(JsonResource.OPTION_EXTENDED_META_DATA, metaData);
        this.testLoad("TestChangeReferenceNameMulti.xmi"); //$NON-NLS-1$
    }

}
