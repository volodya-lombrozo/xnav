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

@ToString
@EqualsAndHashCode
public final class VtdAttr implements Xml {

    /**
     * Attribute name.
     */
    private final String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final VTDNav navigator;

    /**
     * Constructor.
     * @param name Attribute name.
     * @param nav VTD navigator.
     */
    VtdAttr(final String name, final VTDNav nav) {
        this.name = name;
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
        throw new UnsupportedOperationException("Attributes can't have children");
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        throw new UnsupportedOperationException("Attributes do not have attributes");
    }

    @ToString.Include
    @EqualsAndHashCode.Include
    @Override
    public Optional<String> text() {
        try {
            final VTDNav nav = this.start();
            return Optional.ofNullable(nav.toString(nav.getAttrVal(this.name)));
        } catch (final NavException exception) {
            throw new IllegalStateException("Error getting attribute text", exception);
        }
    }

    @Override
    public String name() {
        try {
            final VTDNav nav = this.start();
            return nav.toString(nav.getCurrentIndex());
        } catch (final NavException exception) {
            throw new IllegalStateException("Error getting attribute name", exception);
        }
    }

    @Override
    public Xml copy() {
        return new VtdAttr(this.name, this.navigator);
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Start navigation.
     * @return VTD navigator.
     */
    private VTDNav start() {
        return this.navigator.cloneNav();
    }
}
