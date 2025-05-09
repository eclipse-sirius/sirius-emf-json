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

import java.util.function.Predicate;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.eclipse.sirius.emfjson.utils.JsonHelper;
import org.junit.Test;

import model.TestPojoDataTypeImpl;

/**
 * Tests loading with ExtendedMetaData.
 *
 * @author vrichard
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
        JsonHelper jsonHelper = new JsonHelper() {
            @Override
            public void setValue(EObject object, EStructuralFeature feature, Object value) {
                Object newValue = value;
                if ("NodeSingleValueAttribute".equals(feature.getEContainingClass().getName()) && "singleIntAttribute".equals(feature.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
                    newValue = Integer.valueOf(((String) value).length());
                }
                super.setValue(object, feature, newValue);
            }
        };
        this.options.put(JsonResource.OPTION_CUSTOM_HELPER, jsonHelper);

        Predicate<EDataType> eDataTypeJsonSerializableTester = new Predicate<>() {
            @Override
            public boolean test(EDataType eDataType) {
                return eDataType.getInstanceClass() == TestPojoDataTypeImpl.class;
            }
        };
        this.options.put(JsonResource.OPTION_SHOULD_EDATATYPE_BE_SERIALIZED_IN_JSON_PREDICATE, eDataTypeJsonSerializableTester);

        this.testLoad("TestChangeAttributeTypeMono.xmi"); //$NON-NLS-1$
    }

    /**
     * Change the type of a monovalued EAttribute.
     */
    @Test
    public void testChangeAttributeTypeMulti() {
        JsonHelper jsonHelper = new JsonHelper() {
            @Override
            public void setValue(EObject object, EStructuralFeature feature, Object value) {
                Object newValue = value;
                if ("NodeMultiValuedAttribute".equals(feature.getEContainingClass().getName()) && "multiIntAttribute".equals(feature.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
                    newValue = Integer.valueOf(((String) value).length());
                }
                super.setValue(object, feature, newValue);
            }
        };

        this.options.put(JsonResource.OPTION_CUSTOM_HELPER, jsonHelper);
        this.testLoad("TestChangeAttributeTypeMulti.xmi"); //$NON-NLS-1$
    }

    /**
     * Change the value of a monovalued EAttribute.
     */
    @Test
    public void testChangeAttributeValueMono() {
        JsonHelper jsonHelper = new JsonHelper() {
            @Override
            public void setValue(EObject object, EStructuralFeature feature, Object value) {
                Object newValue = value;
                if ("NodeSingleValueAttribute".equals(feature.getEContainingClass().getName()) && "singleStringAttribute".equals(feature.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
                    newValue = ((String) value).toUpperCase();
                }
                super.setValue(object, feature, newValue);
            }

        };

        this.options.put(JsonResource.OPTION_CUSTOM_HELPER, jsonHelper);
        this.testLoad("TestChangeAttributeValueMono.xmi"); //$NON-NLS-1$
    }

    /**
     * Change the value of a multivalued EAttribute.
     */
    @Test
    public void testChangeAttributeValueMulti() {
        JsonHelper jsonHelper = new JsonHelper() {
            @Override
            public void setValue(EObject object, EStructuralFeature feature, Object value) {
                Object newValue = value;
                if ("NodeMultiValuedAttribute".equals(feature.getEContainingClass().getName()) && "multiStringAttribute".equals(feature.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
                    newValue = ((String) value).toUpperCase();
                }
                super.setValue(object, feature, newValue);
            }
        };

        this.options.put(JsonResource.OPTION_CUSTOM_HELPER, jsonHelper);
        this.testLoad("TestChangeAttributeValueMulti.xmi"); //$NON-NLS-1$
    }

}
