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
import lombok.ToString;
import org.w3c.dom.Node;

/**
 * XML navigator.
 * Allows navigating through an XML document.
 * @since 0.1
 */
@ToString
public final class Navigator {

    /**
     * Actual XML document node.
     */
    private final Xml node;

    /**
     * Ctor.
     * @param join XML document as a string.
     */
    public Navigator(final String join) {
        this(new Xml(join));
    }

    /**
     * Ctor.
     * @param node XML document node.
     */
    public Navigator(final Node node) {
        this(new Xml(node));
    }

    /**
     * Ctor.
     * @param xml XML document node.
     */
    public Navigator(final Xml xml) {
        this.node = xml;
    }

    /**
     * Get a child node by its name.
     * @param name Element name.
     * @return Navigator for the child.
     */
    public Navigator element(final String name) {
        return new Navigator(this.node.child(name));
    }

    /**
     * Get all child nodes by their name.
     * @param filters Filters to apply.
     * @return Stream of navigators for the children.
     */
    public Stream<Navigator> elements(final Filter... filters) {
        return this.node.children().filter(Filter.all(filters)).map(Navigator::new);
    }

    /**
     * Get an attribute by its name.
     * @param name Attribute name.
     * @return Navigator for the attribute.
     */
    public Navigator attribute(final String name) {
        return new Navigator(this.node.attribute(name));
    }

    /**
     * Copy the navigator.
     * @return Copy of the navigator.
     */
    public Navigator copy() {
        return new Navigator(this.node.copy());
    }

    /**
     * Get the text of the current node.
     * @return Text of the node.
     */
    public Optional<String> text() {
        return this.node.text();
    }

    /**
     * Get current node.
     * @return Current node.
     */
    public Node node() {
        return this.node.node();
    }
}
