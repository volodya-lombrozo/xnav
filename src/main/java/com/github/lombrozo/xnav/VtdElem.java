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
        return this.childs();
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        try {
            final VTDNav nav = this.start();
            int index = nav.getAttrVal(name);
            if (index != -1) {
                return Optional.of(new VtdAttr(name, nav));
            }
            return Optional.empty();
        } catch (final NavException exception) {
            throw new RuntimeException("Error getting attribute", exception);
        }
    }

    @Override
    public Optional<String> text() {
        return Optional.of(
            this.childs()
                .map(Xml::text)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining())
        );
    }

    @ToString.Include
    @EqualsAndHashCode.Include
    @Override
    public String name() {
        try {
            final VTDNav nav = this.start();
            return nav.toString(nav.getCurrentIndex());
        } catch (final NavException exception) {
            throw new RuntimeException("Error getting name", exception);
        }
    }

    @Override
    public Xml copy() {
        return new VtdElem(this.navigator);
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


//    private Stream<Xml> childs() {
//        try {
//            final Stream.Builder<Xml> res = Stream.builder();
//            final VTDNav nav = this.start();
//            int depth = 0;
//            final int last = nav.getTokenCount();
//            for (int index = nav.getCurrentIndex(); index < last; index++) {
//                final int type = nav.getTokenType(index);
//                if (type == VTDNav.TOKEN_STARTING_TAG) {
//                    if (depth == 1) {
//                        final VTDNav copy = nav.cloneNav();
//                        copy.recoverNode(index);
//                        res.add(new VtdElem(nav));
//                    }
//                    depth++;
//                } else if (type == VTDNav.TOKEN_ENDING_TAG) {
//                    depth--;
//                    if (depth == 0) {
//                        break;
//                    }
//                } else if (type == VTDNav.TOKEN_CHARACTER_DATA) {
//                    if (depth == 1) {
//                        res.add(new VtdText(nav));
//                    }
//                }
//            }
//            return res.build();
//        } catch (final NavException exception) {
//            throw new IllegalStateException("Error iterating children", exception);
//        }
//    }


    private Stream<Xml> childs() {
        try {
            final VTDNav nav = this.start();
            final int parentIndex = nav.getCurrentIndex();
            final int parentDepth = nav.getCurrentDepth();
            final int tokenCount = nav.getTokenCount();
            final Stream.Builder<Xml> result = Stream.builder();

            System.out.println("parentIndex: " + parentIndex);
            System.out.println("parentDepth: " + parentDepth);
            System.out.println("tokenCount: " + tokenCount);
            System.out.println("tokenLenght: " + nav.getTokenLength(parentIndex));
            System.out.println("length: " + nav.getStringLength(parentIndex));
            System.out.println("Raw length: " + nav.getRawStringLength(parentIndex));
            System.out.println("Normalized length: " + nav.getNormalizedStringLength(parentIndex));
            System.out.println("fragment: " + nav.getContentFragment());
            // Iterate over tokens starting right after the parent's token.
            for (int i = parentIndex; i < tokenCount; i++) {
                System.out.print(" i: " + i);
                System.out.print(" tokenType: " + nav.getTokenType(i));
                System.out.print(" tokenDepth: " + nav.getTokenDepth(i));
                System.out.print(" tokenString: " + nav.toString(i));
                System.out.println();

                // Once the token's depth is not greater than the parent's, we're done.
                if (nav.getTokenDepth(i) == parentDepth) {
                    int tokenType = nav.getTokenType(i);
                    // Clone and recover to the specific token so that
                    // the resulting navigator is positioned on the child.
                    // ignore tokens that are not element or text
                    if (tokenType == VTDNav.TOKEN_CHARACTER_DATA) {
                        final VTDNav clone = nav.cloneNav();
                        clone.recoverNode(i);
                        result.add(new VtdText(clone));
                        // You might want to handle additional token types if needed.
                    }
                }

                // Only consider direct children (depth exactly one level deeper)
                if (nav.getTokenDepth(i) == parentDepth + 1) {
                    int tokenType = nav.getTokenType(i);
                    // Clone and recover to the specific token so that
                    // the resulting navigator is positioned on the child.
                    // ignore tokens that are not element or text
                    if (tokenType == VTDNav.TOKEN_STARTING_TAG) {
                        final VTDNav clone = nav.cloneNav();
                        clone.recoverNode(i);
                        result.add(new VtdElem(clone));
                        // You might want to handle additional token types if needed.
                    }
                }
            }
            // Restore original state.
            nav.recoverNode(parentIndex);
            return result.build();
        } catch (final NavException e) {
            throw new IllegalStateException("Error iterating children", e);
        }
    }


    /**
     * Start the navigation routine.
     * @return VTD navigator to start from.
     */
    private VTDNav start() {
        return this.navigator.cloneNav();
    }
}