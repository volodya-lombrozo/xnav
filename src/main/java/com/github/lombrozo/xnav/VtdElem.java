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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
//        return this.optimizedChildren();
        return this.optimizedChildren2();
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

    private Stream<Xml> optimizedChildren2() {
        try {
            final SortedSet<IndexedXml> results = new TreeSet<>(
                Comparator.comparingInt(IndexedXml::index)
            );
            final VTDNav nav = this.start();
            final int depth = nav.getCurrentDepth();
            if (nav.toElement(VTDNav.FIRST_CHILD)) {
                printIndexes(nav);
                final int max = nav.getTokenCount();
                do {
                    final int curr = nav.getCurrentIndex();
                    results.add(new VtdElem(nav, curr));
                    this.scanUP(nav, curr, depth).forEach(results::add);
                } while (nav.toElement(VTDNav.NEXT_SIBLING));
                this.scanDown(nav, nav.getCurrentIndex(), depth, max).forEach(results::add);
                nav.toElement(VTDNav.PARENT);
            } else {
                int text;
                if ((text = nav.getText()) != -1) {
                    results.add(new VtdText(nav, text));
                }
            }
            return results.stream().map(Xml.class::cast);
        } catch (final NavException e) {
            throw new RuntimeException("Error getting children", e);
        }
    }

    private Stream<IndexedXml> scanUP(VTDNav nav, int from, int redline) {
        final Stream.Builder<IndexedXml> result = Stream.builder();
        for (int i = from - 1; i >= 0; i--) {
            final int depth = nav.getTokenDepth(i);
            final int type = nav.getTokenType(i);
            if (depth == redline && type == VTDNav.TOKEN_CHARACTER_DATA) {
                try {
                    System.out.println("["+i+"]scanUP: Try to find text between " + from + " and 0 with depth " + redline + " -> " + nav.toString(i));
                } catch (final NavException exception) {
                    throw new RuntimeException(exception);
                }
                result.add(new VtdText(nav, i));
            }
            if (depth == redline) {
                break;
            }
        }
        return result.build();
    }

    private Stream<IndexedXml> scanDown(VTDNav nav, int from, int redline, int max) {
        final Stream.Builder<IndexedXml> result = Stream.builder();
        for (int i = from + 1; i < max; i++) {
            final int depth = nav.getTokenDepth(i);
            final int type = nav.getTokenType(i);
            if (depth == redline && type == VTDNav.TOKEN_CHARACTER_DATA) {
                try {
                    System.out.println("scanDown: Try to find text between " + from + " and " + redline + " -> " + nav.toString(i));
                } catch (final NavException exception) {
                    throw new RuntimeException(exception);
                }
                result.add(new VtdText(nav, i));
            }
            if (depth == redline) {
                break;
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
            printIndexes(nav);

            List<Integer> intervals = new ArrayList<>(0);
            final int start = nav.getCurrentIndex();
            intervals.add(start);
            if (nav.toElement(VTDNav.FIRST_CHILD)) {
                do {
                    final int curr = nav.getCurrentIndex();
                    intervals.add(curr);
                    results.add(new VtdElem(nav, curr));
                } while (nav.toElement(VTDNav.NEXT_SIBLING));
                nav.toElement(VTDNav.PARENT);
                final VTDNav clone = nav.cloneNav();
                final boolean last = clone.toElement(VTDNav.NEXT_SIBLING);
                final int l = last ? clone.getCurrentIndex() : clone.getTokenCount() - 1;
                intervals.add(l);

                System.out.println(intervals);
                // wtf

                for (int i = 0; i < intervals.size() - 1; i += 2) {
                    int from = intervals.get(i);
                    int to = intervals.get(i + 1);
                    for (int j = from + 1; j < to; j++) {
                        if (nav.getTokenType(j) == VTDNav.TOKEN_CHARACTER_DATA) {
                            System.out.println(
                                "Try to find text between " + from + " and " + to + " -> " + nav.toString(
                                    j));
                        }
                    }
                }

//                for (int i = 0; i < intervals.size() - 2; i++) {
//                    int from = intervals.get(i);
//                    int to = intervals.get(i + 1);
//
//                    for (int j = from + 1; j < to; j++) {
//                        if (nav.getTokenType(j) == VTDNav.TOKEN_CHARACTER_DATA) {
//                            System.out.println(
//                                "Try to find text between " + from + " and " + to + " -> " + nav.toString(
//                                    j));
//                        }
//                    }
//                }
            } else {
                int text;
                if ((text = nav.getText()) != -1) {
                    results.add(new VtdText(nav, text));
                }
            }


            return results.stream().map(Xml.class::cast);
        } catch (final NavException e) {
            throw new RuntimeException("Error getting children", e);
        }
    }

    private void printIndexes(final VTDNav nav) {
        final VTDNav clone = nav.cloneNav();
        System.out.println("Indexes: ");
        for (int i = 0; i < clone.getTokenCount(); i++) {
            try {
                System.out.println(
                    i + " -> '" + clone.toString(i) + "'" + " depth: " + clone.getTokenDepth(i)
                );
            } catch (final NavException exception) {
                throw new RuntimeException(exception);
            }
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