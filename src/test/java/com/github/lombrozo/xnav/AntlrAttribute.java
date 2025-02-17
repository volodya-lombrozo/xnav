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

@EqualsAndHashCode
final class AntlrAttribute implements Xml {

    @EqualsAndHashCode.Exclude
    private final XMLParser.AttributeContext context;

    public AntlrAttribute(final XMLParser.AttributeContext context) {
        this.context = context;
    }

    @Override
    public Xml child(final String element) {
        return null;
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return Optional.empty();
    }

    @Override
    public Optional<String> text() {
        final String text = this.context.STRING().getText();
        return Optional.of(text.substring(1, text.length() - 1));
    }

    @Override
    public Stream<Xml> children() {
        return null;
    }

    @Override
    public String name() {
        return this.context.Name().getText();
    }

    @Override
    public Xml copy() {
        return null;
    }

    @Override
    public Node node() {
        return null;
    }

    @EqualsAndHashCode.Include
    @Override
    public String toString() {
        return this.context.getText();
    }
}
