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
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.w3c.dom.Node;

@EqualsAndHashCode
final class AntlrXmlDocument implements Xml {

    @EqualsAndHashCode.Exclude
    private ThreadLocal<XMLParser> parser;

    private final String xml;

    public AntlrXmlDocument(final String... xml) {
        this(String.join("", xml));
    }

    AntlrXmlDocument(final String xml) {
        this(AntlrXmlDocument.parser(xml), xml);
    }


    private AntlrXmlDocument(final ThreadLocal<XMLParser> parser, final String xml) {
        this.parser = parser;
        this.xml = xml;
    }

    @Override
    public Xml child(final String element) {
//        AntlrElementVisitor visitor = new AntlrElementVisitor();
//        return visitor.visitElement(this.parser.get().document().element());

        return new AntlrXmlElement(this.parser.get().document().element());
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<String> text() {
        return Optional.of(this.children().map(Xml::text)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.joining()));
    }

    @Override
    public Stream<Xml> children() {
        AntlrElementVisitor visitor = new AntlrElementVisitor();
//        final XMLParser.DocumentContext document = this.parser.get().document();
//        final Xml t = visitor.visitElement(document.element());
        final XMLParser.DocumentContext document = this.parser.get().document();
        System.out.println("DOC: " + document.getText());
        final XMLParser.ElementContext context = document.element();
        final Xml t = new AntlrXmlElement(context);

        return Stream.of(t);
    }

    @Override
    public String name() {
        return new AntlrElementVisitor().visitElement(this.parser.get().document().element())
            .name();
    }

    @Override
    public Xml copy() {
        return new AntlrXmlDocument(this.parser, this.xml);
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String toString() {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>%s", this.xml
        );
    }

    private static ThreadLocal<XMLParser> parser(final String xml) {
        return ThreadLocal.withInitial(
            () -> {
                final XMLParser p = new XMLParser(
                    new CommonTokenStream(new XMLLexer(CharStreams.fromString(xml)))
                );
//                p.setErrorHandler(new BailErrorStrategy());
                return p;
            }
        );

    }
}
