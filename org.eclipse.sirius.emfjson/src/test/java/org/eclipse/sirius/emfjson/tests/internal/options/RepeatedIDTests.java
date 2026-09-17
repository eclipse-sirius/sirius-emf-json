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
package org.eclipse.sirius.emfjson.tests.internal.options;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.sirius.emfjson.resource.IDManager;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.junit.Test;

/**
 * Checks repeated ID assignment without skipping ID-manager callbacks.
 */
public class RepeatedIDTests {
    @Test
    public void repeatedIDsPreserveCallbacksAndIndexEntries() {
        var ids = new IdentityHashMap<EObject, String>();
        var assignments = new AtomicInteger();
        var removals = new AtomicInteger();
        var manager = new IDManager() {
            @Override
            public String getOrCreateId(EObject object) {
                return ids.getOrDefault(object, "initial");
            }

            @Override
            public Optional<String> findId(EObject object) {
                return Optional.ofNullable(ids.get(object));
            }

            @Override
            public String setId(EObject object, String id) {
                assignments.incrementAndGet();
                return ids.put(object, id);
            }

            @Override
            public void clearId(EObject object) {
                ids.remove(object);
            }
        };
        var index = new HashMap<String, EObject>() {
            @Override
            public EObject remove(Object key) {
                removals.incrementAndGet();
                return super.remove(key);
            }
        };
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of(JsonResource.OPTION_ID_MANAGER, manager)) {
            {
                this.idToEObjectMap = index;
            }
        };
        var object = EcoreFactory.eINSTANCE.createEClass();
        resource.getContents().add(object);
        resource.setID(object, new String("initial"));
        assertThat(assignments.get()).isEqualTo(2);
        assertThat(removals.get()).isZero();
        assertThat(resource.getEObject("initial")).isSameAs(object);
        resource.setID(object, "changed");
        assertThat(resource.getEObject("initial")).isNull();
        assertThat(resource.getEObject("changed")).isSameAs(object);
        resource.getContents().remove(object);
        assertThat(resource.getEObject("changed")).isNull();
    }
}
