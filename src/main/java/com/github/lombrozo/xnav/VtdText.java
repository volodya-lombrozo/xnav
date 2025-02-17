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
import org.w3c.dom.Node;

/**
 * VTD text.
 * Represents an XML text node.
 * This class is thread-safe.
 * @since 0.1
 */
@EqualsAndHashCode
final class VtdText implements OrderedXml {

    /**
     * VTD navigator.
     */
    @EqualsAndHashCode.Exclude
    private final VTDNav navigator;

    /**
     * Order number.
     */
    @EqualsAndHashCode.Exclude
    private final int index;

    /**
     * Constructor.
     * @param navigator VTD navigator.
     * @param index Order number.
     */
    VtdText(final VTDNav navigator, final int index) {
        this.navigator = navigator;
        this.index = index;
    }

    @Override
    public String toString() {
        return this.text().orElse("");
    }

    @Override
    public Xml child(final String element) {
        throw new UnsupportedOperationException("Text nodes do not have children");
    }

    @Override
    public Stream<Xml> children() {
        return Stream.empty();
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        throw new UnsupportedOperationException("Text nodes do not have attributes");
    }

    @Override
    public Optional<String> text() {
        try {
            return Optional.of(this.navigator.toString(this.index));
        } catch (final NavException exception) {
            throw new IllegalStateException(
                String.format("Can't retrive text from '%s'", this),
                exception
            );
        }
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public Xml copy() {
        return new VtdText(this.navigator.cloneNav(), this.index);
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Text can't be converted to DOM node");
    }

    @Override
    public int position() {
        return this.index;
    }
}