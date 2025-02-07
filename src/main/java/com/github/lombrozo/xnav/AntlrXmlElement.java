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
 * element
 *     : '<' Name attribute* '>' content '<' '/' Name '>'
 *     | '<' Name attribute* '/>'
 *     ;
 */
@EqualsAndHashCode
public final class AntlrXmlElement implements Xml {

    @EqualsAndHashCode.Exclude
    XMLParser.ElementContext context;

    public AntlrXmlElement(XMLParser.ElementContext context) {
        this.context = context;
    }

    @Override
    public Xml child(final String element) {
        final XMLParser.ContentContext content = this.context.content();
        for (final XMLParser.ElementContext context : content.element()) {
            final AntlrXmlElement elem = new AntlrXmlElement(context);
            if (elem.name().equals(element)) {
                return elem;
            }
        }
        return new Empty();
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return Optional.empty();
    }

    @Override
    public Stream<Xml> children() {
        return new AntlrXmlContent(this.context.content()).children();
    }

    @Override
    public Optional<String> text() {
        return new AntlrXmlContent(this.context.content()).text();
    }

    @Override
    public String name() {
        return this.context.Name(0).getText();
    }

    @Override
    public Xml copy() {
        return new AntlrXmlElement(this.context);
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    @EqualsAndHashCode.Include
    public String toString() {
        return this.context.getText();
    }
}
