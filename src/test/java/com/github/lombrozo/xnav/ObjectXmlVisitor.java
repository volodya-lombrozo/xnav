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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Antlr visitor for XML parsing.
 * @since 0.1
 */
final class ObjectXmlVisitor extends XMLParserBaseVisitor<Xml> {

    @Override
    public Xml visitDocument(final XMLParser.DocumentContext ctx) {
        final XMLParser.ElementContext context = ctx.element();
        return new ObjectDocument(this.visitElement(context));
    }

    @Override
    public Xml visitElement(final XMLParser.ElementContext ctx) {
        return new ObjectElement(
            ctx.Name(0).getText(),
            ctx.attribute()
                .stream()
                .map(this::visitAttribute)
                .collect(Collectors.toList()),
            this.visitContent(ctx.content())
        );
    }

    @Override
    public Xml visitAttribute(final XMLParser.AttributeContext ctx) {
        final TerminalNode name = ctx.Name();
        final TerminalNode value = ctx.STRING();
        return new ObjectAttribute(name.getText(), value.getText());
    }

    @Override
    public Xml visitContent(final XMLParser.ContentContext ctx) {
        final List<Xml> res = new ArrayList<>(0);
        final List<ParseTree> children = Optional.ofNullable(ctx)
            .map(c -> c.children)
            .orElse(new ArrayList<>(0));
        for (final ParseTree child : children) {
            if (child instanceof XMLParser.ElementContext) {
                res.add(this.visitElement((XMLParser.ElementContext) child));
            } else if (child instanceof XMLParser.ChardataContext) {
                res.add(this.visitChardata((XMLParser.ChardataContext) child));
            }
        }
        return new ObjectContent(res);
    }

    @Override
    public Xml visitChardata(final XMLParser.ChardataContext ctx) {
        return new ObjectChardata(ctx.getText());
    }
}
