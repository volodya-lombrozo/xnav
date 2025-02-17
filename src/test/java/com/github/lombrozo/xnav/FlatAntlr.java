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

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * Flat model parsed by Antlr from XML.
 *
 * @since 0.1
 */
public final class FlatAntlr {

    /**
     * XML string.
     */
    private final String xml;

    /**
     * Constructor.
     * @param xml XML string.
     */
    public FlatAntlr(final String xml) {
        this.xml = xml;
    }

    /**
     * Parse XML to a flat model.
     * @return Flat XML model.
     */
    public FlatXmlModel parse() {
        try {
            final FlatAntlrVisitor visitor = new FlatAntlrVisitor();
            final XMLParser parser = new XMLParser(
                new CommonTokenStream(new XMLLexer(CharStreams.fromString(this.xml)))
            );
            parser.setErrorHandler(new BailErrorStrategy());
            return visitor.visitDocument(parser.document());
        } catch (final ParseCancellationException e) {
            throw new IllegalArgumentException("Invalid XML", e);
        }
    }
}
