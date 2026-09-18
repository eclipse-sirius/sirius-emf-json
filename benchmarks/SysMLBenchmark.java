/*******************************************************************************
 * Copyright (c) 2026 Obeo.
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.eclipse.sirius.emfjson.utils.GsonEObjectDeserializer;
import org.eclipse.sirius.emfjson.utils.GsonEObjectSerializer;
import org.omg.sysml.lang.sysml.SysMLPackage;
import org.omg.sysml.logic.SysMLLogicStandaloneSetup;

/** Standalone real-corpus save/load benchmark; no filesystem I/O or validation inside timing. */
public class SysMLBenchmark {
    private final BenchmarkIDManager identifiers = new BenchmarkIDManager();

    public static void main(String[] args) throws Exception {
        if (args.length < 3 || !List.of("prepare", "save", "binary-save", "load", "save-string", "load-string", "tree", "emit", "parse", "materialize").contains(args[0])) {
            throw new IllegalArgumentException("prepare|save|binary-save|load|save-string|load-string|tree|emit|parse|materialize corpus.bin output-directory [warmup=10] [iterations=30] [recording.jfr]");
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
        var source = operation.startsWith("save") || operation.equals("tree") ? this.source(Path.of(args[1])) : null;
        var binarySource = operation.equals("binary-save") ? this.binarySource(Path.of(args[1])) : null;
        var tree = List.of("emit", "materialize").contains(operation) ? this.parse(baseline) : null;
        var text = operation.equals("load-string") ? new String(baseline, StandardCharsets.UTF_8) : null;
        if (source != null) {
            this.validate(source, expected);
        }
        if (binarySource != null) {
            this.validate(binarySource, expected);
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
        Object result = null;
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
                result = switch (operation) {
                    case "save" -> this.save(source);
                    case "binary-save" -> this.saveBinary(binarySource);
                    case "load" -> this.load(baseline);
                    case "save-string" -> this.output(source).toString(StandardCharsets.UTF_8);
                    case "load-string" -> this.load(text.getBytes(StandardCharsets.UTF_8));
                    case "tree" -> new GsonEObjectSerializer(source, this.saveOptions()).serialize(source.getContents(), List.class, null);
                    case "emit" -> this.emit(tree);
                    case "parse" -> this.parse(baseline);
                    case "materialize" -> this.materialize(tree);
                    default -> throw new IllegalArgumentException(operation);
                };
                long elapsed = System.nanoTime() - wall;
                long consumed = thread.getCurrentThreadCpuTime() - cpu;
                long allocated = thread.getThreadAllocatedBytes(threadId) - allocation;
                if (iteration >= 0) {
                    long outputBytes = result instanceof byte[] bytes ? bytes.length : baseline.length;
                    samples[iteration] = new long[] {elapsed, consumed, allocated, outputBytes};
                }
            }
            if (recording != null) {
                measurement.end();
                measurement.commit();
                recording.stop();
                recording.dump(Path.of(args[5]));
            }
        }
        Resource loaded;
        if (operation.equals("binary-save")) {
            loaded = this.loadBinary((byte[]) result);
        } else if (result instanceof JsonResourceImpl resource) {
            loaded = resource;
        } else {
            byte[] bytes = switch (result) {
                case byte[] data -> data;
                case String data -> data.getBytes(StandardCharsets.UTF_8);
                case JsonElement data -> this.emit(data);
                default -> throw new IllegalStateException("Missing benchmark result");
            };
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
        var binary = this.binarySource(path);
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

    private XMIResourceImpl binarySource(Path path) throws Exception {
        var binary = new XMIResourceImpl(URI.createFileURI(path.toAbsolutePath().toString()));
        var resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(SysMLPackage.eNS_URI, SysMLPackage.eINSTANCE);
        resourceSet.getResources().add(binary);
        binary.load(Map.of(XMLResource.OPTION_BINARY, true));
        binary.setURI(URI.createURI("sirius:///apollo-11"));
        return binary;
    }

    private byte[] save(JsonResourceImpl resource) throws Exception {
        return this.output(resource).toByteArray();
    }

    private byte[] saveBinary(XMIResourceImpl resource) throws Exception {
        var output = new ByteArrayOutputStream();
        resource.save(output, Map.of(XMLResource.OPTION_BINARY, true));
        return output.toByteArray();
    }

    private ByteArrayOutputStream output(JsonResourceImpl resource) throws Exception {
        var output = new ByteArrayOutputStream();
        resource.save(output, this.saveOptions());
        return output;
    }

    private Map<?, ?> saveOptions() {
        var namespaces = new ArrayList<Map.Entry<String, String>>();
        var listener = new JsonResource.ISerializationListener.NoOp() {
            @Override
            public void onNsHeaderEntryAdded(String prefix, String uri) {
                namespaces.add(Map.entry(prefix, uri));
            }
        };
        return Map.of(JsonResource.OPTION_ID_MANAGER, this.identifiers,
                JsonResource.OPTION_SCHEMA_LOCATION, true,
                JsonResource.OPTION_SERIALIZATION_LISTENER, listener,
                JsonResource.OPTION_DISPLAY_DYNAMIC_INSTANCES, true,
                JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);
    }

    private JsonElement parse(byte[] bytes) throws Exception {
        try (var reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8))) {
            return new Gson().fromJson(reader, JsonElement.class);
        }
    }

