/*******************************************************************************
 * Copyright (c) 2026 Obeo.
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import java.util.Optional;
import java.util.UUID;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.emfjson.resource.IDManager;

/** Mirrors Sirius Web EObjectIDManager's UUID conversions and linear adapter lookup. */
public class BenchmarkIDManager implements IDManager {
    @Override
    public String getOrCreateId(EObject object) {
        var adapter = this.findAdapter(object);
        return adapter == null ? UUID.randomUUID().toString() : adapter.getId().toString();
    }

    @Override
    public Optional<String> findId(EObject object) {
        var adapter = this.findAdapter(object);
        return adapter == null ? Optional.empty() : Optional.of(adapter.getId().toString());
    }

    @Override
    public void clearId(EObject object) {
        var iterator = object.eAdapters().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof BenchmarkIDAdapter) {
                iterator.remove();
            }
        }
    }

    @Override
    public String setId(EObject object, String id) {
        var adapter = this.findAdapter(object);
        if (adapter != null) {
            var previous = adapter.getId().toString();
            adapter.setId(UUID.fromString(id));
            return previous;
        }
        object.eAdapters().add(new BenchmarkIDAdapter(UUID.fromString(id)));
        return null;
    }

    private BenchmarkIDAdapter findAdapter(EObject object) {
        for (var adapter : object.eAdapters()) {
            if (adapter instanceof BenchmarkIDAdapter identifier) {
                return identifier;
            }
        }
        return null;
    }
}
