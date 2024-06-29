/*******************************************************************************
 * Copyright (c) 2023 Obeo.
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

package model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// CHECKSTYLE:OFF
public class TestPojoDataTypeImpl {

    /**
     * A String value.
     */
    private String stringValue = null;

    /**
     * An integer value.
     */
    private int intValue = 0;

    /**
     * Not serialized.
     */
    private transient int transientIntValue = 0;

    /**
     * Empty constructor for Json serialization.
     *
     */
    public TestPojoDataTypeImpl() {

    }

    /**
     * Returns the stringValue.
     *
     * @return The stringValue
     */
    public String getStringValue() {
        return this.stringValue;
    }

    /**
     * Sets the stringValue.
     *
     * @param stringValue
     *            The stringValue to set
     */
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Returns the intValue.
     *
     * @return The intValue
     */
    public int getIntValue() {
        return this.intValue;
    }

    /**
     * Sets the intValue.
     *
     * @param intValue
     *            The intValue to set
     */
    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    /**
     * Returns the transientIntValue.
     *
     * @return The transientIntValue
     */
    public int getTransientIntValue() {
        return this.transientIntValue;
    }

    /**
     * Sets the transientIntValue.
     *
     * @param transientIntValue
     *            The transientIntValue to set
     */
    public void setTransientIntValue(int transientIntValue) {
        this.transientIntValue = transientIntValue;
    }

    /**
     * Used by the XMI serialization. This doesn't produce as a Json string on purpose, not to interfere with the Json
     * serialization.<br>
     *
     * @return A string representing this {@link TestPojoDataTypeImpl} instance.
     */
    @Override
    public String toString() {
        return String.format("TestPojoDataTypeImpl('%s', %d)", this.stringValue.replaceAll("'", "&apos;"), Integer.valueOf(this.intValue)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Used by the XMI deserialization. This doesn't take a Json string on purpose, not to interfere with the Json
     * deserialization.
     *
     * @param serialized
     *            The serialized form of a {@link TestPojoDataTypeImpl} as produced by
     *            {@link TestPojoDataTypeImpl#toString()}.
     * @return A new instance of {@link TestPojoDataTypeImpl} if the given serialized form is consistent, null
     *         otherwise.
     */
    public static TestPojoDataTypeImpl valueOf(String serialized) {
        TestPojoDataTypeImpl value = null;
        Matcher matcher = Pattern.compile("TestPojoDataTypeImpl\\('([^']*)', ([0-9]+)\\)").matcher(serialized); //$NON-NLS-1$
        if (matcher.matches()) {
            value = new TestPojoDataTypeImpl();
            value.setStringValue(matcher.group(1).replaceAll("&apos;", "'")); //$NON-NLS-1$ //$NON-NLS-2$
            value.setIntValue(Integer.parseInt(matcher.group(2)));
        }
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.intValue;
        result = prime * result + ((this.stringValue == null) ? 0 : this.stringValue.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        TestPojoDataTypeImpl other = (TestPojoDataTypeImpl) obj;
        if (this.intValue != other.intValue)
            return false;
        if (this.stringValue == null) {
            if (other.stringValue != null)
                return false;
        } else if (!this.stringValue.equals(other.stringValue))
            return false;
        return true;
    }

}
