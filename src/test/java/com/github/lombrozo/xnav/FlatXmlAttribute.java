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
import lombok.ToString;
import org.w3c.dom.Node;

/**
 * Flat representation of XML attribute.
 * @since 0.1
 */
@EqualsAndHashCode
final class FlatXmlAttribute implements Xml {

    /**
     * Element id.
     */
    @EqualsAndHashCode.Exclude
    private final int identifier;

    /**
     * Model of flat xml.
     */
    @EqualsAndHashCode.Exclude
    private final FlatXmlModel xml;

    /**
     * Constructor.
     * @param identifier Element id.
     * @param xml Flat xml model.
     */
    FlatXmlAttribute(final int identifier, final FlatXmlModel xml) {
        this.identifier = identifier;
        this.xml = xml;
    }

    @Override
    public Xml child(final String element) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return Optional.empty();
    }

    @ToString.Include
    @EqualsAndHashCode.Include
    @Override
    public String name() {
        return this.full().split("=", 2)[0];
    }

    @ToString.Include
    @EqualsAndHashCode.Include
    @Override
    public Optional<String> text() {
        final String text = this.full().split("=", 2)[1];
        return Optional.of(text.substring(1, text.length() - 1));
    }

    @Override
    public Stream<Xml> children() {
        return Stream.empty();
    }

    @Override
    public Xml copy() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String toString() {
        return String.format("%s='%s'", this.name(), this.text().orElse(""));
    }

    /**
     * Entire content of the element.
     * @return Content.
     */
    private String full() {
        return this.xml.content(this.identifier);
    }
}
