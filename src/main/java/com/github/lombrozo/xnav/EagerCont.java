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

import com.github.lombrozo.xnav.Xml;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Node;

@EqualsAndHashCode
@ToString
public final class EagerCont implements Xml {


    private final List<Xml> elements;
    private final List<Xml> chardata;

    public EagerCont(final List<Xml> elements, final List<Xml> chardata) {
        this.elements = elements;
        this.chardata = chardata;
    }

    @Override
    public Xml child(final String element) {
        return this.elements.stream()
            .filter(e -> e.name().equals(element))
            .findFirst()
            .orElse(new Empty());
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Optional<String> text() {
        return Optional.empty();
    }

    @Override
    public Stream<Xml> children() {
        return Stream.empty();
    }

    @Override
    public String name() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Xml copy() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
