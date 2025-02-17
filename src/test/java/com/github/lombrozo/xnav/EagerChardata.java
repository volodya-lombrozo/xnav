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

/**
 * Chardata.
 * @since 0.1
 */
@EqualsAndHashCode
final class EagerChardata implements Xml {

    /**
     * Chardata.
     */
    private final String text;

    /**
     * Constructor.
     * @param chardata Text.
     */
    EagerChardata(final String chardata) {
        this.text = chardata;
    }

    @Override
    public Xml child(final String element) {
        throw new UnsupportedOperationException("Text node has no children.");
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return Optional.empty();
    }

    @Override
    public Optional<String> text() {
        return Optional.of(this.text);
    }

    @Override
    public Stream<Xml> children() {
        return Stream.empty();
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public Xml copy() {
        return new EagerChardata(this.text);
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Text node can't be converted to a DOM node.");
    }

    @Override
    public String toString() {
        return this.text;
    }
}
