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
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

@EqualsAndHashCode
public final class VtdElem implements IndexedXml {

    @EqualsAndHashCode.Exclude
    private final VTDNav navigator;

    @EqualsAndHashCode.Exclude
    private final int index;

    VtdElem(final VTDNav nav, int index) {
        try {
            this.navigator = nav.cloneNav();
            this.navigator.recoverNode(index);
            this.index = index;
        } catch (final NavException exception) {
            throw new RuntimeException("Error recovering node", exception);
        }
    }

    /**
     * Constructor.
     * @param nav VTD navigator.
     */
    VtdElem(final VTDNav nav) {
        this(nav, nav.getCurrentIndex());
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
//        return this.childs();
        return this.optimizedChildren();
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
            this.children()
                .map(Xml::text)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining())
        );
    }

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

    @EqualsAndHashCode.Include
    @Override
    public String toString() {
        final String name = this.name();
        return String.format(
            "<%s%s>%s</%s>",
            name,
            this.attrs(),
            this.children().map(Xml::toString).collect(Collectors.joining()),
            name
        );
    }

    private String attrs() {
        final String res = this.attributes().map(Xml::toString).collect(Collectors.joining(" "));
        if (res.isEmpty()) {
            return "";
        } else {
            return " " + res;
        }
    }

    private Stream<Xml> attributes() {
        try {
            final VTDNav nav = this.start();
            final AutoPilot pilot = new AutoPilot(nav);
            pilot.selectAttr("*");
            final Stream.Builder<Xml> builder = Stream.builder();
            int id;
            while ((id = pilot.iterateAttr()) != -1) {
                builder.add(new VtdAttr(nav.toString(id), nav));
            }
            return builder.build();
        } catch (final NavException exception) {
            throw new RuntimeException("Error getting attribute", exception);
        }
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


    /**
     * It is the working solution, but not efficient since it travers all tokens.
     * @return Stream of children.
     */
    private Stream<Xml> childs() {
        final VTDNav nav = this.start();
        final int parentIndex = nav.getCurrentIndex();
        final int parentDepth = nav.getCurrentDepth();
        final int max = nav.getTokenCount();
        final Stream.Builder<Xml> result = Stream.builder();
        for (int i = parentIndex + 1; i < max; i++) {
            final int type = nav.getTokenType(i);
            final int depth = nav.getTokenDepth(i);
            if (depth < parentDepth) {
                break;
            }
            if (depth == parentDepth) {
                if (type == VTDNav.TOKEN_CHARACTER_DATA) {
                    result.add(new VtdText(nav, i));
                }
                if (type == VTDNav.TOKEN_STARTING_TAG) {
                    break;
                }
            }
            if (depth == parentDepth + 1) {
                if (type == VTDNav.TOKEN_STARTING_TAG) {
                    result.add(new VtdElem(nav, i));
                }
            }
        }
        return result.build();
    }


    private Stream<Xml> optimizedChildren() {
        try {
            final SortedSet<IndexedXml> results = new TreeSet<>(
                Comparator.comparingInt(IndexedXml::index)
            );
            final VTDNav nav = this.start();
            final int original = nav.getCurrentIndex();

            // Collect child elements
            if (nav.toElement(VTDNav.FIRST_CHILD)) {
                do {
                    results.add(new VtdElem(nav, nav.getCurrentIndex()));
                } while (nav.toElement(VTDNav.NEXT_SIBLING));
                nav.toElement(VTDNav.PARENT);
            }

            // Collect text nodes
            final int text = nav.getText();
            if (text != -1) {
                results.add(new VtdText(nav, text));
            }

            nav.recoverNode(original);
            return results.stream().map(Xml.class::cast);
        } catch (final NavException e) {
            throw new RuntimeException("Error getting children", e);
        }
    }

    /**
     * Start the navigation routine.
     * @return VTD navigator to start from.
     */
    private VTDNav start() {
        return this.navigator.cloneNav();
    }


    @Override
    public int index() {
        return this.index;
    }
}