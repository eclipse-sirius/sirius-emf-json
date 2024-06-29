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

package org.eclipse.sirius.emfjson.tests.internal.unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.emfjson.resource.IDManager;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * Assert that a removed object cannot be found with resource.getEObject(id).
 *
 * @author <a href="mailto:hugo.marchadour@obeo.fr">Hugo Marchadour</a>
 */
public class RemoveObjectTests {

    /**
     * The {@link EPackage} used for tests.
     */
    private final EPackage ePackage;

    /**
     * The {@link EClass} used for tests.
     */
    private final EClass eClass;

    /**
     * The id {@link EAttribute} used for tests.
     */
    private final EAttribute eAttribute;

    /**
     * The constructor.
     *
     */
    public RemoveObjectTests() {
        this.ePackage = EcoreFactory.eINSTANCE.createEPackage();
        this.ePackage.setName("test"); //$NON-NLS-1$
        this.ePackage.setNsPrefix("test"); //$NON-NLS-1$
        this.ePackage.setNsURI("http//:test"); //$NON-NLS-1$
        this.eClass = EcoreFactory.eINSTANCE.createEClass();
        this.eClass.setName("Test"); //$NON-NLS-1$
        this.eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
        this.eAttribute.setID(true);
        this.eAttribute.setName("id"); //$NON-NLS-1$
        this.eAttribute.setEType(EcorePackage.eINSTANCE.getEString());
        this.eClass.getEStructuralFeatures().add(this.eAttribute);
        this.ePackage.getEClassifiers().add(this.eClass);
    }

    /**
     * A removed object should not be found with resource.getEObject(id).
     */
    @Test
    public void testGetEObjectById() {
        String id = "007"; //$NON-NLS-1$
        Resource resource = this.createJsonResource();

        EObject eObjectWithId = this.createEObjectWithId(id);
        resource.getContents().add(eObjectWithId);
        Assert.assertEquals(eObjectWithId, resource.getEObject(id));

        resource.getContents().remove(eObjectWithId);
        Assert.assertNull(resource.getEObject(id));
    }

    /**
     * A removed object should not be found using resource.getEObject(id) when its id has been set using
     * {@link JsonResource#setID(EObject, String)}.
     */
    @Test
    public void testGetEObjectByExtrinsicId() {
        JsonResource resource = this.createJsonResource();

        EFactory eFactoryInstance = this.ePackage.getEFactoryInstance();
        EObject eObject = eFactoryInstance.create(this.eClass);
        resource.getContents().add(eObject);

        String extrinsicId = "007"; //$NON-NLS-1$
        resource.setID(eObject, extrinsicId);
        Assert.assertEquals(eObject, resource.getEObject(extrinsicId));

        resource.getContents().remove(eObject);
        Assert.assertNull(resource.getEObject(extrinsicId));
    }

    /**
     * Creates a resource with an {@link IDManager}
     *
     * @return the created resource
     */
    private JsonResource createJsonResource() {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(this.ePackage.getNsURI(), this.ePackage);
        Map<String, Object> options = new HashMap<String, Object>();
        options.put(JsonResource.OPTION_ID_MANAGER, new IDManager() {
            @Override
            public String getOrCreateId(EObject eObject) {
                return EcoreUtil.getID(eObject);
            }

            @Override
            public void setId(EObject eObject, String id) {
                EcoreUtil.setID(eObject, id);
            }

            @Override
            public Optional<String> findId(EObject eObject) {
                return Optional.ofNullable(this.getOrCreateId(eObject));
            }

            @Override
            public Optional<String> clearId(EObject eObject) {
                return Optional.empty();
            }
        });
        return new JsonResourceImpl(URI.createURI(""), options); //$NON-NLS-1$
    }

    /**
     * Creates an {@link EObject} with the given id.
     *
     * @param id
     *            The id of the EObject to create
     * @return The created {@link EObject}
     */
    private EObject createEObjectWithId(String id) {
        EFactory eFactoryInstance = this.ePackage.getEFactoryInstance();
        EObject eObject = eFactoryInstance.create(this.eClass);
        eObject.eSet(this.eAttribute, id);
        return eObject;
    }

}
