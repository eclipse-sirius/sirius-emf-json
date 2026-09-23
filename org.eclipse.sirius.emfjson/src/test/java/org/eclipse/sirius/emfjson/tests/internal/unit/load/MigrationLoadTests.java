/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.emfjson.tests.internal.unit.load;

import com.google.gson.JsonObject;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests loading with some migration.
 *
 * @author vrichard
 */
public class MigrationLoadTests extends AbstractEMFJsonTests {

    /**
     * {@inheritDoc}
     *
     * @see AbstractEMFJsonTests#getRootPath()
     */
    @Override
    protected String getRootPath() {
        return "/unit/references/migration/"; //$NON-NLS-1$
    }

    /**
     * Change the name of a EReference.
     */
    @Test
    public void testChangeReferenceName() {
        var jsonResourceProcessor = new JsonResource.IJsonResourceProcessor.NoOp() {
            @Override
            public String getEObjectUri(JsonResource resource, EObject eObject, EReference eReference, String uri) {
                if (eObject.eClass().getName().equals("Book")
                        && (eReference.getName().equals("library") || eReference.getName().equals("libraries"))) {
                    return uri.replace("branches", "customBranches");
                }
                return super.getEObjectUri(resource, eObject, eReference, uri);
            }
        };

        ExtendedMetaData metaData = new BasicExtendedMetaData() {
            @Override
            public EStructuralFeature getElement(EClass eClass, String namespace, String name) {
                if ("Library".equals(eClass.getName()) && "branches".equals(name)) {
                    return eClass.getEStructuralFeature("customBranches");
                }
                return eClass.getEStructuralFeature(name);
            }
        };

        this.options.put(JsonResource.OPTION_EXTENDED_META_DATA, metaData);
        this.options.put(JsonResource.OPTION_JSON_RESSOURCE_PROCESSOR, jsonResourceProcessor);
        this.testLoad("LibraryWithContainmentAndNonContainmentEReference.xmi"); //$NON-NLS-1$
    }

    /**
     * Migrates a namespace before resolving the prefix used by a root object.
     */
    @Test
    public void testChangeNamespaceBeforePrefixResolution() {
        var jsonResourceProcessor = new JsonResource.IJsonResourceProcessor.NoOp() {
            @Override
            public void preDeserialization(JsonResource resource, JsonObject jsonObject) {
                jsonObject.getAsJsonObject("ns").addProperty("ecore", EcorePackage.eNS_URI); //$NON-NLS-1$ //$NON-NLS-2$
            }
        };

        this.options.put(JsonResource.OPTION_JSON_RESSOURCE_PROCESSOR, jsonResourceProcessor);
        Resource resource = this.loadResource(this.getRootPath() + "NamespaceMigration.json", java.util.List.of(EcorePackage.eINSTANCE)); //$NON-NLS-1$

        Assert.assertEquals(EcorePackage.Literals.EPACKAGE, resource.getContents().get(0).eClass());
    }

}
