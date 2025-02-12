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
import javax.xml.parsers.DocumentBuilderFactory;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.w3c.dom.Node;

public final class OptXml implements Xml {

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


    private final OptimizedXml doc;

    public OptXml(final String... xml) {
        this(String.join("", xml));
    }

    private OptXml(final String xml) {
        this(new StringNode(xml).toNode());
    }

    private OptXml(final Node node) {
        this.doc = OptXml.parseDom(node);
    }

    private static OptimizedXml parseAntlr(final String xml) {
        try {
            final OptimizedVisitor visitor = new OptimizedVisitor();
            final XMLParser p = new XMLParser(
                new CommonTokenStream(new XMLLexer(CharStreams.fromString(xml)))
            );
            p.setErrorHandler(new BailErrorStrategy());
            return visitor.visitDocument(p.document());
        } catch (ParseCancellationException e) {
            throw new IllegalArgumentException("Invalid XML", e);
        }
    }

    private static OptimizedXml parseDom(final Node node) {
        return new OptimizedDom(node).parse();
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
        //wtf? Why no 0?
        return this.doc.content(1);
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
