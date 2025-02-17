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
import org.w3c.dom.Node;

/**
 * Flat representation of XML.
 * @since 0.1
 */
final class FlatXml implements Xml {

    /**
     * Document flat model.
     */
    private final FlatXmlModel doc;

    /**
     * Constructor.
     * @param xml XML string.
     */
    FlatXml(final String... xml) {
        this(String.join("", xml));
    }

    FlatXml(final String xml, final FlatParser parser){
        this(parser.parse(xml));
    }

    /**
     * Constructor.
     * @param xml XML string.
     */
    private FlatXml(final String xml) {
        this(xml, new FlatDom());
    }


    /**
     * Constructor.
     * @param doc Flat XML model.
     */
    FlatXml(final FlatXmlModel doc) {
        this.doc = doc;
    }


    @Override
    public Xml child(final String element) {
        return this.children()
            .filter(e -> e.name().equals(element))
            .findFirst()
            .orElseThrow();
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<String> text() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Stream<Xml> children() {
        return this.doc.children(0);
    }

    @Override
    public String name() {
        return this.doc.content(1);
    }

    @Override
    public Xml copy() {
        return new FlatXml(this.doc);
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String toString() {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>%s",
            this.doc.child(1)
        );
    }
}
