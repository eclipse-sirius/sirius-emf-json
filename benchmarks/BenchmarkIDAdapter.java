/*******************************************************************************
 * Copyright (c) 2026 Obeo.
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import java.util.UUID;

import org.eclipse.emf.common.notify.impl.AdapterImpl;

/** UUID storage equivalent to Sirius Web's IDAdapter, for the standalone benchmark. */
public class BenchmarkIDAdapter extends AdapterImpl {
    private UUID id;

    public BenchmarkIDAdapter(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
