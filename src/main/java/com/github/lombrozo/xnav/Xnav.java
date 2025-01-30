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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Node;

/**
 * XML navigator.
 * Allows navigating through an XML document.
 * This class is thread-safe.
 *
 * @since 0.1
 */
@ToString
@EqualsAndHashCode
public final class Xnav {

    /**
     * Actual XML document node.
     */
    private final Xml xml;

    /**
     * Constructor.
     *
     * @param file XML document file.
     */
    public Xnav(final Path file) {
        this(Xnav.from(file));
    }

    /**
     * Constructor.
     *
     * @param file XML document file.
     */
    public Xnav(final File file) {
        this(Xnav.from(file.toPath()));
    }

    /**
     * Ctor.
     *
     * @param join XML document as a string.
     */
    public Xnav(final String join) {
        this(new Xml(join));
    }

    /**
     * Ctor.
     *
     * @param node XML document node.
     */
    public Xnav(final Node node) {
        this(new Xml(node));
    }

    /**
     * Ctor.
     *
     * @param xml XML document node.
     */
    public Xnav(final Xml xml) {
        this.xml = xml;
    }

    /**
     * Get a child node by its name.
     *
     * @param name Element name.
     * @return Navigator for the child.
     */
    public Xnav element(final String name) {
        return new Xnav(this.xml.child(name));
    }

    /**
     * Get all child nodes by their name.
     *
     * @param filters Filters to apply.
     * @return Stream of navigators for the children.
     */
    public Stream<Xnav> elements(final Filter... filters) {
        return this.xml.children().filter(Filter.all(filters)).map(Xnav::new);
    }

    /**
     * Get an attribute by its name.
     *
     * @param name Attribute name.
     * @return Navigator for the attribute.
     */
    public Xnav attribute(final String name) {
        return new Xnav(
            this.xml.attribute(name).orElseThrow(
                () -> new IllegalStateException(
                    String.format("Attribute '%s' not found in '%s'", name, this)
                )
            )
        );
    }

    /**
     * Get nodes by XPath expression.
     *
     * @param path XPath expression.
     * @return Stream of navigators for the nodes.
     */
    public Stream<Xnav> path(final String path) {
        return new Xpath(this.xml, path).nodes().map(Xnav::new);
    }

    /**
     * Make a deep copy of the navigator.
     *
     * @return Deep copy of the navigator.
     */
    public Xnav copy() {
        return new Xnav(this.xml.copy());
    }

    /**
     * Get the text of the current node.
     *
     * @return Text of the node.
     */
    public Optional<String> text() {
        return this.xml.text();
    }

    /**
     * Get current node.
     *
     * @return Current node.
     */
    public Node node() {
        return this.xml.node();
    }

    /**
     * Get the XML document from the file.
     *
     * @param file XML file.
     * @return XML document.
     */
    private static Xml from(final Path file) {
        try {
            return new Xml(Files.readString(file));
        } catch (final IOException exception) {
            throw new IllegalStateException(
                String.format("Failed to read file '%s'", file),
                exception
            );
        }
    }
}
