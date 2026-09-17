/*******************************************************************************
 * Copyright (c) 2026 Obeo.
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import jdk.jfr.Event;
import jdk.jfr.Name;

/** Bounds measured operations so profiles can exclude setup, warmup and validation. */
@Name("emfjson.Measurement")
public class Measurement extends Event {
}
