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
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.w3c.dom.Node;

/**
 * Object representation of XML.
 * Inefficient implementation, but simple and thread-safe.
 * @since 0.1
 */
@EqualsAndHashCode
public final class ObjectXml implements Xml {

    /**
     * XML document.
     */
    private final Xml doc;

    /**
     * Constructor.
     * @param xml XML string.
     */
    ObjectXml(final String... xml) {
        this(String.join("", xml));
    }

    /**
     * Constructor.
     * @param xml XML string.
     */
    private ObjectXml(final String xml) {
        this(ObjectXml.parse(xml));
    }

    /**
     * Constructor.
     * @param doc XML document.
     */
    private ObjectXml(final Xml doc) {
        this.doc = doc;
    }

    @Override
    public Xml child(final String element) {
        return this.doc.child(element);
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return this.doc.attribute(name);
    }

    @Override
    public Optional<String> text() {
        return this.doc.text();
    }

    @Override
    public Stream<Xml> children() {
        return this.doc.children();
    }

    @Override
    public String name() {
        return this.doc.name();
    }

    @Override
    public Xml copy() {
        return this.doc.copy();
    }

    @Override
    public Node node() {
        return this.doc.node();
    }

    @Override
    public String toString() {
        return this.doc.toString();
    }

    /**
     * Parse XML string.
     * We use ANTLR to parse XML.
     * @param xml XML string.
     * @return XML document.
     */
    private static Xml parse(final String xml) {
        try {
            final XMLParser parser = new XMLParser(
                new CommonTokenStream(new XMLLexer(CharStreams.fromString(xml)))
            );
            parser.setErrorHandler(new BailErrorStrategy());
            return new ObjectXmlVisitor().visitDocument(parser.document());
        } catch (final ParseCancellationException exception) {
            throw new IllegalArgumentException(
                String.format("Invalid XML: %s", xml.substring(0, Math.min(100, xml.length()))),
                exception
            );
        }
    }
}
