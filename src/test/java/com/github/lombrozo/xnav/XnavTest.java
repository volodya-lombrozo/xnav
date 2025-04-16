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

import com.yegor256.Together;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test case for {@link Xnav}.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
final class XnavTest {

    @Test
    void createsXnavFromPath(@TempDir final Path temp) throws IOException {
        final Path file = temp.resolve("path.xml");
        Files.write(
            file,
            "<path>point</path>".getBytes(StandardCharsets.UTF_8)
        );
        MatcherAssert.assertThat(
            "We expect the navigator to be created from path",
            new Xnav(file).toString(),
            Matchers.containsString("point")
        );
    }

    @Test
    void createsXnavFromFile(@TempDir final Path temp) throws IOException {
        final Path file = temp.resolve("file.xml");
        Files.write(
            file,
            "<file>content</file>".getBytes(StandardCharsets.UTF_8)
        );
        MatcherAssert.assertThat(
            "We expect the navigator to be created from file",
            new Xnav(file.toFile()).toString(),
            Matchers.containsString("content")
        );
    }

    @Test
    void failsToCreateXnavFromNonExistentFile(@TempDir final Path temp) {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new Xnav(temp.resolve("nonexistent.xml")),
            "We expect the navigator to fail creating from non-existent file"
        );
    }

    @Test
    void convertsToString() {
        MatcherAssert.assertThat(
            "We expect the navigator to be converted to string",
            new Xnav("<root><child>text</child></root>").element("root").toString(),
            Matchers.equalTo("Xnav(xml=<root><child>text</child></root>)")
        );
    }

    @Test
    void createsNavigatorFromNode() {
        MatcherAssert.assertThat(
            "We expect the navigator to be created from node",
            new Xnav(new StringNode("<a>text</a>").toNode())
                .element("a")
                .toString(),
            Matchers.equalTo("Xnav(xml=<a>text</a>)")
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

    @Test
    void retrievesElementsConcurrently() {
        final Xnav xnav = new Xnav("<base><a>1</a><b>2</b><c>3</c></base>");
        final List<String> res = new Together<>(
            3,
            idx -> {
                final String root = "base";
                final String first = xnav.element(root).element("a").text().orElseThrow();
                final String second = xnav.element(root).element("b").text().orElseThrow();
                final String third = xnav.element(root).element("c").text().orElseThrow();
                return String.format("%s %s %s", first, second, third);
            }
        ).asList();
        final String expected = "1 2 3";
        MatcherAssert.assertThat(
            "We expect the navigator to retrieve elements concurrently",
            res,
            Matchers.contains(expected, expected, expected)
        );
    }

    @Test
    void retrievesAttributesConcurrently() {
        final Xnav root = new Xnav(
            "<main><a at='a'>1</a><b at='b'>2</b><c at='c'>3</c></main>"
        ).element("main");
        final List<String> res = new Together<>(
            3,
            idx -> {
                final String attribute = "at";
                final String first = root.element("a").attribute(attribute).text().orElseThrow();
                final String second = root.element("b").attribute(attribute).text().orElseThrow();
                final String third = root.element("c").attribute(attribute).text().orElseThrow();
                return String.format("%s %s %s", first, second, third);
            }
        ).asList();
        final String expected = "a b c";
        MatcherAssert.assertThat(
            "We expect the navigator to retrieve attributes concurrently",
            res,
            Matchers.contains(expected, expected, expected)
        );
    }

    @Test
    void retrivesUnexistedAttribute() {
        MatcherAssert.assertThat(
            "We expect the navigator to retrieve unexisted empty attribute",
            new Xnav("<foo></foo>")
                .element("foo")
                .element("bar")
                .element("xyz")
                .attribute("f")
                .text()
                .isPresent(),
            Matchers.is(false)
        );
    }

    @Test
    void retrievesUnexistedElement() {
        MatcherAssert.assertThat(
            "We expect the navigator to retrieve unexisted empty element",
            new Xnav("<foo></foo>")
                .element("foo")
                .element("bar")
                .element("xyz")
                .text()
                .isPresent(),
            Matchers.is(false)
        );
    }

    @Test
    void retrievesStrictNumberOfChildren() {
        MatcherAssert.assertThat(
            "We expect exactly 3 child nodes to be retrieved",
            new Xnav("<strict><a>1</a><b>2</b><c>3</c></strict>")
                .element("strict")
                .strict(3)
                .count(),
            Matchers.equalTo(3L)
        );
    }

    @Test
    void failsWhenChildrenAreLessThanStrictNumber() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new Xnav("<strict><a>1</a><b>2</b></strict>")
                .element("strict")
                .strict(3)
                .collect(Collectors.toList()),
            "We expect an exception when there are fewer children than the strict number"
        );
    }

    @Test
    void failsWhenChildrenAreMoreThanStrictNumber() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new Xnav("<strict><a>1</a><b>2</b><c>3</c><d>4</d></strict>")
                .element("strict")
                .strict(3)
                .collect(Collectors.toList()),
            "We expect an exception when there are more children than the strict number"
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
     *
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
            Arguments.of(xml, "version\n1.2.3"),
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
     *
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
     *
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
