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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test case for {@link Xnav}.
 * @since 0.1
 */
final class XnavTest {

    @Test
    void convertsToString() {
        MatcherAssert.assertThat(
            "We expect the navigator to be converted to string",
            new Xnav("<root><child>text</child></root>").element("root").toString(),
            Matchers.equalTo("Navigator(xml=<root><child>text</child></root>)")
        );
    }

    @Test
    void createsNavigatorFromNode() {
        MatcherAssert.assertThat(
            "We expect the navigator to be created from node",
            new Xnav(new StringNode("<a>text</a>").toNode())
                .element("a")
                .toString(),
            Matchers.equalTo("Navigator(xml=<a>text</a>)")
        );
    }

    @Test
    void retrievesSeveralElements() {
        MatcherAssert.assertThat(
            "We expect the navigator to retrieve several elements",
            new Xnav("<root><a>1</a><b>2</b><c>3</c></root>")
                .element("root")
                .elements()
                .map(Xnav::text)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()),
            Matchers.contains("1", "2", "3")
        );
    }

    @Test
    void copies() {
        final Xnav navigator = new Xnav("<root><a>1</a><b>2</b><c>3</c></root>");
        MatcherAssert.assertThat(
            "We expect the navigator to be copied",
            navigator.copy(),
            Matchers.allOf(
                Matchers.equalTo(navigator),
                Matchers.not(Matchers.sameInstance(navigator))
            )
        );
    }

    @Test
    void retrievesNode() {
        final Xnav navigator = new Xnav("<root><a>1</a><b>2</b><c>3</c></root>");
        MatcherAssert.assertThat(
            "We expect the navigator to retrieve the node",
            navigator.element("root")
                .element("a")
                .node()
                .isEqualNode(new StringNode("<a>1</a>").toNode().getFirstChild()),
            Matchers.is(true)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filters")
    void filtersSuccessfully(final String title, final Filter filter, final List<String> expected) {
        MatcherAssert.assertThat(
            String.format("We expect the navigator to filter elements in the '%s' check", title),
            new Xnav(
                "<root><a attr='a'>a</a><b attr='b'>b</b><c attr='c'>c</c><d attr='d'>d</d><e>e</e><f>f</f></root>"
            ).element("root")
                .elements(filter)
                .map(Xnav::text)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()),
            Matchers.equalTo(expected)
        );
    }

    @ParameterizedTest
    @MethodSource({"elementPaths", "attributePaths"})
    void retrievesTextFromElements(final Xnav navigator, final String expected) {
        MatcherAssert.assertThat(
            "We expect the text to be retrieved correctly",
            navigator.text().orElseThrow(
                () -> new IllegalStateException(
                    String.format("Text not found in navigator %s", navigator)
                )
            ).replaceAll(" ", "").trim(),
            Matchers.equalTo(expected)
        );
    }

    /**
     * Provide navigators to test.
     * This method provides a stream of arguments to the test method:
     * {@link #retrievesTextFromElements(Xnav, String)}.
     * @return Stream of arguments.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> elementPaths() {
        final Xnav xml = new Xnav(
            String.join(
                "\n",
                "<program><metas>",
                "  <meta>",
                "    <head>version</head>",
                "    <tail>1.2.3</tail>",
                "  </meta>",
                "</metas></program>"
            )
        );
        final String program = "program";
        final String metas = "metas";
        return Stream.of(
            Arguments.of(xml, ""),
            Arguments.of(xml.element(program).element(metas), "version\n1.2.3"),
            Arguments.of(xml.element(program).element(metas).element("meta"), "version\n1.2.3"),
            Arguments.of(
                xml.element(program)
                    .element(metas)
                    .element("meta")
                    .element("head"),
                "version"
            ),
            Arguments.of(
                xml.element(program)
                    .element(metas)
                    .element("meta")
                    .element("tail"),
                "1.2.3"
            )
        );
    }

    /**
     * Provide filters to test.
     * This method provides a stream of arguments to the test method.
     * @return Stream of arguments.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> filters() {
        return Stream.of(
            Arguments.of("Empty ANY filter", Filter.any(), List.of("a", "b", "c", "d", "e", "f")),
            Arguments.of("Empty ALL filter", Filter.all(), List.of("a", "b", "c", "d", "e", "f")),
            Arguments.of("a", Filter.withName("a"), List.of("a")),
            Arguments.of("b", Filter.withName("b"), List.of("b")),
            Arguments.of("c", Filter.withName("c"), List.of("c")),
            Arguments.of("d", Filter.withName("d"), List.of("d")),
            Arguments.of("e", Filter.withName("e"), List.of("e")),
            Arguments.of("f", Filter.withName("f"), List.of("f")),
            Arguments.of("With attribute 'a'", Filter.withAttribute("attr", "a"), List.of("a")),
            Arguments.of(
                "Has attribute 'attr'",
                Filter.hasAttribute("attr"),
                List.of("a", "b", "c", "d")
            ),
            Arguments.of(
                "Has not attribute 'attr'",
                Filter.not(Filter.hasAttribute("attr")),
                List.of("e", "f")
            ),
            Arguments.of(
                "With name 'a' and attribute 'attr'",
                Filter.all(Filter.withName("a"), Filter.withAttribute("attr", "a")),
                List.of("a")
            ),
            Arguments.of(
                "With name 'a' or 'b'",
                Filter.any(Filter.withName("a"), Filter.withName("b")),
                List.of("a", "b")
            ),
            Arguments.of(
                "With name 'a' and 'b'",
                Filter.all(Filter.withName("a"), Filter.withName("b")),
                List.of()
            )
        );
    }

    /**
     * Provide navigators to test.
     * This method provides a stream of arguments to the test method:
     * {@link #retrievesTextFromElements(Xnav, String)}.
     * @return Stream of arguments.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> attributePaths() {
        final Xnav xml = new Xnav(
            String.join(
                "\n",
                "<prog progattr='1'>",
                "  <m metarg='2'>",
                "    <h harg='3'>version</h>",
                "    <t targ='4'>1.2.3</t>",
                "  </m></prog>"
            )
        );
        return Stream.of(
            Arguments.of(xml.element("prog").attribute("progattr"), "1"),
            Arguments.of(xml.element("prog").element("m").attribute("metarg"), "2"),
            Arguments.of(xml.element("prog").element("m").element("h").attribute("harg"), "3"),
            Arguments.of(xml.element("prog").element("m").element("t").attribute("targ"), "4")
        );
    }
}
