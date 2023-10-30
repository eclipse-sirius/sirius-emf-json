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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.eclipse.sirius.emfjson.utils.JsonHelper;
import org.junit.Test;

/**
 * Tests loading with ExtendedMetaData.
 */
public class JsonHelperDataLoadTests extends AbstractEMFJsonTests {

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
     * Change the type of a monovalued EAttribute.
     */
    @Test
    public void testChangeAttributeTypeMono() {
        // CHECKSTYLE:OFF
        JsonHelper jsonHelper = new JsonHelper() {

            /**
             * {@inheritDoc}
             *
             * @see org.eclipse.sirius.emfjson.utils.JsonHelper#setValue(org.eclipse.emf.ecore.EObject,
             *      org.eclipse.emf.ecore.EStructuralFeature, java.lang.Object)
             */
            @Override
            public void setValue(EObject object, EStructuralFeature feature, Object value) {
                Object newValue = value;
                if ("NodeSingleValueAttribute".equals(feature.getEContainingClass().getName()) && "singleIntAttribute".equals(feature.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
                    newValue = new Integer(((String) value).length());
                }
                super.setValue(object, feature, newValue);
            }

        };

        // CHECKSTYLE:ON
        this.options.put(JsonResource.OPTION_CUSTOM_HELPER, jsonHelper);
        this.testLoad("TestChangeAttributeTypeMono.xmi"); //$NON-NLS-1$
    }

}
