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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Node;

@EqualsAndHashCode
@ToString
public final class OptCont implements Xml {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final int id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final OptimizedXml xml;

    public OptCont(final int id, final OptimizedXml xml) {
        this.id = id;
        this.xml = xml;
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
    public Stream<Xml> children() {
        return this.xml.children(this.id);
    }

    @ToString.Include
    @EqualsAndHashCode.Include
    @Override
    public Optional<String> text() {
        final Stream<Xml> children = this.xml.children(this.id);
        return Optional.of(
            children
                .map(Xml::text)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining())
        );
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public Xml copy() {
        return null;
    }

    @Override
    public Node node() {
        return null;
    }
}
