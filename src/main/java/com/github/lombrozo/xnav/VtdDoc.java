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

import com.ximpleware.VTDNav;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

/**
 * VTD document.
 * Represents an XML document.
 * This class is thread-safe.
 * @since 0.1
 */
@EqualsAndHashCode
final class VtdDoc implements Xml {

    /**
     * Root element.
     */
    @EqualsAndHashCode.Exclude
    private final Xml root;

    /**
     * Constructor.
     * @param nav VTD navigator.
     */
    VtdDoc(final VTDNav nav) {
        this(new VtdElem(nav));
    }

    /**
     * Constructor.
     * @param root Root element.
     */
    private VtdDoc(final Xml root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s",
            this.root
        );
    }

    @Override
    public Xml child(final String element) {
        return this.children()
            .filter(e -> e.name().equals(element))
            .findFirst()
            .orElseThrow();
    }

    @Override
    public Stream<Xml> children() {
        return Stream.of(this.root);
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return this.root.attribute(name);
    }

    @Override
    public Optional<String> text() {
        return Optional.of(
            this.children()
                .map(Xml::text)
                .flatMap(Optional::stream)
                .collect(Collectors.joining())
        );
    }

    @EqualsAndHashCode.Include
    @Override
    public String name() {
        return this.root.name();
    }

    @Override
    public Xml copy() {
        return new VtdDoc(this.root.copy());
    }

    @Override
    public Node node() {
        return new StringNode(this.toString()).toNode();
    }
}
