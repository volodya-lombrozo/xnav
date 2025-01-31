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
import org.w3c.dom.Node;

/**
 * XML abstraction over an XML document.
 * All implementations of this interface are thread-safe.
 *
 * @since 0.1
 */
public interface Xml {

    /**
     * Get a child node by its name.
     *
     * @param element Element name.
     * @return Child.
     */
    Xml child(String element);

    /**
     * Get an attribute by its name.
     *
     * @param name Attribute name.
     * @return Attribute.
     */
    Optional<Xml> attribute(String name);

    /**
     * Get the text of the current node.
     *
     * @return Text of the node.
     */
    Optional<String> text();

    /**
     * Get children of the current node.
     *
     * @return Children.
     */
    Stream<Xml> children();

    /**
     * Get the name of the node.
     *
     * @return Node name.
     */
    String name();

    /**
     * Copy the XML document.
     *
     * @return Copy of the document.
     */
    Xml copy();

    /**
     * Get the actual node.
     *
     * @return Node.
     */
    Node node();
}
