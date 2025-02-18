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

/**
 * VTD element.
 * Represents an XML element.
 * This class is thread-safe.
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
@EqualsAndHashCode
final class VtdElem implements OrderedXml {

    /**
     * VTD navigator.
     */
    @EqualsAndHashCode.Exclude
    private final VTDNav navigator;

    /**
     * Order number.
     * Used to sort elements.
     * This number is unique for each element in the document.
     */
    @EqualsAndHashCode.Exclude
    private final int order;

    /**
     * Constructor.
     * @param nav VTD navigator.
     */
    VtdElem(final VTDNav nav) {
        this(nav, nav.getCurrentIndex());
    }

    /**
     * Constructor.
     * @param nav VTD navigator.
     * @param index Element order.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    private VtdElem(final VTDNav nav, final int index) {
        try {
            this.navigator = nav.cloneNav();
            this.navigator.recoverNode(index);
            this.order = index;
        } catch (final NavException exception) {
            throw new IllegalStateException(
                "Error recovering node for the future navigation",
                exception
            );
        }
    }

    @Override
    public Xml child(final String element) {
        return this.children()
            .filter(e -> e.name().equals(element))
            .findFirst()
            .orElseGet(Empty::new);
    }

    @Override
    public Stream<Xml> children() {
        try {
            final SortedSet<OrderedXml> results = new TreeSet<>(
                Comparator.comparingInt(OrderedXml::position)
            );
            final VTDNav nav = this.start();
            final int depth = nav.getCurrentDepth();
            if (nav.toElement(VTDNav.FIRST_CHILD)) {
                do {
                    final int curr = nav.getCurrentIndex();
                    results.add(new VtdElem(nav, curr));
                    VtdElem.scanUp(nav, curr, depth).forEach(results::add);
                } while (nav.toElement(VTDNav.NEXT_SIBLING));
                final int last = this.findLastTokenIndex(nav);
                nav.toElement(VTDNav.PARENT);
                VtdElem.scanUp(nav, last, depth).forEach(results::add);
            } else {
                final int text = nav.getText();
                if (text != -1) {
                    results.add(new VtdText(nav, text));
                }
            }
            return results.stream().map(Xml.class::cast);
        } catch (final NavException exception) {
            throw new IllegalStateException("Error getting element children", exception);
        }
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        try {
            final VTDNav nav = this.start();
            final int index = nav.getAttrVal(name);
            final Optional<Xml> result;
            if (index == -1) {
                result = Optional.empty();
            } else {
                result = Optional.of(new VtdAttr(name, nav));
            }
            return result;
        } catch (final NavException exception) {
            throw new IllegalStateException(
                String.format("Error getting attribute '%s'", name),
                exception
            );
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
            throw new IllegalStateException(
                String.format("Error getting name from '%s'", this),
                exception
            );
        }
    }

    @Override
    public Xml copy() {
        return new VtdElem(this.navigator);
    }

    @Override
    public Node node() {
        return new StringNode(this.toString()).toNode().getFirstChild();
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

    @Override
    public int position() {
        return this.order;
    }

    /**
     * Get a list of attributes.
     * @return List of attributes.
     */
    private String attrs() {
        try {
            final VTDNav nav = this.start();
            final AutoPilot pilot = new AutoPilot(nav);
            pilot.selectAttr("*");
            final Stream.Builder<Xml> builder = Stream.builder();
            int identifier = pilot.iterateAttr();
            while (identifier != -1) {
                builder.add(new VtdAttr(nav.toString(identifier), nav));
                identifier = pilot.iterateAttr();
            }
            final String all = builder.build()
                .map(Xml::toString)
                .collect(Collectors.joining(" "));
            final String res;
            if (all.isEmpty()) {
                res = "";
            } else {
                res = String.format(" %s", all);
            }
            return res;
        } catch (final NavException exception) {
            throw new IllegalStateException(
                String.format("Error getting list of attributes in '%s'", this),
                exception
            );
        }
    }

    /**
     * Find elements from the current element upper to the redline.
     * @param nav VTD navigator to use.
     * @param from Start index.
     * @param redline Redline depth - the depth to stop at.
     * @return Stream of elements from top to up.
     */
    private static Stream<OrderedXml> scanUp(final VTDNav nav, final int from, final int redline) {
        final Stream.Builder<OrderedXml> result = Stream.builder();
        for (int index = from - 1; index >= 0; index = index - 1) {
            final int depth = nav.getTokenDepth(index);
            final int type = nav.getTokenType(index);
            if (depth == redline && type == VTDNav.TOKEN_CHARACTER_DATA) {
                result.add(new VtdText(nav, index));
            }
            if (depth == redline) {
                break;
            }
        }
        return result.build();
    }

    /**
     * Find the last token index in the current element.
     * <p>
     *     This method is used to find the last token index in the current element.
     *     It's used to determine the end of the element.
     * </p>
     * @param nav VTD navigator.
     * @return Last token index.
     */
    private int findLastTokenIndex(final VTDNav nav) {
        return this.findLastToken(nav.cloneNav());
    }

    /**
     * Find the last token index in the current element.
     * @param current VTD navigator.
     * @return Last token index.
     */
    @SuppressWarnings("PMD.ConfusingTernary")
    private int findLastToken(final VTDNav current) {
        try {
            final int result;
            if (!current.toElement(VTDNav.PARENT)) {
                result = current.getTokenCount();
            } else if (current.toElement(VTDNav.NEXT_SIBLING)) {
                result = current.getCurrentIndex();
            } else {
                result = this.findLastToken(current);
            }
            return result;
        } catch (final NavException exception) {
            throw new IllegalStateException(
                String.format("Error finding last token index in '%s'", current),
                exception
            );
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
