/*******************************************************************************
 * Copyright (c) 2026 Obeo.
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import com.sun.management.ThreadMXBean;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.omg.sysml.lang.sysml.SysMLPackage;
import org.omg.sysml.logic.SysMLLogicStandaloneSetup;

/** Standalone real-corpus save/load benchmark; no filesystem I/O or validation inside timing. */
public class SysMLBenchmark {
    private final BenchmarkIDManager identifiers = new BenchmarkIDManager();

    public static void main(String[] args) throws Exception {
        if (args.length < 3 || !List.of("prepare", "save", "load").contains(args[0])) {
            throw new IllegalArgumentException("prepare|save|load corpus.bin output-directory [warmup=10] [iterations=30] [recording.jfr]");
        }
        SysMLLogicStandaloneSetup.doSetup();
        new SysMLBenchmark().run(args);
    }

    private void run(String[] args) throws Exception {
        var directory = Path.of(args[2]);
        var operation = args[0];
        System.err.println("emfjson=" + JsonResourceImpl.class.getProtectionDomain().getCodeSource().getLocation());
        for (var type : List.of(URI.class, ResourceSetImpl.class, XMIResourceImpl.class, com.google.gson.Gson.class,
                SysMLPackage.class, SysMLLogicStandaloneSetup.class)) {
            System.err.println(type.getName() + "=" + type.getProtectionDomain().getCodeSource().getLocation());
        }
        if (operation.equals("prepare")) {
            Files.createDirectories(directory);
            var source = this.source(Path.of(args[1]));
            var expected = this.fingerprint(source);
            var bytes = this.save(source);
            this.validate(this.load(bytes), expected);
            Files.write(directory.resolve("baseline.json"), bytes);
            Files.writeString(directory.resolve("model.sha256"), expected);
            System.err.println("Validated baseline: " + bytes.length + " JSON bytes; fingerprint=" + expected);
            return;
        }
        var expected = Files.readString(directory.resolve("model.sha256"));
        var baseline = Files.readAllBytes(directory.resolve("baseline.json"));
        var source = operation.equals("save") ? this.source(Path.of(args[1])) : null;
        if (source != null) {
            this.validate(source, expected);
        }
        int warmup = args.length > 3 ? Integer.parseInt(args[3]) : 10;
        int iterations = args.length > 4 ? Integer.parseInt(args[4]) : 30;
        if (warmup < 0 || iterations < 1) {
            throw new IllegalArgumentException("Require warmup >= 0 and iterations >= 1");
        }
        var thread = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        thread.setThreadAllocatedMemoryEnabled(true);
        thread.setThreadCpuTimeEnabled(true);
        long threadId = Thread.currentThread().threadId();
        byte[] bytes = baseline;
        JsonResourceImpl loaded = null;
        long[][] samples = new long[iterations][4];
        try (var recording = args.length > 5 ? new Recording(Configuration.getConfiguration("profile")) : null) {
            var measurement = new Measurement();
            if (recording != null) {
                recording.enable("jdk.GCCPUTime");
                recording.enable("emfjson.Measurement");
                recording.start();
            }
            for (int iteration = -warmup; iteration < iterations; iteration++) {
                if (iteration == 0 && recording != null) {
                    measurement.begin();
                }
                long allocation = thread.getThreadAllocatedBytes(threadId);
                long cpu = thread.getCurrentThreadCpuTime();
                long wall = System.nanoTime();
                if (operation.equals("save")) {
                    bytes = this.save(source);
                } else {
                    loaded = this.load(baseline);
                }
                long elapsed = System.nanoTime() - wall;
                long consumed = thread.getCurrentThreadCpuTime() - cpu;
                long allocated = thread.getThreadAllocatedBytes(threadId) - allocation;
                if (iteration >= 0) {
                    samples[iteration] = new long[] {elapsed, consumed, allocated, bytes.length};
                }
            }
            if (recording != null) {
                measurement.end();
                measurement.commit();
                recording.stop();
                recording.dump(Path.of(args[5]));
            }
        }
        if (operation.equals("save")) {
            if (!Arrays.equals(baseline, bytes)) {
                throw new IllegalStateException("Saved bytes differ from baseline JSON");
            }
            loaded = this.load(bytes);
        }
        this.validate(loaded, expected);
        System.out.println("operation,iteration,wall_ns,cpu_ns,allocated_bytes,output_bytes");
        for (int index = 0; index < samples.length; index++) {
            var sample = samples[index];
            System.out.printf("%s,%d,%d,%d,%d,%d%n", operation, index, sample[0], sample[1], sample[2], sample[3]);
        }
    }

