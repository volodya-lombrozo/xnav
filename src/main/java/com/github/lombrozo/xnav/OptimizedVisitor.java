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
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

public final class OptimizedVisitor extends XMLParserBaseVisitor<OptimizedXml> {

    private final AtomicInteger index = new AtomicInteger(-1);
    private final Deque<Integer> stack = new ArrayDeque<>(0);
    private OptimizedXml xml;

    public OptimizedVisitor() {
        this(new OptimizedXml());
    }

    public OptimizedVisitor(final OptimizedXml xml) {
        this.xml = xml;
    }

    @Override
    public OptimizedXml visitDocument(final XMLParser.DocumentContext ctx) {
        this.xml.addElement(
            this.index.get(),
            this.index.incrementAndGet(),
            OptimizedXml.Type.DOCUMENT,
            ""
        );
        this.stack.push(this.index.get());
        ctx.children.forEach(super::visit);
        this.stack.pop();
        return this.xml;
    }

    @Override
    public OptimizedXml visitElement(final XMLParser.ElementContext ctx) {
        this.xml.addElement(
            this.stack.peek(),
            this.index.incrementAndGet(),
            OptimizedXml.Type.ELEMENT,
            ctx.Name(0).getText()
        );
        this.stack.push(this.index.get());
        ctx.children.forEach(super::visit);
        this.stack.pop();
        return this.xml;
    }

    @Override
    public OptimizedXml visitAttribute(final XMLParser.AttributeContext ctx) {
        this.xml.addElement(
            this.stack.peek(),
            this.index.incrementAndGet(),
            OptimizedXml.Type.ATTRIBUTE,
            ctx.getText()
        );
        this.stack.push(this.index.get());
        ctx.children.forEach(super::visit);
        this.stack.pop();
        return this.xml;
    }

    @Override
    public OptimizedXml visitContent(final XMLParser.ContentContext ctx) {
        this.xml.addContent(
            this.stack.peek(),
            this.index.incrementAndGet(),
            OptimizedXml.Type.CONTENT
        );
        this.stack.push(this.index.get());
        ctx.children.forEach(super::visit);
        this.stack.pop();
        return this.xml;
    }

    @Override
    public OptimizedXml visitChardata(final XMLParser.ChardataContext ctx) {
        final String text = ctx.getText();
        final int current = this.index.incrementAndGet();
        this.xml.addElement(
            this.stack.peek(),
            current,
            OptimizedXml.Type.CHARDATA,
            text
        );
        return this.xml;
    }
}
