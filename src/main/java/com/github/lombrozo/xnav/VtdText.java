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
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Node;

@EqualsAndHashCode
public final class VtdText implements IndexedXml {
    @EqualsAndHashCode.Exclude
    private final VTDNav vn;

    @EqualsAndHashCode.Exclude
    private final int id;

    public VtdText(final VTDNav vn, final int id) {
        this.vn = vn;
        this.id = id;
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
            return Optional.of(this.vn.toString(this.id));
        } catch (final NavException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public Xml copy() {
        return new VtdText(this.vn.cloneNav(), this.id);
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public int index() {
        return this.id;
    }
}