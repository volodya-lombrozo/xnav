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
import lombok.ToString;
import org.w3c.dom.Node;

@EqualsAndHashCode
@ToString
public final class EagerDoc implements Xml {

    private final Xml element;


    public EagerDoc(final Xml element) {
        this.element = element;
    }

    @Override
    public Xml child(final String element) {
        if (this.element.name().equals(element)) {
            return this.element;
        } else {
            return new Empty();
        }
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return this.element.attribute(name);
    }

    @Override
    public Optional<String> text() {
        return this.element.text();
    }

    @Override
    public Stream<Xml> children() {
        return this.element.children();
    }

    @Override
    public String name() {
        return this.element.name();
    }

    @Override
    public Xml copy() {
        return new EagerDoc(this.element.copy());
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
