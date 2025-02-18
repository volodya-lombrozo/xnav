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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cactoos.experimental.Threads;
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
@SuppressWarnings("PMD.TooManyMethods")
final class XmlTest {

    @ParameterizedTest(name = "{1}")
    @MethodSource("all")
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
    @MethodSource("all")
    void convertsNodeToString(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("Node is not converted to string by %s", label),
            impl.apply("<paragraph><line>text</line></paragraph>")
                .child("paragraph")
                .child("line")
                .toString(),
            Matchers.equalTo("<line>text</line>")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("all")
    void failsToCreateCorruptedDocument(final Function<String, Xml> impl, final String label) {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> impl.apply("<doc...").children(),
            String.format("We expected the corrupted document is not created by %s", label)
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("all")
    void retrievesChildren(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("Children are not retrieved by '%s' implementation", label),
            impl.apply("<doc><char>first</char><char>second</char></doc>")
                .child("doc")
                .children()
                .collect(Collectors.toList()),
            Matchers.hasItems(
                impl.apply("<char>first</char>").child("char"),
                impl.apply("<char>second</char>").child("char")
            )
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("all")
    void retrievesAttribute(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("Attribute is not retrieved by '%s' implementation", label),
            impl.apply("<script><idea author='value'>text</idea></script>")
                .child("script")
                .child("idea")
                .attribute("author")
                .orElseThrow()
                .text()
                .get(),
            Matchers.equalTo("value")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("all")
    void retrivesSameElementTwice(final Function<String, Xml> impl, final String label) {
        final Xml doc = impl.apply("<zoo><cage attribute='value'>monkey</cage></zoo>").child("zoo");
        final String animal = "monkey";
        MatcherAssert.assertThat(
            String.format("We expect to retrieve the same element twice by '%s'", label),
            doc.child(animal),
            Matchers.equalTo(doc.child(animal))
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("all")
    void copiesNode(final Function<String, Xml> impl, final String label) {
        final Xml xml = impl.apply("<doc><node>text</node></doc>");
        MatcherAssert.assertThat(
            String.format("Node is not copied by %s implementation", label),
            xml.copy().toString(),
            Matchers.equalTo(xml.toString())
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("mightBeConvertedToDom")
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
    @MethodSource("all")
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
    @MethodSource("all")
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
            Matchers.equalTo("#document")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("all")
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
    @MethodSource("all")
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
    @MethodSource("all")
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
    @MethodSource("all")
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
    @MethodSource("all")
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
    @MethodSource("all")
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
    @MethodSource("all")
    void retrievesAttributeChildren(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("We expect to find the correct attribute children by '%s'", label),
            impl.apply("<tree honey='no'>fir</tree>")
                .child("tree")
                .attribute("honey")
                .orElseThrow()
                .children()
                .collect(Collectors.toList()),
            Matchers.empty()
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("all")
    void retrievesAttributeNameByXpathInParallel(
        final Function<String, Xml> impl, final String label
    ) {
        final String xml = String.join(
            "\n",
            "<program name=\"j$Collections\">",
            "    <objects>",
            "        <o base=\"jeo.class\">",
            "            <o>",
            "                <o>",
            "                    <o base=\"org.eolang.bytes\"/>",
            "                </o>",
            "            </o>",
            "        </o>",
            "    </objects>",
            "</program>"
        );
        final String path = "/program/objects/o/o/o/o/@base";
        final Xml implementation = impl.apply(xml);
        final List<String> together = new Together<>(
            10,
            input -> {
                final String res = new Xpath(implementation, path).nodes().findFirst()
                    .map(Xml::text)
                    .orElseThrow(
                        () -> new IllegalStateException(
                            String.format("Can't find any nodes by path '%s'", path)
                        )
                    ).get();
                return res;
            }
        ).asList();
        MatcherAssert.assertThat(
            "We expect to find the correct number of results",
            together.size(),
            Matchers.equalTo(10)
        );
        MatcherAssert.assertThat(
            "We expect to find the correct attribute name by xpath",
            together,
            Matchers.hasItem("org.eolang.bytes")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("all")
    void retrievesAttributeFromAttribute(final Function<String, Xml> impl, final String label) {
        MatcherAssert.assertThat(
            String.format("We expect to get empty attribute from attribute by '%s'", label),
            impl.apply("<strange case='yes'>fir</strange>")
                .child("strange")
                .attribute("case")
                .orElseThrow()
                .attribute("unknown"),
            Matchers.equalTo(Optional.empty())
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("all")
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

    @ParameterizedTest
    @MethodSource("all")
    void retrievesAttributeName(final Function<String, Xml> impl, final String label) {
        final String carrot = "carrot";
        MatcherAssert.assertThat(
            String.format("We expect to find the correct attribute name by '%s'", label),
            impl.apply("<o carrot='red'>red</o>")
                .child("o")
                .attribute(carrot)
                .orElseThrow()
                .name(),
            Matchers.equalTo(carrot)
        );
    }

    /**
     * All implementations.
     * @return All XML implementations as arguments.
     */
    private static Stream<Arguments> all() {
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

    /**
     * Might be converted to DOM.
     * All implementations that might be converted to DOM.
     * @return Arguments.
     */
    private static Stream<Arguments> mightBeConvertedToDom() {
        return Stream.of(
            Arguments.of((Function<String, Xml>) VtdXml::new, "vtd-xml"),
            Arguments.of((Function<String, Xml>) DomXml::new, "dom-xml")
        );
    }
}
