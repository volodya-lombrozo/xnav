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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Node;

@ToString
@EqualsAndHashCode
public final class VtdElem implements Xml {

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final VTDNav vn;

    public VtdElem(final VTDNav vn) {
        this.vn = vn.cloneNav();
    }

    @Override
    public Xml child(final String element) {
        final Stream<Xml> children = this.children();
        final List<Xml> collect = children.collect(Collectors.toList());
        return collect.stream()
            .filter(e -> e.name().equals(element))
            .findFirst()
            .orElseThrow();
    }

    @Override
    public Stream<Xml> children() {
        return VtdElem.childish(this.vn.cloneNav());
//        final Stream.Builder<Xml> builder = Stream.builder();
//        try {
//            final VTDNav current = this.vn.cloneNav();
//            if (current.toElement(VTDNav.FIRST_CHILD)) {
//                do {
//                    builder.add(new VtdElem(current.cloneNav()));
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
                return Optional.of(new VtdAttr(name, this.vn.cloneNav()));
            }
        } catch (final VTDException exception) {
            throw new RuntimeException("Error getting attribute", exception);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> text() {
        return Optional.of(childish(this.vn.cloneNav())
            .map(Xml::text)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.joining())
        );
//        try {
//            return Optional.of(getElementTextPreservingSpaces(this.vn.cloneNav()));
//            final String spaces = getTextWithAllSpaces(this.vn.cloneNav());
//            return Optional.of(spaces);
//        } catch (final NavException exception) {
//            throw new RuntimeException(exception);
//        }
    }


    public static Stream<Xml> childish(VTDNav vn) {
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

    private Xml element(int tokenType, VTDNav nav) {
        if (tokenType == VTDNav.TOKEN_CHARACTER_DATA) {
            return new VtdText(nav); // Preserve raw string to keep spaces
        } else {
            return new VtdElem(nav);
        }
    }

    public static String getElementTextPreservingSpaces(VTDNav vn) throws NavException {
        final StringBuilder text = new StringBuilder();
        int currentIndex = vn.getCurrentIndex();
        int index = vn.getCurrentIndex() + 1; // Start looking at the next token
        int tokenType;
        final int all = vn.getTokenCount();
        while (index < all) {
            tokenType = vn.getTokenType(index);
            final String string = vn.toString(index);
            text.append(string);
//            if (tokenType == VTDNav.TOKEN_CHARACTER_DATA || tokenType == VTDNav.TOKEN_CDATA_VAL) {
//                final String string = vn.toRawString(index);
//                vn.toString(index);
//                text.append(string); // Preserve raw string to keep spaces
//            } else if (tokenType == VTDNav.TOKEN_STARTING_TAG) {
//                // Move into the element and process recursively
//                if (vn.toElement(VTDNav.FIRST_CHILD)) {
//                    text.append(VtdElem.getElementTextPreservingSpaces(vn));
//                    vn.toElement(VTDNav.PARENT); // Return to the parent after processing
//                }
//            } else if (tokenType == VTDNav.TOKEN_ENDING_TAG) {
//                break; // Stop when reaching the end of the current element
//            }
            index++;
        }
        vn.recoverNode(currentIndex); // Restore position
        return text.toString();
    }

    public static String getTextWithAllSpaces(VTDNav vn) throws NavException {
        StringBuilder text = new StringBuilder();
        int currentIndex = vn.getCurrentIndex(); // Save the current position
        int tokenIndex = vn.getCurrentIndex() + 1; // Start scanning from next token
        int tokenCount = vn.getTokenCount();

        while (tokenIndex < tokenCount) {
            int tokenType = vn.getTokenType(tokenIndex);

            if (tokenType == VTDNav.TOKEN_CHARACTER_DATA) {
                text.append(vn.toRawString(tokenIndex)); // Append regular text or CDATA
//            } else if (tokenType == VTDNav.TOKEN_STARTING_TAG) {
//                int offset = vn.getTokenOffset(tokenIndex);
//                int prevTokenOffset = vn.getTokenOffset(tokenIndex - 1);
//
//                 Capture spaces between the previous token and the new start tag
//                if (offset > prevTokenOffset + vn.getTokenLength(tokenIndex - 1)) {
//                    final String string = vn.toRawString(
//                        prevTokenOffset + vn.getTokenLength(tokenIndex - 1),
//                        offset - (prevTokenOffset + vn.getTokenLength(tokenIndex - 1))
//                    );
//                    String interElementSpace = string;
//                    text.append(interElementSpace);
//                }

//                if (vn.toElement(VTDNav.FIRST_CHILD)) {
//                    text.append(getTextWithAllSpaces(vn));
//                    vn.toElement(VTDNav.PARENT); // Move back up
//                }
            } else if (tokenType == VTDNav.TOKEN_ENDING_TAG) {
                break; // Stop when reaching the end of the current element
            }

            tokenIndex++;
        }

        vn.recoverNode(currentIndex); // Restore original position
        return text.toString();
    }

    @ToString.Include
    @EqualsAndHashCode.Include
    @Override
    public String name() {
        try {
            return this.vn.cloneNav().toString(this.vn.getCurrentIndex());
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
}