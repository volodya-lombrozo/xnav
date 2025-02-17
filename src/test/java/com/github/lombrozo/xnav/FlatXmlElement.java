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
import org.w3c.dom.Node;

/**
 * Flat representation of XML element.
 * @since 0.1
 */
@EqualsAndHashCode
public final class FlatXmlElement implements Xml {

    /**
     * Element id.
     */
    @EqualsAndHashCode.Exclude
    private final int id;

    /**
     * Model of flat xml.
     */
    @EqualsAndHashCode.Exclude
    private final FlatXmlModel xml;

    /**
     * Constructor.
     * @param identifier Element id.
     * @param model Flat xml model.
     */
    FlatXmlElement(final int identifier, final FlatXmlModel model) {
        this.id = identifier;
        this.xml = model;
    }

    @Override
    public Xml child(final String element) {
        return this.children()
            .filter(e -> e.name().equals(element))
            .findFirst()
            .orElse(new Empty());
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return this.attributes()
            .filter(e -> e.name().equals(name))
            .findFirst();
    }

    @EqualsAndHashCode.Include
    @Override
    public Optional<String> text() {
        return Optional.of(
            this.children()
                .map(Xml::text)
                .flatMap(Optional::stream)
                .collect(Collectors.joining())
        );
    }

    @Override
    public Stream<Xml> children() {
        return this.xml.children(this.id)
            .filter(e -> !(e instanceof FlatXmlAttribute))
            .flatMap(Xml::children);
    }

    @EqualsAndHashCode.Include
    @Override
    public String name() {
        return this.xml.content(this.id);
    }

    @Override
    public Xml copy() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String toString() {
        String atts = this.attributes()
            .map(Xml::toString)
            .collect(Collectors.joining(" "));
        if (!atts.isEmpty()) {
            atts = String.format(" %s", atts);
        }
        return String.format(
            "<%s%s>%s</%s>",
            this.name(),
            atts,
            this.text().orElse(""),
            this.name()
        );
    }

    /**
     * Attributes.
     * @return Stream of attributes.
     */
    private Stream<Xml> attributes() {
        return this.xml.children(this.id)
            .filter(FlatXmlAttribute.class::isInstance);
    }
}
