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

import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

@EqualsAndHashCode
public final class VtdXml implements Xml {

    /**
     * Root document.
     */
    private VtdDoc doc;

    /**
     * Constructor.
     * @param xml XML document lines.
     */
    VtdXml(final String... xml) {
        this(String.join("", xml));
    }

    /**
     * Constructor.
     * @param xml XML document string.
     */
    VtdXml(final String xml) {
        this(VtdXml.parseDoc(xml));
    }

    /**
     * Constructor.
     * @param doc VTD document.
     */
    private VtdXml(final VtdDoc doc) {
        this.doc = doc;
    }

    @Override
    public String toString() {
        return this.doc.toString();
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
        return this.doc.children();
    }

    @Override
    public String name() {
        return this.doc.name();
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        return this.doc.attribute(name);
    }

    @Override
    public Optional<String> text() {
        return this.doc.text();
    }

    @Override
    public Xml copy() {
        return new VtdXml(this.doc);
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Parse the XML document.
     * @param xml XML document.
     * @return VTD XML document.
     */
    private static VtdDoc parseDoc(final String xml) {
        try {
            final VTDGen vg = new VTDGen();
            vg.setDoc(xml.getBytes(StandardCharsets.UTF_8));
            vg.parse(false);
            return new VtdDoc(vg.getNav());
        } catch (final ParseException exception) {
            throw new IllegalArgumentException(
                String.format("Can't prepare document for VTD: Invalid XML: %s", xml),
                exception
            );
        }
    }
}
