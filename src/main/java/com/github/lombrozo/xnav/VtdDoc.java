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

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Node;

@ToString
@EqualsAndHashCode
public final class VtdDoc implements Xml {

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final VTDNav vn;

    public VtdDoc(final VTDNav vn) {
        this.vn = vn;
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
//        Stream.Builder<Xml> builder = Stream.builder();
//        try {
//            final VTDNav current = this.vn.cloneNav();
//            if (current.toElement(VTDNav.FIRST_CHILD)) {
//                do {
//                    builder.add(new VtdElem(current));
//                } while (current.toElement(VTDNav.NEXT_SIBLING));
//            }
//        } catch (VTDException e) {
//            throw new RuntimeException("Error iterating children", e);
//        }
//        return builder.build();
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        try {
            int index = this.vn.cloneNav().getAttrVal(name);
            if (index != -1) {
                return Optional.of(new VtdDoc(this.vn.cloneNav()));
            }
        } catch (VTDException e) {
            throw new RuntimeException("Error getting attribute", e);
        }
        return Optional.empty();
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

    @ToString.Include
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
