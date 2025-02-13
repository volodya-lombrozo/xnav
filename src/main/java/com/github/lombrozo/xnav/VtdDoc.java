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

import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

@EqualsAndHashCode
public final class VtdDoc implements Xml {

    @EqualsAndHashCode.Exclude
    private final VTDNav vn;

    public VtdDoc(final VTDNav vn) {
        this.vn = vn;
    }

    @Override
    public String toString() {
        return String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>%s",
            new VtdElem(this.vn)
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
        return Stream.of(new VtdElem(this.vn));
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return new VtdElem(this.vn).attribute(name);
    }


    @Override
    public Optional<String> text() {
        final Stream<Xml> children = this.children();
        final List<Xml> collect = children.collect(Collectors.toList());
        return Optional.of(
            collect.stream()
                .map(Xml::text)
                .flatMap(Optional::stream)
                .collect(Collectors.joining())
        );
    }

    @EqualsAndHashCode.Include
    @Override
    public String name() {
        try {
            return vn.cloneNav().toString(this.vn.getCurrentIndex());
        } catch (VTDException e) {
            throw new RuntimeException("Error getting name", e);
        }
    }

    @Override
    public Xml copy() {
        return new VtdDoc(vn.cloneNav());
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
