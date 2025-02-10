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

public final class EagerVisitor extends XMLParserBaseVisitor<Xml> {

    @Override
    public Xml visitDocument(final XMLParser.DocumentContext ctx) {
        return new EagerDoc(this.visitElement(ctx.element()));
    }

    @Override
    public Xml visitElement(final XMLParser.ElementContext ctx) {
        final TerminalNode name = ctx.Name(0);
        final List<Xml> attrs = ctx.attribute()
            .stream()
            .map(this::visitAttribute)
            .collect(Collectors.toList());
        final Xml content = this.visitContent(ctx.content());
        return new EagerElem(name.getText(), attrs, content);
    }

    @Override
    public Xml visitAttribute(final XMLParser.AttributeContext ctx) {
        final TerminalNode name = ctx.Name();
        final TerminalNode value = ctx.STRING();
        return new EagerAttr(name.getText(), value.getText());
    }

    @Override
    public Xml visitContent(final XMLParser.ContentContext ctx) {
        final List<Xml> all = new ArrayList<>(0);
        final List<ParseTree> children = Optional.ofNullable(ctx)
            .map(c -> c.children)
            .orElse(new ArrayList<>(0));
        for (final ParseTree child : children) {
            if (child instanceof XMLParser.ElementContext) {
                all.add(this.visitElement((XMLParser.ElementContext) child));
            } else if (child instanceof XMLParser.ChardataContext) {
                all.add(this.visitChardata((XMLParser.ChardataContext) child));
            }
        }
        return new EagerCont(all);
    }

    @Override
    public Xml visitChardata(final XMLParser.ChardataContext ctx) {
        return new EagerChard(ctx.getText());
    }
}
