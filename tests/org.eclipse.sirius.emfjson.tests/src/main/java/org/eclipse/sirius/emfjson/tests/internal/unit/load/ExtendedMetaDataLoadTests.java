/*******************************************************************************
 * Copyright (c) 2023 Obeo.
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
 */
public class ExtendedMetaDataLoadTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/attributes/extendedmetadata/"; //$NON-NLS-1$
    }

    /**
     * Change the name of a monovalued EAttribute.
     */
    @Test
    public void testChangeAttributeNameMono() {
        ExtendedMetaData metaData = new BasicExtendedMetaData() {
            @Override
            public EStructuralFeature getElement(EClass eClass, String namespace, String name) {
                if ("NodeSingleValueAttribute".equals(eClass.getName()) && "singleStringAttributeOld".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
                    return eClass.getEStructuralFeature("singleStringAttribute"); //$NON-NLS-1$
                }
                return super.getElement(eClass, namespace, name);
            }

        };
        this.options.put(JsonResource.OPTION_EXTENDED_META_DATA, metaData);
        this.testLoad("TestChangeAttributeNameMono.xmi"); //$NON-NLS-1$
    }

    /**
     * Change the name of a multivalued EAttribute.
     */
    @Test
    public void testChangeAttributeNameMulti() {
        // CHECKSTYLE:OFF
        ExtendedMetaData metaData = new BasicExtendedMetaData() {

            /**
             * {@inheritDoc}
             *
             * @see org.eclipse.emf.ecore.util.BasicExtendedMetaData#getElement(org.eclipse.emf.ecore.EClass,
             *      java.lang.String, java.lang.String)
             */
            @Override
            public EStructuralFeature getElement(EClass eClass, String namespace, String name) {
                if ("NodeMultiValuedAttribute".equals(eClass.getName()) && "multiStringAttributeOld".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
                    return eClass.getEStructuralFeature("multiStringAttribute"); //$NON-NLS-1$
                }
                return super.getElement(eClass, namespace, name);
            }

        };
        // CHECKSTYLE:ON
        this.options.put(JsonResource.OPTION_EXTENDED_META_DATA, metaData);
        this.testLoad("TestChangeAttributeNameMulti.xmi"); //$NON-NLS-1$
    }

}
