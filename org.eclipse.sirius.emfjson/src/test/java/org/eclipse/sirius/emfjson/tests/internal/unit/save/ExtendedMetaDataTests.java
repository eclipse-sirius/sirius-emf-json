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

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.tests.internal.AbstractEMFJsonTests;
import org.junit.Test;

/**
 * Class used to test the serialization of extendedMetaData.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class ExtendedMetaDataTests extends AbstractEMFJsonTests {

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
     * Test with the BasicExtendedMetaData class provide by EMF.
     */
    @Test
    public void testWithDefaultExtendedMetadata() {
        this.options.put(JsonResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
        this.testSave("TestWithDefaultExtendedMetadata.xmi"); //$NON-NLS-1$
    }

    /**
     * Test retrieved a package with an empty nsPrefix.
     */
    @Test
    public void testChangePackageWithEmptyNsPrefix() {
        // CHECKSTYLE:OFF
        ExtendedMetaData metaData = new BasicExtendedMetaData() {

            /**
             * {@inheritDoc}
             *
             * @see org.eclipse.emf.ecore.util.BasicExtendedMetaData#getPackage(java.lang.String)
             */
            @Override
            public EPackage getPackage(String namespace) {
                EPackage demandPackage = this.demandPackage(namespace);
                demandPackage.setNsPrefix(""); //$NON-NLS-1$
                demandPackage.setNsURI("http://www.obeo.fr"); //$NON-NLS-1$
                return demandPackage;
            }

            /**
             * {@inheritDoc}
             *
             * @see org.eclipse.emf.ecore.util.BasicExtendedMetaData#getNamespace(org.eclipse.emf.ecore.EPackage)
             */
            @Override
            public String getNamespace(EPackage ePackage) {
                if ("example".equals(ePackage.getNsPrefix())) { //$NON-NLS-1$
                    return ePackage.getNsURI();
                }
                return null;
            }

            /**
             * {@inheritDoc}
             *
             * @see org.eclipse.emf.ecore.util.BasicExtendedMetaData#getNamespace(org.eclipse.emf.ecore.EStructuralFeature)
             */
            @Override
            public String getNamespace(EStructuralFeature eStructuralFeature) {
                EAnnotation eAnnotation = eStructuralFeature.getEAnnotation(ExtendedMetaData.ANNOTATION_URI);
                if (eAnnotation != null) {
                    return "http://obeo.fr/emfjson"; //$NON-NLS-1$
                }
                return null;
            }

        };
        // CHECKSTYLE:ON
        this.options.put(JsonResource.OPTION_FORCE_PREFIX_ON_EMPTY_ONE, Boolean.TRUE);
        this.options.put(JsonResource.OPTION_EXTENDED_META_DATA, metaData);
        this.testSave("TestChangePackageWithEmptyNsPrefix.xmi"); //$NON-NLS-1$
    }

    /**
     * Test changing a class name with an empty String.
     */
    @Test
    public void testChangeClassNameWithEmptyString() {
        // CHECKSTYLE: OFF
        ExtendedMetaData metaData = new BasicExtendedMetaData() {

            /**
             * {@inheritDoc}
             *
             * @see org.eclipse.emf.ecore.util.BasicExtendedMetaData#getName(org.eclipse.emf.ecore.EClassifier)
             */
            @Override
            public String getName(EClassifier eClassifier) {
                return ""; //$NON-NLS-1$
            }
        };
        // CHECKSTYLE:ON
        this.options.put(JsonResource.OPTION_EXTENDED_META_DATA, metaData);
        this.testSave("TestChangeClassNameWithEmptyString.xmi"); //$NON-NLS-1$
    }

    /**
     * Test adding a namespace on a featureName.
     */
    @Test
    public void testAddNamespaceOnFeatureName() {
        // CHECKSTYLE: OFF
        ExtendedMetaData metaData = new BasicExtendedMetaData() {

            /**
             * {@inheritDoc}
             *
             * @see org.eclipse.emf.ecore.util.BasicExtendedMetaData#getNamespace(org.eclipse.emf.ecore.EStructuralFeature)
             */
            @Override
            public String getNamespace(EStructuralFeature eStructuralFeature) {
                EAnnotation eAnnotation = eStructuralFeature.getEAnnotation(ExtendedMetaData.ANNOTATION_URI);
                if (eAnnotation != null) {
                    return "http://obeo.fr/emfjson"; //$NON-NLS-1$
                }
                return null;
            }
        };
        // CHECKSTYLE:ON
        this.options.put(JsonResource.OPTION_EXTENDED_META_DATA, metaData);
        this.testSave("TestAddNamespaceOnFeatureName.xmi"); //$NON-NLS-1$
    }

    /**
     * Change the EClass name of an Object.
     */
    @Test
    public void testChangeClassName() {
        // CHECKSTYLE:OFF
        ExtendedMetaData metaData = new BasicExtendedMetaData() {

            @Override
            public EClassifier getType(EPackage ePackage, String name) {
                EClassifier eClassifier = super.getType(ePackage, name);
                return eClassifier;
            }

            @Override
            public String getName(EClassifier eClassifier) {
                EClassifier classifier = eClassifier;
                String name = null;
                if (classifier != null) {
                    if ("Node".equals(classifier.getName())) { //$NON-NLS-1$
                        super.setName(classifier, "metaNode"); //$NON-NLS-1$
                    }
                    name = super.getName(classifier);
                }

                return name;
            }

            @Override
            public String getName(EStructuralFeature eStructuralFeature) {
                String name = null;
                if (eStructuralFeature != null) {
                    name = eStructuralFeature.getName();
                    EClass eContainingClass = eStructuralFeature.getEContainingClass();
                    if ("NodeSingleValueAttribute".equals(eContainingClass.getName()) //$NON-NLS-1$
                            && "singleIntAttribute".equals(name)) { //$NON-NLS-1$
                        super.setName(eStructuralFeature, "intAttribute"); //$NON-NLS-1$
                    }
                    name = super.getName(eStructuralFeature);
                }
                return name;
            }
        };
        // CHECKSTYLE:ON
        this.options.put(JsonResource.OPTION_EXTENDED_META_DATA, metaData);
        this.testSave("TestChangeClassName.xmi"); //$NON-NLS-1$
    }

    /**
     * Test URIs conflict. Add a number as a suffix on older occurrences.
     */
    @Test
    public void testURIsConflict() {
        // CHECKSTYLE:OFF
        ExtendedMetaData metaData = new BasicExtendedMetaData() {

            private int nsuri = 0;

            private String namespaceAdress = "http://conflict.com/obeo_"; //$NON-NLS-1$

            /**
             * Depending on numbers of EStructuralFeature named "nsURI", this method create a new package with different
             * namespace but with the same nsPrefix. That create conflict during serialization if
             * BasicExtendedMetaData#getNamespace return conflicted namespace.
             *
             * @see org.eclipse.emf.ecore.util.BasicExtendedMetaData#getPackage(java.lang.String)
             */
            @Override
            public EPackage getPackage(String namespace) {
                EPackage demandPackage = null;
                demandPackage = this.demandPackage(this.namespaceAdress + this.nsuri);
                EPackage ePackage = (EPackage) this.demandRegistry.get(this.namespaceAdress + this.nsuri);
                ePackage.setNsPrefix("conflict"); //$NON-NLS-1$
                return demandPackage;
            }

            /**
             * {@inheritDoc}
             *
             * @see org.eclipse.emf.ecore.util.BasicExtendedMetaData#getName(org.eclipse.emf.ecore.EStructuralFeature)
             */
            @Override
            public String getName(EStructuralFeature eStructuralFeature) {
                String name = super.getName(eStructuralFeature);
                if (eStructuralFeature.getName().equals("nsURI")) {//$NON-NLS-1$

                    this.getPackage(null);
                }
                return name;
            }

            /**
             * Return conflicted namespace.
             *
             * @see org.eclipse.emf.ecore.util.BasicExtendedMetaData#getNamespace(org.eclipse.emf.ecore.EStructuralFeature)
             */
            @Override
            public String getNamespace(EStructuralFeature eStructuralFeature) {
                String namespace = null;
                if ("nsURI".equals(eStructuralFeature.getName())) { //$NON-NLS-1$
                    namespace = this.namespaceAdress + this.nsuri;
                    this.nsuri++;
                }
                return namespace;
            }
        };
        // CHECKSTYLE:ON
        this.options.put(JsonResource.OPTION_EXTENDED_META_DATA, metaData);
        this.testSave("TestURIsConflict.ecore"); //$NON-NLS-1$
    }

}
