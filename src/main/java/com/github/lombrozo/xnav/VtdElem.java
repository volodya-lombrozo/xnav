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
import java.util.List;
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

//    @EqualsAndHashCode.Exclude
//    private final long fragment;
//
//    @EqualsAndHashCode.Exclude
//    private final int last;

    VtdElem(final VTDNav nav, int index) {
        try {
            this.navigator = nav.cloneNav();
            this.navigator.recoverNode(index);
            this.index = index;
//            this.fragment = nav.getElementFragment();
//            this.last = findLastTokenIndex(nav);
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

    private Stream<Xml> childs() {
        try {
            final SortedSet<IndexedXml> results = new TreeSet<>(
                Comparator.comparingInt(IndexedXml::index)
            );
            final VTDNav nav = this.start();
            final int depth = nav.getCurrentDepth();
            final int max = nav.getTokenCount();
//            printIndexes(nav);
            if (nav.toElement(VTDNav.FIRST_CHILD)) {
                do {
                    final int curr = nav.getCurrentIndex();
                    results.add(new VtdElem(nav, curr));
                    this.scanUP(nav, curr, depth).forEach(results::add);
                } while (nav.toElement(VTDNav.NEXT_SIBLING));
//                final int ind = findLastIndex(nav);
//                this.scanUP(nav, ind, depth).forEach(results::add);
//                System.out.println("Last index: " + ind);

//                OLD:
//                this.scanDown(nav, nav.getCurrentIndex(), depth, max).forEach(results::add);
//                _______
                nav.toElement(VTDNav.PARENT);
//                final int last = findLastTokenIndex(nav);
//                this.scanUP(nav, last, depth).forEach(results::add);
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
                result.add(new VtdText(nav, i));
            }
            if (depth == redline) {
                break;
            }
        }
        return result.build();
    }

    private Stream<IndexedXml> scanDown(VTDNav nav, int from, int redline, int max) {
        findLastTokenIndex(nav);
        final List<IndexedXml> res = new ArrayList<>(0);
        int iterations = 0;
        for (int i = from + 1; i < max; i++) {
            iterations++;
            final int depth = nav.getTokenDepth(i);
            final int type = nav.getTokenType(i);
            if (
                depth == redline
                    && type == VTDNav.TOKEN_CHARACTER_DATA
                    && nav.getTokenLength(i) > 0
            ) {
                res.add(new VtdText(nav, i));
            }
            if (depth == redline) {
                break;
            }
        }
        System.out.println(
            "scanDown: from: " + from + " looking depth: " + redline + " Number of tokens: " + max + " Iterations: " + iterations
        );
//        return result.build();
        return res.stream();
    }

    private int findLastTokenIndex(final VTDNav nav) {
        try {
            int elementOffset = (int) nav.getElementFragment();
            int length = (int) (nav.getElementFragment() >> 32);
//            int elementOffset = (int) fragment;
//            int length = (int) (fragment >> 32);
            int elementEndOffset = elementOffset + length;
//            System.out.printf(
//                "%s | offset: %d length: %d endOffset: %d%n",
//                new VtdElem(nav),
//                elementOffset,
//                length,
//                elementEndOffset
//            );
            int low = nav.getCurrentIndex();
            int high = nav.getTokenCount();
            int lastTokenIndex = -1;
            while (low <= high) {
                int mid = (low + high) / 2;
                int tokenOffset = nav.getTokenOffset(mid);
                int tokenLength = nav.getTokenLength(mid);
                int tokenEndOffset = tokenOffset + tokenLength;

                if (tokenOffset >= elementOffset && tokenEndOffset <= elementEndOffset) {
                    lastTokenIndex = mid;
                    low = mid + 1; // Continue searching in the upper half
                } else if (tokenOffset < elementOffset) {
                    low = mid + 1; // Search in the upper half
                } else {
                    high = mid - 1; // Search in the lower half
                }
            }
            return lastTokenIndex;
//            System.out.println("Last token index: " + lastTokenIndex);


        } catch (final NavException exception) {
            throw new RuntimeException(exception);
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