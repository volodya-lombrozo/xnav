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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

/**
 * XML content as an object.
 * @since 0.1
 */
@EqualsAndHashCode
public final class ObjectContent implements Xml {

    /**
     * All elements.
     */
    private final List<Xml> all;

    /**
     * Constructor.
     * @param all All elements
     */
    ObjectContent(final List<Xml> all) {
        this.all = all;
    }

    @Override
    public Xml child(final String element) {
        return this.elements()
            .filter(e -> e.name().equals(element))
            .findFirst()
            .orElse(new Empty());
    }

    @Override
    public Stream<Xml> children() {
        return this.all.stream();
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        throw new UnsupportedOperationException("XML content does not have attributes.");
    }

    @Override
    public Optional<String> text() {
        return Optional.of(
            this.all.stream()
                .map(Xml::text)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining())
        );
    }

    @Override
    public String name() {
        throw new UnsupportedOperationException("XML content does not have a name.");
    }

    @Override
    public Xml copy() {
        return new ObjectContent(this.all.stream().map(Xml::copy).collect(Collectors.toList()));
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("XML content can't be converted to a node.");
    }

    @Override
    public String toString() {
        return this.all.stream().map(Object::toString).collect(Collectors.joining());
    }

    /**
     * Get all elements.
     * @return All elements.
     */
    private Stream<Xml> elements() {
        return this.all.stream().filter(ObjectElement.class::isInstance);
    }
}
