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
 * Test case for {@link Navigator}.
 * @since 0.1
 */
final class NavigatorTest {

    @Test
    void convertsToString() {
        MatcherAssert.assertThat(
            "We expect the navigator to be converted to string",
            new Navigator("<root><child>text</child></root>").element("root").toString(),
            Matchers.equalTo("Navigator(node=<root><child>text</child></root>)")
        );
    }

    @Test
    void createsNavigatorFromNode() {
        MatcherAssert.assertThat(
            "We expect the navigator to be created from node",
            new Navigator(new StringNode("<a>text</a>").toNode())
                .element("a")
                .toString(),
            Matchers.equalTo("Navigator(node=<a>text</a>)")
        );
    }

    @Test
    void retrievesSeveralElements() {
        MatcherAssert.assertThat(
            "We expect the navigator to retrieve several elements",
            new Navigator("<root><a>1</a><b>2</b><c>3</c></root>")
                .element("root")
                .elements()
                .map(Navigator::text)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()),
            Matchers.contains("1", "2", "3")
        );
    }

    @ParameterizedTest
    @MethodSource({"elementPaths", "attributePaths"})
    void retrievesTextFromElements(final Navigator navigator, final String expected) {
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
     * {@link #retrievesTextFromElements(Navigator, String)}.
     * @return Stream of arguments.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> elementPaths() {
        final Navigator xml = new Navigator(
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
                xml.element(program).element(metas).element("meta").element("head"), "version"),
            Arguments.of(
                xml.element(program).element(metas).element("meta").element("tail"), "1.2.3")
        );
    }

    /**
     * Provide navigators to test.
     * This method provides a stream of arguments to the test method:
     * {@link #retrievesTextFromElements(Navigator, String)}.
     * @return Stream of arguments.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> attributePaths() {
        final Navigator xml = new Navigator(
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
