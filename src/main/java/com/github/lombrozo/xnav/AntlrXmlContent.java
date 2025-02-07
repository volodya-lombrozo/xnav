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
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Node;

/**
 * content
 *     : chardata? ((element | reference | CDATA | PI | COMMENT) chardata?)*
 *     ;
 */
public final class AntlrXmlContent implements Xml {

    private final XMLParser.ContentContext content;

    public AntlrXmlContent(final XMLParser.ContentContext content) {
        this.content = content;
    }

    @Override
    public Xml child(final String element) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        throw new UnsupportedOperationException("Not implemented");

    }

    @Override
    public Optional<String> text() {
        final List<ParseTree> children = this.content.children;
        final String collect = children.stream().<Optional<String>>map(
            child -> {
                if (child instanceof XMLParser.ChardataContext) {
                    return new AntlrChardata((XMLParser.ChardataContext) child).text();
                }
                return Optional.empty();
            }
        ).filter(Optional::isPresent).map(Optional::get).collect(Collectors.joining());
        return Optional.of(collect);
    }

    @Override
    public Stream<Xml> children() {
        final XMLParser.ContentContext ctx = this.content;
        return ctx.children.stream()
            .map(
            child -> {
                if (child instanceof XMLParser.ElementContext) {
                    return new AntlrXmlElement((XMLParser.ElementContext) child);
                }
                if (child instanceof XMLParser.ChardataContext) {
                    return new AntlrChardata((XMLParser.ChardataContext) child);
                }
                throw new UnsupportedOperationException("Not implemented");
            }
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
