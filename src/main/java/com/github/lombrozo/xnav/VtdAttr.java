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
import java.util.Optional;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Node;

@ToString
@EqualsAndHashCode
public final class VtdAttr implements Xml {

    private final String name;
    private final VTDNav vn;

    public VtdAttr(final String name, final VTDNav vn) {
        this.name = name;
        this.vn = vn;
    }

    @Override
    public Xml child(final String element) {
        throw new UnsupportedOperationException("Attributes do not have children");
    }

    @Override
    public Stream<Xml> children() {
        throw new UnsupportedOperationException("Attributes do not have children");
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        throw new UnsupportedOperationException("Attributes do not have attributes");
    }

    @Override
    public Optional<String> text() {
        try {
            return Optional.ofNullable(vn.toString(vn.getAttrVal(name)));
//            int index = this.vn.getAttrVal(this.name());
//            if (index != -1) {
//                return Optional.of(this.vn.toString(index));
//            }
        } catch (VTDException e) {
            throw new RuntimeException("Error getting attribute text", e);
        }
//        return Optional.empty();
    }

    @Override
    public String name() {
        try {
            return vn.toString(this.vn.getCurrentIndex());
        } catch (VTDException e) {
            throw new RuntimeException("Error getting attribute name", e);
        }
    }

    @Override
    public Xml copy() {
        return new VtdAttr(this.name, vn.cloneNav());
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
