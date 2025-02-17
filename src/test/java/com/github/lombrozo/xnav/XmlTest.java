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
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test cases for different XML implementations.
 *
 * @since 0.1
 */
final class XmlTest {

    private static Stream<Arguments> implementations() {
        return Stream.of(
            Arguments.of((Function<String, Xml>) VtdXml::new, "vtd-xml"),
            Arguments.of((Function<String, Xml>) DomXml::new, "dom-xml"),
            Arguments.of((Function<String, Xml>) ObjectXml::new, "antlr-object-xml"),
            Arguments.of(
                (Function<String, Xml>) xml -> new FlatXml(xml, new FlatDom()),
                "flat-dom-xml"
            ),
            Arguments.of(
                (Function<String, Xml>) xml -> new FlatXml(xml, new FlatAntlr()),
                "flat-antlr-xml"
            )
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void convertsDocumentToString(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("Document is not converted to string by %s", label),
            impl.apply("<doc></doc>").toString(),
            Matchers.anyOf(
                Matchers.equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><doc></doc>"),
                Matchers.equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><doc/>")
            )
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void convertsNodeToString(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("Node is not converted to string by %s", label),
            impl.apply("<doc><node>text</node></doc>")
                .child("doc")
                .child("node")
                .toString(),
            Matchers.equalTo("<node>text</node>")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void failsToCreateCorruptedDocument(final Function<String, Xml> impl, final String label) {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> impl.apply("<doc...").children(),
            String.format("We expected the corrupted document is not created by %s", label)
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrievesChildren(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("Children are not retrieved by '%s' implementation", label),
            impl.apply("<doc><node>first</node><node>second</node></doc>")
                .child("doc")
                .children()
                .collect(Collectors.toList()),
            Matchers.hasItems(
                impl.apply("<node>first</node>").child("node"),
                impl.apply("<node>second</node>").child("node")
            )
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrievesAttribute(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("Attribute is not retrieved by '%s' implementation", label),
            impl.apply("<doc><node attribute='value'>text</node></doc>")
                .child("doc")
                .child("node")
                .attribute("attribute")
                .orElseThrow()
                .text()
                .get(),
            Matchers.equalTo("value")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrivesSameElementTwice(final Function<String, Xml> impl, final String label) {
        final Xml doc = impl.apply("<doc><node attribute='value'>text</node></doc>").child("doc");
        MatcherAssert.assertThat(
            String.format("We expect to retrieve the same element twice by '%s'", label),
            doc.child("node"),
            Matchers.equalTo(doc.child("node"))
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void copiesNode(final Function<String, Xml> impl, final String label) {
        final Xml xml = impl.apply("<doc><node>text</node></doc>");
        MatcherAssert.assertThat(
            String.format("Node is not copied by %s implementation", label),
            xml.copy().toString(),
            Matchers.equalTo(xml.toString())
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrievesNode(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("We expect the node to be retrieved by '%s' implementation", label),
            impl.apply("<doc><node attr='value'>text</node></doc>")
                .child("doc")
                .child("node")
                .node()
                .isEqualNode(
                    new StringNode("<node attr='value'>text</node>").toNode().getFirstChild()
                ),
            Matchers.is(true)
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrievesTextFromSeveralNodes(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("Text is not retrieved from several nodes by '%s' implementation", label),
            impl.apply(
                String.join(
                    "\n",
                    "<doc>",
                    "  <fnode>first </fnode>",
                    "  <snode>second</snode>",
                    "</doc>"
                )
            ).child("doc").text().orElseThrow(),
            Matchers.equalTo("\n  first \n  second\n")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrievesDocName(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format(
                "We expect to find the correct document name by '%s' implementation", label
            ),
            impl.apply(
                String.join(
                    "\n",
                    "<o base='bytes'>",
                    "  <o base='bytes'>2-bytes-</o>",
                    "  <o base='bytes'><o base='bytes'>content</o></o>",
                    "</o>"
                )
            ).name(),
            Matchers.equalTo("o")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrievesChildNames(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            "We expect to find the correct child names",
            impl.apply(
                String.join(
                    "\n",
                    "<o base='child'>",
                    "  <a base='bytes'>3-bytes-</a>",
                    "  <b base='bytes'><c base='bytes'>4</c></b>",
                    "</o>"
                )
            ).child("o").children().map(Xml::name).collect(Collectors.toList()),
            Matchers.hasItems("a", "b")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void printsAllAttributes(final Function<String, Xml> impl, final String label) {
        final String same = "<colors base='bytes' color='red'></colors>";
        MatcherAssert.assertThat(
            String.format("We expect to find all attributes by '%s' implementation", label),
            impl.apply(same).child("colors").toString(),
            Matchers.anyOf(
                Matchers.equalTo(same),
                Matchers.equalTo("<colors base=\"bytes\" color=\"red\"/>")
            )
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrievesObjects(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("Objects are not retrieved by '%s' implementation", label),
            impl.apply(
                String.join(
                    "\n",
                    "<o>",
                    "  <o color='red'>red</o>",
                    "  <o color='blue'>blue</o>",
                    "</o>"
                )
            ).child("o").children().filter(Filter.withName("o")).collect(Collectors.toList()),
            Matchers.hasItems(
                impl.apply("<o color='red'>red</o>").child("o"),
                impl.apply("<o color='blue'>blue</o>").child("o")
            )
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrievesText(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("Retrieved text is not correct by '%s' implementation", label),
            impl.apply("<o><o>yellow</o></o>")
                .child("o")
                .children()
                .flatMap(Xml::children)
                .findFirst()
                .orElseThrow()
                .text()
                .orElseThrow(),
            Matchers.equalTo("yellow")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrievesSeveralChildren(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format(
                "We expect to retrieve exactly two children by '%s' implementation", label
            ),
            impl.apply(
                String.join(
                    "\n",
                    "<all><o color='beautiful'>yellow</o>",
                    "<o color='stylish'>green</o></all>"
                )
            ).child("all").children().flatMap(Xml::children).collect(Collectors.toList()),
            Matchers.hasSize(2)
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrievesTextWithAllSpaces(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            "We expect to find the correct first text from nested XML",
            impl.apply(
                String.join(
                    "\n",
                    "<o>",
                    "  <o><o color='red'>red</o></o>",
                    "  <o color='blue'>blue</o>",
                    "</o>"
                )
            ).text().get(),
            Matchers.equalTo("\n  red\n  blue\n")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("implementations")
    void retrievesChildrenConcurrently(final Function<String, Xml> impl, final String label) {
        final Xml xml = impl.apply(
            String.join(
                "\n",
                "<colors><o color='yellow'>yellow</o>",
                "<o color='green'>green</o></colors>"
            )
        );
        final int threads = 10;
        MatcherAssert.assertThat(
            String.format(
                "We expect to retrieve all children concurrently by '%s' implementation", label
            ),
            new Together<>(
                threads,
                indx -> xml.child("colors")
                    .children()
                    .flatMap(Xml::children)
                    .collect(Collectors.toList())
            ).asList().stream().flatMap(List::stream).collect(Collectors.toList()),
            Matchers.hasSize(threads * 2)
        );
    }

}