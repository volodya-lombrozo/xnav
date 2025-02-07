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

final class AntlrElementVisitor extends XMLParserBaseVisitor<Xml> {

    @Override
    public Xml visitDocument(final XMLParser.DocumentContext ctx) {
        return this.visit(ctx.element());
    }

    @Override
    public Xml visitElement(final XMLParser.ElementContext ctx) {

        final List<Xml> children = new ArrayList<>(0);
        final XMLParser.ContentContext content = ctx.content();
        if (content == null) {
            throw new IllegalArgumentException("Can't parse XML element without content");
        }
        for (final XMLParser.ElementContext context : content.element()) {
            children.add(this.visit(context));
        }
        return new AntlrXmlElement(ctx);
    }


}
