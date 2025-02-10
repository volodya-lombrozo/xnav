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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.antlr.v4.runtime.misc.Interval;
import org.w3c.dom.Node;

@EqualsAndHashCode
@ToString
public final class EagerElem implements Xml {

    private final String name;
    private final List<Xml> attributes;
    private final Xml content;

    public EagerElem(final String name, final List<Xml> attrs, final Xml content) {
        this.name = name;
        this.attributes = attrs;
        this.content = content;
    }

    @Override
    public Xml child(final String element) {
        return this.content.child(element);
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return this.attributes.stream()
            .filter(attr -> attr.name().equals(name))
            .findFirst();
    }

    @Override
    public Optional<String> text() {
        return this.content.text();
    }

    @Override
    public Stream<Xml> children() {
        return this.content.children();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Xml copy() {
        return new EagerElem(
            this.name,
            this.attributes.stream().map(Xml::copy).collect(Collectors.toList()),
            this.content.copy()
        );
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
