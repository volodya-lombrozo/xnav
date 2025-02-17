/*
 * MIT License
 *
 * Copyright (c) 2025 Volodya Lombrozo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.lombrozo.xnav;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

/**
 * Xml attribute as an object.
 * @since 0.1
 */
@EqualsAndHashCode
final class ObjectXmlAttribute implements Xml {

    /**
     * Attribute name.
     */
    private final String name;

    /**
     * Attribute value.
     */
    private final String value;

    /**
     * Constructor.
     * @param name Attribute name
     * @param value Attribute value
     */
    ObjectXmlAttribute(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public Xml child(final String element) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return Optional.empty();
    }

    @Override
    public Optional<String> text() {
        return Optional.of(this.value.substring(1, this.value.length() - 1));
    }

    @Override
    public Stream<Xml> children() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Xml copy() {
        return new ObjectXmlAttribute(this.name, this.value);
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String toString() {
        return String.format("%s=\"%s\"", this.name, this.value);
    }
}