    private JsonResourceImpl resource() {
        var resource = new JsonResourceImpl(URI.createURI("sirius:///apollo-11"), Map.of(
                JsonResource.OPTION_ID_MANAGER, this.identifiers,
                JsonResource.OPTION_DISPLAY_DYNAMIC_INSTANCES, true));
        resource.setIntrinsicIDToEObjectMap(new HashMap<>());
        var resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(SysMLPackage.eNS_URI, SysMLPackage.eINSTANCE);
        resourceSet.getResources().add(resource);
        return resource;
    }

    private JsonResourceImpl source(Path path) throws Exception {
        var binary = new XMIResourceImpl(URI.createFileURI(path.toAbsolutePath().toString()));
        var resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(SysMLPackage.eNS_URI, SysMLPackage.eINSTANCE);
        resourceSet.getResources().add(binary);
        binary.load(Map.of(XMLResource.OPTION_BINARY, true));
        var objects = binary.getAllContents();
        while (objects.hasNext()) {
            var object = objects.next();
            var id = binary.getID(object);
            if (id == null) {
                throw new IllegalStateException("Binary EObject missing ID: " + object.eClass().getName());
            }
            this.identifiers.setId(object, id);
        }
        var resource = this.resource();
        resource.getContents().addAll(new ArrayList<>(binary.getContents()));
        return resource;
    }

    private byte[] save(JsonResourceImpl resource) throws Exception {
        var output = new ByteArrayOutputStream();
        var namespaces = new ArrayList<Map.Entry<String, String>>();
        var listener = new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onNsHeaderEntryAdded(String prefix, String uri) {
                namespaces.add(Map.entry(prefix, uri));
            }
        };
        resource.save(output, Map.of(JsonResource.OPTION_ID_MANAGER, this.identifiers,
                JsonResource.OPTION_SCHEMA_LOCATION, true,
                JsonResource.OPTION_SERIALIZATION_LISTENER, listener));
        return output.toByteArray();
    }

    private JsonResourceImpl load(byte[] bytes) throws Exception {
        var resource = this.resource();
        resource.load(new ByteArrayInputStream(bytes), Map.of());
        return resource;
    }

    private void validate(JsonResourceImpl resource, String expected) throws Exception {
        if (!resource.getErrors().isEmpty() || !resource.getWarnings().isEmpty()
                || !expected.equals(this.fingerprint(resource))) {
            throw new IllegalStateException("Round-trip changed IDs, persistent model structure or diagnostics");
        }
    }

    private String fingerprint(JsonResourceImpl resource) throws Exception {
        var digest = MessageDigest.getInstance("SHA-256");
        var ids = new HashSet<String>();
        try (var output = new DataOutputStream(new DigestOutputStream(OutputStream.nullOutputStream(), digest))) {
            output.writeInt(resource.getContents().size());
            var objects = resource.getAllContents();
            while (objects.hasNext()) {
                var object = objects.next();
                var id = resource.getID(object);
                if (id == null || !ids.add(id) || resource.getEObject(id) != object) {
                    throw new IllegalStateException("Missing, duplicate or unresolvable ID: " + id);
                }
                this.text(output, id);
                this.text(output, EcoreUtil.getURI(object.eClass()).toString());
                this.text(output, object.eContainer() == null ? "" : resource.getID(object.eContainer()));
                for (var feature : object.eClass().getEAllStructuralFeatures()) {
                    if (feature.isDerived() || feature.isTransient() || feature.isVolatile()) {
                        continue;
                    }
                    this.text(output, feature.getName());
                    if (feature.isUnsettable()) {
                        output.writeBoolean(object.eIsSet(feature));
                    }
                    var value = object.eGet(feature);
                    var values = feature.isMany() ? (List<?>) value : value == null ? List.of() : List.of(value);
                    output.writeInt(values.size());
                    for (var item : values) {
                        if (feature instanceof EAttribute attribute) {
                            this.text(output, EcoreUtil.convertToString(attribute.getEAttributeType(), item));
                        } else {
                            var target = (EObject) item;
                            if (target.eIsProxy() || target.eResource() != resource) {
                                throw new IllegalStateException("Unexpected external or unresolved reference: " + feature.getName());
                            }
                            this.text(output, resource.getID(target));
                        }
                    }
                }
            }
        }
        if (resource.getResourceSet().getResources().size() != 1) {
            throw new IllegalStateException("Validation loaded an external resource");
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private void text(DataOutputStream output, String value) throws Exception {
        var bytes = value.getBytes(StandardCharsets.UTF_8);
        output.writeInt(bytes.length);
        output.write(bytes);
    }
}
