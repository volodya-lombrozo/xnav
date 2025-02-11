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

import java.util.Objects;
import org.antlr.v4.runtime.ParserRuleContext;

public final class CounterVisitor extends XMLParserBaseVisitor<Integer> {

    @Override
    public Integer visitDocument(final XMLParser.DocumentContext ctx) {
        return 1 + this.visitChildren(ctx);
    }

    @Override
    public Integer visitElement(final XMLParser.ElementContext ctx) {
        return 1 + this.visitChildren(ctx);
    }

    @Override
    public Integer visitAttribute(final XMLParser.AttributeContext ctx) {
        return 1;
    }

    @Override
    public Integer visitContent(final XMLParser.ContentContext ctx) {
        return 1 + this.visitChildren(ctx);
    }

    @Override
    public Integer visitChardata(final XMLParser.ChardataContext ctx) {
        return 1;
    }

    private int visitChildren(ParserRuleContext ctx) {
        if (ctx == null) {
            return 0;
        }
        if (ctx.children == null) {
            return 0;
        }
        return ctx.children.stream()
            .map(this::visit)
            .filter(Objects::nonNull)
            .reduce(0, Integer::sum);
    }
}