    private byte[] emit(JsonElement tree) throws Exception {
        var output = new ByteArrayOutputStream();
        try (var writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8)))) {
            new GsonBuilder().disableHtmlEscaping().create().toJson(tree, writer);
        }
        return output.toByteArray();
    }

    private JsonResourceImpl materialize(JsonElement tree) {
        var resource = this.resource();
        new GsonEObjectDeserializer(resource, Map.of(JsonResource.OPTION_ID_MANAGER, this.identifiers,
                JsonResource.OPTION_DISPLAY_DYNAMIC_INSTANCES, true,
                JsonResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8)).deserialize(tree, List.class, null);
        return resource;
    }

    private JsonResourceImpl load(byte[] bytes) throws Exception {
        var resource = this.resource();
        resource.load(new ByteArrayInputStream(bytes), Map.of());
        return resource;
    }

    private XMIResourceImpl loadBinary(byte[] bytes) throws Exception {
        var resource = new XMIResourceImpl(URI.createURI("sirius:///apollo-11.bin"));
        var resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(SysMLPackage.eNS_URI, SysMLPackage.eINSTANCE);
        resourceSet.getResources().add(resource);
        resource.load(new ByteArrayInputStream(bytes), Map.of(XMLResource.OPTION_BINARY, true));
        return resource;
    }

    private void validate(Resource resource, String expected) throws Exception {
        if (!resource.getErrors().isEmpty() || !resource.getWarnings().isEmpty()
                || !expected.equals(this.fingerprint(resource))) {
            throw new IllegalStateException("Round-trip changed IDs, persistent model structure or diagnostics");
        }
    }

    private String fingerprint(Resource resource) throws Exception {
        var digest = MessageDigest.getInstance("SHA-256");
        var ids = new HashSet<String>();
        try (var output = new DataOutputStream(new DigestOutputStream(OutputStream.nullOutputStream(), digest))) {
            output.writeInt(resource.getContents().size());
            var objects = resource.getAllContents();
            while (objects.hasNext()) {
                var object = objects.next();
                var id = this.getID(resource, object);
                if (id == null || !ids.add(id) || resource.getEObject(id) != object) {
                    throw new IllegalStateException("Missing, duplicate or unresolvable ID: " + id);
                }
                this.text(output, id);
                this.text(output, EcoreUtil.getURI(object.eClass()).toString());
                this.text(output, object.eContainer() == null ? "" : this.getID(resource, object.eContainer()));
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
                            this.text(output, this.getID(resource, target));
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

    private String getID(Resource resource, EObject object) {
        if (resource instanceof JsonResourceImpl jsonResource) {
            return jsonResource.getID(object);
        }
        return ((XMLResource) resource).getID(object);
    }

    private void text(DataOutputStream output, String value) throws Exception {
        var bytes = value.getBytes(StandardCharsets.UTF_8);
        output.writeInt(bytes.length);
        output.write(bytes);
    }
}
