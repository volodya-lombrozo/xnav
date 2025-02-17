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
import java.util.Optional;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Antlr visitor for XML parsing.
 * @since 0.1
 */
final class ObjectXmlVisitor extends XMLParserBaseVisitor<Xml> {

    @Override
    public Xml visitDocument(final XMLParser.DocumentContext ctx) {
        return new ObjectXmlDocument(this.visitElement(ctx.element()));
    }

    @Override
    public Xml visitElement(final XMLParser.ElementContext ctx) {
        return new ObjectXmlElement(
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
        return new ObjectXmlAttribute(ctx.Name().getText(), ctx.STRING().getText());
    }

    @Override
    public Xml visitContent(final XMLParser.ContentContext ctx) {
        return new ObjectXmlContent(
            Optional.ofNullable(ctx)
                .map(content -> content.children)
                .orElse(new ArrayList<>(0))
                .stream()
                .filter(ObjectXmlVisitor::isChild)
                .map(this::toChild)
                .collect(Collectors.toList())
        );
    }

    @Override
    public Xml visitChardata(final XMLParser.ChardataContext ctx) {
        return new ObjectXmlChardata(ctx.getText());
    }

    /**
     * Convert child to XML.
     * @param child Child to convert
     * @return XML
     */
    private Xml toChild(final ParseTree child) {
        if (ObjectXmlVisitor.isElement(child)) {
            return this.visitElement((XMLParser.ElementContext) child);
        } else if (child instanceof XMLParser.ChardataContext) {
            return this.visitChardata((XMLParser.ChardataContext) child);
        }
        throw new IllegalStateException(
            String.format("Unexpected child type: %s", child.getClass())
        );
    }

    /**
     * Check if the ANTLR context is a content child.
     * @param child Context to check.
     * @return True if the context is a content child.
     */
    private static boolean isChild(final ParseTree child) {
        return ObjectXmlVisitor.isChardata(child) || ObjectXmlVisitor.isElement(child);
    }

    /**
     * Check if the child is chardata.
     * @param child Child to check
     * @return True if the child is chardata
     */
    private static boolean isChardata(final ParseTree child) {
        return child instanceof XMLParser.ChardataContext;
    }

    /**
     * Check if the child is an element.
     * @param child Child to check
     * @return True if the child is an element
     */
    private static boolean isElement(final ParseTree child) {
        return child instanceof XMLParser.ElementContext;
    }
}
