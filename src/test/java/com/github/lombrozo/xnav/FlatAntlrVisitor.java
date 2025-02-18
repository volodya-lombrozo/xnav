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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Antlr visitor to build a flat XML model.
 * @since 0.1
 */
final class FlatAntlrVisitor extends XMLParserBaseVisitor<FlatXmlModel> {

    /**
     * Index of the current element.
     */
    private final AtomicInteger index;

    /**
     * Stack of the current element.
     */
    private final Deque<Integer> stack;

    /**
     * Flat XML model.
     */
    private FlatXmlModel xml;

    /**
     * Constructor.
     */
    FlatAntlrVisitor() {
        this(new FlatXmlModel());
    }

    /**
     * Constructor.
     * @param xml Flat XML model to fill.
     */
    private FlatAntlrVisitor(final FlatXmlModel xml) {
        this.index = new AtomicInteger(-1);
        this.stack = new ArrayDeque<>(0);
        this.xml = xml;
    }

    @Override
    public FlatXmlModel visitDocument(final XMLParser.DocumentContext ctx) {
        this.xml.add(
            this.index.get(),
            this.index.incrementAndGet(),
            FlatXmlModel.Type.DOCUMENT,
            ""
        );
        this.stack.push(this.index.get());
        ctx.children.forEach(super::visit);
        this.stack.pop();
        return this.xml;
    }

    @Override
    public FlatXmlModel visitElement(final XMLParser.ElementContext ctx) {
        this.xml.add(
            this.stack.peek(),
            this.index.incrementAndGet(),
            FlatXmlModel.Type.ELEMENT,
            ctx.Name(0).getText()
        );
        this.stack.push(this.index.get());
        ctx.children.forEach(super::visit);
        this.stack.pop();
        return this.xml;
    }

    @Override
    public FlatXmlModel visitAttribute(final XMLParser.AttributeContext ctx) {
        this.xml.add(
            this.stack.peek(),
            this.index.incrementAndGet(),
            FlatXmlModel.Type.ATTRIBUTE,
            ctx.getText()
        );
        return this.xml;
    }

    @Override
    public FlatXmlModel visitContent(final XMLParser.ContentContext ctx) {
        this.xml.add(
            this.stack.peek(),
            this.index.incrementAndGet(),
            FlatXmlModel.Type.CONTENT,
            null
        );
        this.stack.push(this.index.get());
        Optional.ofNullable(ctx)
            .map(c -> c.children)
            .stream()
            .flatMap(Collection::stream)
            .forEach(super::visit);
        this.stack.pop();
        return this.xml;
    }

    @Override
    public FlatXmlModel visitChardata(final XMLParser.ChardataContext ctx) {
        final String text = ctx.getText();
        final int current = this.index.incrementAndGet();
        this.xml.add(
            this.stack.peek(),
            current,
            FlatXmlModel.Type.CHARDATA,
            text
        );
        return this.xml;
    }
}
