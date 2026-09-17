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
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResourceImpl;
import org.junit.Test;

/**
 * Checks character encoding and the existing resource-handler stream boundary.
 */
public class JsonWriterTests {

    @Test
    public void propagatesFailureWhenFlushingBufferedCharacters() {
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        resource.getContents().add(EcoreFactory.eINSTANCE.createEClass());
        var failure = new IOException("Cannot write JSON");
        var output = new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                throw failure;
            }
        };
        assertThatThrownBy(() -> resource.save(output, Map.of())).isSameAs(failure);
    }

    @Test
    public void writesCompleteUnicodeContentWithConfiguredEncodingAndIndent() throws Exception {
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        var model = EcoreFactory.eINSTANCE.createEClass();
        model.setName("é漢字😀\n\"\\<&>".repeat(3000));
        resource.getContents().add(model);
        for (String encoding : new String[] {"UTF-8", "UTF-16LE"}) {
            for (String indent : new String[] {"", "    "}) {
                var options = Map.of(JsonResource.OPTION_ENCODING, encoding, JsonResource.OPTION_PRETTY_PRINTING_INDENT, indent);
                var expected = new StringWriter();
                var writer = new JsonWriter(expected);
                writer.setIndent(indent);
                var tree = JsonResourceImpl.toJsonTree(resource, options).getAsJsonObject();
                tree.getAsJsonObject("json").addProperty("encoding", encoding);
                new GsonBuilder().disableHtmlEscaping().create().toJson(tree, writer);
                writer.close();
                var actual = new ByteArrayOutputStream();
                resource.save(actual, options);
                assertThat(actual.toByteArray()).isEqualTo(expected.toString().getBytes(Charset.forName(encoding)));
            }
        }
    }

    @Test
    public void preservesUnflushedPostSaveHandlerBoundary() throws Exception {
        var resource = new JsonResourceImpl(URI.createURI("test.json"), Map.of());
        var model = EcoreFactory.eINSTANCE.createEClass();
        model.setName("x".repeat(12000));
        resource.getContents().add(model);
        var expected = new ByteArrayOutputStream();
        var writer = new JsonWriter(new OutputStreamWriter(expected, StandardCharsets.UTF_8));
        new GsonBuilder().disableHtmlEscaping().create().toJson(JsonResourceImpl.toJsonTree(resource, Map.of()), writer);
        int visibleBeforeClose = expected.size();
        assertThat(visibleBeforeClose).isPositive();
        writer.close();
        var actual = new ByteArrayOutputStream();
        var handler = new JsonResource.ResourceHandler() {
            @Override
            public void preLoad(JsonResource value, InputStream stream, Map<?, ?> options) {
            }

            @Override
            public void postLoad(JsonResource value, InputStream stream, Map<?, ?> options) {
            }

            @Override
            public void preSave(JsonResource value, OutputStream stream, Map<?, ?> options) {
            }

            @Override
            public void postSave(JsonResource value, OutputStream stream, Map<?, ?> options) {
                assertThat(actual.size()).isEqualTo(visibleBeforeClose);
            }
        };
        resource.save(actual, Map.of(JsonResource.OPTION_RESOURCE_HANDLER, handler));
        assertThat(actual.toByteArray()).isEqualTo(expected.toByteArray()).isNotEmpty();
    }
}
