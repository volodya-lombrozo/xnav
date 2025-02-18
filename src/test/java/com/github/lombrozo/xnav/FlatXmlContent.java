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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Node;

/**
 * Flat xml content.
 * This is a content of an element.
 * Might contain text and children elements.
 * @since 0.1
 */
@EqualsAndHashCode
@ToString
final class FlatXmlContent implements Xml {

    /**
     * Node id.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final int identifier;

    /**
     * Flat xml model.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final FlatXmlModel xml;

    /**
     * Constructor.
     * @param identifier Node id.
     * @param xml Flat xml model.
     */
    FlatXmlContent(final int identifier, final FlatXmlModel xml) {
        this.identifier = identifier;
        this.xml = xml;
    }

    @Override
    public Xml child(final String element) {
        return this.children()
            .filter(e -> e.name().equals(element))
            .findFirst()
            .orElse(new Empty());
    }

    @Override
    public Stream<Xml> children() {
        return this.xml.children(this.identifier);
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return Optional.empty();
    }

    @ToString.Include
    @EqualsAndHashCode.Include
    @Override
    public Optional<String> text() {
        return Optional.of(
            this.xml.children(this.identifier)
                .map(Xml::text)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining())
        );
    }

    @Override
    public String name() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Xml copy() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
