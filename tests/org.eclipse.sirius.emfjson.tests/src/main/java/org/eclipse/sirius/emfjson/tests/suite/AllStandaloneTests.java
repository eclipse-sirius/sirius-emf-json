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
package org.eclipse.sirius.emfjson.tests.suite;

import org.eclipse.sirius.emfjson.tests.internal.integration.JsonSerializationAPITests;
import org.eclipse.sirius.emfjson.tests.internal.integration.SerializeCommonObjectWithResourceAsAttribute;
import org.eclipse.sirius.emfjson.tests.internal.integration.StandaloneIntegrationTests;
import org.eclipse.sirius.emfjson.tests.internal.options.EObjectHandlerTests;
import org.eclipse.sirius.emfjson.tests.internal.options.IDManagerOptionsTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.RemoveObjectTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.load.AnnotationLoadTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.load.AttributesLoadTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.load.ClassLoadTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.load.ContainmentReferencesLoadTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.load.DataTypeLoadTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.load.DeserializeOptionsTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.load.EnumerationsLoadTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.load.NonContainmentReferencesLoadTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.load.OperationLoadTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.load.SubPackageLoadTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.load.TypeParametersLoadTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.AnnotationSaveTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.AttributesSaveTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.ClassSaveTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.ContainmentReferencesSaveTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.DataTypeSaveTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.EnumerationsSaveTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.ExtendedMetaDataTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.NonContainmentReferencesSaveTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.OperationsSaveTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.SerializeOptionsTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.SubPackageSaveTests;
import org.eclipse.sirius.emfjson.tests.internal.unit.save.TypeParametersSaveTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.textui.TestRunner;

/**
 * This suite launch all the tests defined for the emfJson project.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
@RunWith(Suite.class)
@SuiteClasses({ AnnotationSaveTests.class, AttributesSaveTests.class, ClassSaveTests.class, DataTypeSaveTests.class, EnumerationsSaveTests.class, ExtendedMetaDataTests.class,
        OperationsSaveTests.class, ContainmentReferencesSaveTests.class, NonContainmentReferencesSaveTests.class, SerializeOptionsTests.class, SubPackageSaveTests.class, TypeParametersSaveTests.class,
        StandaloneIntegrationTests.class, AnnotationLoadTests.class, AttributesLoadTests.class, ClassLoadTests.class, DataTypeLoadTests.class, EnumerationsLoadTests.class, OperationLoadTests.class,
        ContainmentReferencesLoadTests.class, NonContainmentReferencesLoadTests.class, DeserializeOptionsTests.class, SubPackageLoadTests.class, TypeParametersLoadTests.class,
        EObjectHandlerTests.class, JsonSerializationAPITests.class, SerializeCommonObjectWithResourceAsAttribute.class, IDManagerOptionsTests.class, RemoveObjectTests.class })
public final class AllStandaloneTests {

    /**
     * The constructor.
     */
    private AllStandaloneTests() {
    }

    /**
     * Launches the test with the given arguments.
     *
     * @param args
     *            Arguments of the testCase.
     */
    public static void main(String[] args) {
        TestRunner.run(AllStandaloneTests.suite());
    }

    /**
     * Creates the {@link junit.framework.TestSuite TestSuite} for all the test.
     *
     * @return The test suite containing all the tests
     */
    public static Test suite() {
        return new JUnit4TestAdapter(AllStandaloneTests.class);
    }

}
