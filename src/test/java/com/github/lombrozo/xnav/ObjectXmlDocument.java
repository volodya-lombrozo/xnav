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
 * Xml document as an object.
 * @since 0.1
 */
@EqualsAndHashCode
final class ObjectXmlDocument implements Xml {

    /**
     * Element.
     */
    private final Xml element;

    /**
     * Constructor.
     * @param element Root element.
     * */
    ObjectXmlDocument(final Xml element) {
        this.element = element;
    }

    @Override
    public Xml child(final String element) {
        final Xml result;
        if (this.element.name().equals(element)) {
            result = this.element;
        } else {
            result = new Empty();
        }
        return result;
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return this.element.attribute(name);
    }

    @Override
    public Optional<String> text() {
        return this.element.text();
    }

    @Override
    public Stream<Xml> children() {
        return Stream.of(this.element);
    }

    @Override
    public String name() {
        return "#document";
    }

    @Override
    public Xml copy() {
        return new ObjectXmlDocument(this.element.copy());
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Document cannot be converted to a Node.");
    }

    @Override
    public String toString() {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s",
            this.element.toString()
        );
    }
}
