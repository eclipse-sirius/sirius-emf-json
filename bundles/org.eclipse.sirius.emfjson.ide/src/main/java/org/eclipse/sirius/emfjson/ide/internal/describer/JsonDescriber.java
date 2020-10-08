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
package org.eclipse.sirius.emfjson.ide.internal.describer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;

/**
 * This class is used to test the json content type.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 *
 */
public class JsonDescriber implements ITextContentDescriber {

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.content.IContentDescriber#getSupportedOptions()
     */
    @Override
    public QualifiedName[] getSupportedOptions() {
        return new QualifiedName[0];
    }

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.content.IContentDescriber#describe(InputStream, IContentDescription)
     */
    @Override
    public int describe(InputStream contents, IContentDescription description) throws IOException {
        return this.describe(new InputStreamReader(contents), description);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.content.ITextContentDescriber#describe(Reader, IContentDescription)
     */
    @Override
    public int describe(Reader contents, IContentDescription description) throws IOException {
        contents.read(); // read first opened json character '{'
        StringBuilder builder = new StringBuilder();
        char read = (char) contents.read(); // if it's the expected describer this readed character can be 'j' or a
                                            // whitespace
        int charToRead = 5;
        int numberOfReadedChar = 0;
        while (numberOfReadedChar < charToRead && read != '{') {
            numberOfReadedChar++;
            if (Character.isWhitespace(read) || read == '"' || read == ':') {
                numberOfReadedChar--;
            } else {
                builder.append(read);
            }
            read = (char) contents.read();
        }
        // here StringBuilder normally contain the string "json"
        if (JsonResourceFactoryImpl.EXTENSION.equals(builder.toString())) {
            return IContentDescriber.VALID;
        }
        return IContentDescriber.INVALID;
    }

}
