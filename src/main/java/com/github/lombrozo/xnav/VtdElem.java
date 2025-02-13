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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Node;

@EqualsAndHashCode
public final class VtdElem implements Xml {

    @EqualsAndHashCode.Exclude
    private final VTDNav navigator;

    /**
     * Constructor.
     * @param nav VTD navigator.
     */
    VtdElem(final VTDNav nav) {
        this.navigator = nav.cloneNav();
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
        return VtdElem.child(this.navigator.cloneNav());
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        try {
            int index = this.navigator.cloneNav().getAttrVal(name);
            if (index != -1) {
                return Optional.of(new VtdAttr(name, this.navigator.cloneNav()));
            }
        } catch (final VTDException exception) {
            throw new RuntimeException("Error getting attribute", exception);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> text() {
        return Optional.of(VtdElem.child(this.navigator.cloneNav())
            .map(Xml::text)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.joining())
        );
    }

    public static Stream<Xml> child(final VTDNav vn) {
        try {
            final Stream.Builder<Xml> builder = Stream.builder();
            int currentIndex = vn.getCurrentIndex();
            int index = vn.getCurrentIndex() + 1; //// Start looking at the next token
            int tokenType = vn.getTokenType(index);
            while (tokenType != VTDNav.TOKEN_STARTING_TAG
                && tokenType != VTDNav.TOKEN_CHARACTER_DATA) {
                index++;
                tokenType = vn.getTokenType(index);
            }
            if (tokenType == VTDNav.TOKEN_CHARACTER_DATA) {
                builder.add(new VtdText(vn)); // Preserve raw string to keep spaces
            } else {
                final VTDNav current = vn.cloneNav();
                current.recoverNode(currentIndex);
                if (current.toElement(VTDNav.FIRST_CHILD)) {
                    do {
                        builder.add(new VtdElem(current.cloneNav()));
                    } while (current.toElement(VTDNav.NEXT_SIBLING));
                }
            }
            vn.recoverNode(currentIndex); // Restore position
            return builder.build();
        } catch (NavException e) {
            throw new RuntimeException("Error iterating children", e);
        }
    }


    @ToString.Include
    @EqualsAndHashCode.Include
    @Override
    public String name() {
        try {
            return this.navigator.cloneNav().toString(this.navigator.getCurrentIndex());
        } catch (final VTDException exception) {
            throw new RuntimeException("Error getting name", exception);
        }
    }

    @Override
    public Xml copy() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Node node() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String toString() {
        final String name = this.name();
        return String.format(
            "<%s>%s</%s>",
            name,
            this.children().map(Xml::toString).collect(Collectors.joining()),
            name
        );
    }
}