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
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.w3c.dom.Node;

public final class LazyXml implements Xml {

    private final OptimizedXml doc;

    public LazyXml(final String... xml) {
        this(String.join("", xml));
    }

    private LazyXml(final String xml) {
        this.doc = LazyXml.parse(xml);
    }

    private static OptimizedXml parse(final String xml) {
        final OptimizedVisitor visitor = new OptimizedVisitor();
        final XMLParser p = new XMLParser(
            new CommonTokenStream(new XMLLexer(CharStreams.fromString(xml)))
        );
        p.setErrorHandler(new BailErrorStrategy());
        final OptimizedXml res = visitor.visitDocument(p.document());
        return res;
    }


    @Override
    public Xml child(final String element) {
        final Xml xml = this.children()
            .filter(e -> e.name().equals(element))
            .findFirst()
            .orElseThrow();
        return xml;
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
        final Stream<Xml> children = this.doc.children(0);
        return children;
    }

    @Override
    public String name() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Xml copy() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
