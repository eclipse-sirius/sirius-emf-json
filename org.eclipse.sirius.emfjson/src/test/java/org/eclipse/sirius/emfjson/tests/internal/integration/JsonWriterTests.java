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
package org.eclipse.sirius.emfjson.tests.internal.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.junit.Test;

/**
 * Verifies that buffering preserves encoded JSON and the bytes visible to postSave before writer closure.
 */
public class JsonWriterTests {

    @Test
    public void savePropagatesIOExceptionFromFinalBufferFlush() {
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        // This small document stays buffered until save closes its writer.
        resource.getContents().add(EcoreFactory.eINSTANCE.createEClass());
        var writeFailure = new IOException("Cannot write JSON");
        var failingOutput = new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                throw writeFailure;
            }
        };
        assertThatThrownBy(() -> resource.save(failingOutput, Map.of())).isSameAs(writeFailure);
    }

    @Test
    public void bufferedSaveMatchesGsonForLargeUnicodeValuesWithEachEncodingAndIndent() throws Exception {
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        var eClass = EcoreFactory.eINSTANCE.createEClass();
        // Exceed the buffers with multibyte characters, a surrogate pair, and JSON/HTML-sensitive characters.
        eClass.setName("é漢字😀\n\"\\<&>".repeat(3000));
        resource.getContents().add(eClass);
        for (String encoding : new String[] {"UTF-8", "UTF-16LE"}) {
            for (String indent : new String[] {"", "    "}) {
                var options = Map.of(JsonResource.OPTION_ENCODING, encoding, JsonResource.OPTION_PRETTY_PRINTING_INDENT, indent);
                String expectedJson = this.renderExpectedJsonWithGson(resource, options, encoding, indent);

                var actualBytes = new ByteArrayOutputStream();
                resource.save(actualBytes, options);

                assertThat(actualBytes.toByteArray())
                        .as("saved bytes with encoding %s and indent width %s", encoding, indent.length())
                        .isEqualTo(expectedJson.getBytes(Charset.forName(encoding)));
            }
        }
    }

    @Test
    public void postSaveSeesTheSameUnflushedBytesAsDirectEncoding() throws Exception {
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        var eClass = EcoreFactory.eINSTANCE.createEClass();
        // Force some bytes out while leaving a final partial buffer for writer.close().
        eClass.setName("x".repeat(12000));
        resource.getContents().add(eClass);
        var referenceBytes = new ByteArrayOutputStream();
        var referenceWriter = new JsonWriter(new OutputStreamWriter(referenceBytes, StandardCharsets.UTF_8));
        new GsonBuilder().disableHtmlEscaping().create().toJson(JsonResourceImpl.toJsonTree(resource, Map.of()), referenceWriter);
        int byteCountBeforeClose = referenceBytes.size();
        assertThat(byteCountBeforeClose).isPositive();
        referenceWriter.close();
        assertThat(byteCountBeforeClose).isLessThan(referenceBytes.size());

        var actualBytes = new ByteArrayOutputStream();
        var byteCountsObservedByPostSave = new ArrayList<Integer>();
        var handler = this.recordByteCountOnPostSave(actualBytes, byteCountsObservedByPostSave);
        resource.save(actualBytes, Map.of(JsonResource.OPTION_RESOURCE_HANDLER, handler));

        assertThat(byteCountsObservedByPostSave).containsExactly(byteCountBeforeClose);
        assertThat(actualBytes.toByteArray()).isEqualTo(referenceBytes.toByteArray()).isNotEmpty();
    }

    private String renderExpectedJsonWithGson(JsonResource resource, Map<?, ?> options, String encoding, String indent) throws IOException {
        var expectedJson = new StringWriter();
        var writer = new JsonWriter(expectedJson);
        writer.setIndent(indent);
        var tree = JsonResourceImpl.toJsonTree(resource, options).getAsJsonObject();
        // toJsonTree fixes the header to UTF-8; match the encoding passed to save.
        tree.getAsJsonObject("json").addProperty("encoding", encoding);
        new GsonBuilder().disableHtmlEscaping().create().toJson(tree, writer);
        writer.close();
        return expectedJson.toString();
    }

    private JsonResource.ResourceHandler recordByteCountOnPostSave(ByteArrayOutputStream output, List<Integer> observedByteCounts) {
        return new JsonResource.ResourceHandler() {
            @Override
            public void preLoad(JsonResource resource, InputStream inputStream, Map<?, ?> options) {
            }

            @Override
            public void postLoad(JsonResource resource, InputStream inputStream, Map<?, ?> options) {
            }

            @Override
            public void preSave(JsonResource resource, OutputStream outputStream, Map<?, ?> options) {
            }

            @Override
            public void postSave(JsonResource resource, OutputStream outputStream, Map<?, ?> options) {
                observedByteCounts.add(output.size());
            }
        };
    }
}
