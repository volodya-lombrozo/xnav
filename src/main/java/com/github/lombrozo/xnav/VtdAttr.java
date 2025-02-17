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

import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Node;

/**
 * VTD attribute.
 * Represents an XML attribute.
 * This class is thread-safe.
 * @since 0.1
 */
@EqualsAndHashCode
final class VtdAttr implements Xml {

    /**
     * Attribute name.
     */
    private final String attr;

    /**
     * VTD navigator.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final VTDNav navigator;

    /**
     * Constructor.
     * @param name Attribute name.
     * @param nav VTD navigator.
     */
    VtdAttr(final String name, final VTDNav nav) {
        this.attr = name;
        this.navigator = nav.cloneNav();
    }

    @Override
    public Xml child(final String element) {
        throw new UnsupportedOperationException(
            String.format("Attributes can't have a child with name %s", element)
        );
    }

    @Override
    public Stream<Xml> children() {
        return Stream.empty();
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return Optional.empty();
    }

    @ToString.Include
    @EqualsAndHashCode.Include
    @Override
    public Optional<String> text() {
        try {
            final VTDNav nav = this.start();
            return Optional.ofNullable(nav.toString(nav.getAttrVal(this.attr)));
        } catch (final NavException exception) {
            throw new IllegalStateException("Error getting attribute text", exception);
        }
    }

    @Override
    public String name() {
        return this.attr;
    }

    @Override
    public Xml copy() {
        return new VtdAttr(this.attr, this.navigator);
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Attributes can't be converted to DOM nodes");
    }

    @Override
    public String toString() {
        return this.attr + "='" + this.text().orElse("") + "'";
    }

    /**
     * Start navigation.
     * @return VTD navigator.
     */
    private VTDNav start() {
        return this.navigator.cloneNav();
    }
}
