/*******************************************************************************
 * Copyright (c) 2026 Obeo.
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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonElement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.junit.Test;

/**
 * Checks that repeated lexical class names do not freeze package resolution.
 */
public class EClassResolutionTests {

    @Test
    public void packageRegistryChangesRemainVisibleBetweenObjects() throws IOException {
        EPackage firstPackage = this.createPackage("first");
        EPackage secondPackage = this.createPackage("second");
        var resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put("urn:model", firstPackage);
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        resourceSet.getResources().add(resource);
        var handler = new JsonResource.IEObjectHandler() {
            private boolean first = true;

            @Override
            public JsonElement processSerializedContent(JsonElement json, EObject object) {
                return json;
            }

            @Override
            public EObject processDeserializedContent(EObject object, JsonElement json) {
                if (this.first) {
                    resourceSet.getPackageRegistry().put("urn:model", secondPackage);
                    this.first = false;
                }
                return object;
            }
        };
        String json = "{\"json\":{},\"ns\":{\"m\":\"urn:model\"},\"content\":[{\"eClass\":\"m:Node\"},{\"eClass\":\"m:Node\"}]}";
        resource.load(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), Map.of(JsonResource.OPTION_EOBJECT_HANDLER, handler));

        assertThat(resource.getContents()).extracting(EObject::eClass)
                .containsExactly((EClass) firstPackage.getEClassifier("Node"), (EClass) secondPackage.getEClassifier("Node"));
    }

    private EPackage createPackage(String name) {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName(name);
        ePackage.setNsPrefix("m");
        ePackage.setNsURI("urn:model");
        EClass eClass = EcoreFactory.eINSTANCE.createEClass();
        eClass.setName("Node");
        ePackage.getEClassifiers().add(eClass);
        return ePackage;
    }
}
