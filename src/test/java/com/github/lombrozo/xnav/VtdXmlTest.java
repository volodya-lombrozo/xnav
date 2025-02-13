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
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class VtdXmlTest {

    @Test
    void convertsDocumentToString() {
        MatcherAssert.assertThat(
            "Document is not converted to string",
            new VtdXml("<doc></doc>").toString(),
            Matchers.equalTo(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><doc></doc>"
            )
        );
    }

    @Test
    void convertsNodeToString() {
        MatcherAssert.assertThat(
            "Node is not converted to string",
            new VtdXml("<doc><node>text</node></doc>").child("doc").child("node")
                .toString(),
            Matchers.equalTo("<node>text</node>")
        );
    }

    @Test
    void failsToCreateCorruptedDocument() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new VtdXml("<doc...").children(),
            "Corrupted document is not created, exception is expected"
        );
    }

    @Test
    void retrievesChildren() {
        MatcherAssert.assertThat(
            "Children are not retrieved",
            new VtdXml(
                "<doc><node>first</node><node>second</node></doc>")
                .child("doc")
                .children()
                .collect(Collectors.toList()),
            Matchers.hasItems(
                new VtdXml("<node>first</node>").child("node"),
                new VtdXml("<node>second</node>").child("node")
            )
        );
    }

    @Test
    void retrievesAttribute() {
        MatcherAssert.assertThat(
            "Attribute is not retrieved",
            new VtdXml("<doc><node attribute='value'>text</node></doc>")
                .child("doc")
                .child("node")
                .attribute("attribute")
                .orElseThrow()
                .text()
                .get(),
            Matchers.equalTo("value")
        );
    }

    @Test
    void retrivesSameElementTwice() {
        final Xml doc = new VtdXml("<doc><node attribute='value'>text</node></doc>")
            .child("doc");
        MatcherAssert.assertThat(
            doc.child("node"),
            Matchers.equalTo(doc.child("node"))
        );
    }

    @Test
    void copiesNode() {
        final Xml xml = new VtdXml("<doc><node>text</node></doc>");
        MatcherAssert.assertThat(
            "Node is not copied",
            xml.copy().toString(),
            Matchers.equalTo(xml.toString())
        );
    }

    //todo: fix this test
    @Test
    @Disabled
    void retrievesNode() {
        MatcherAssert.assertThat(
            "We expect the node to be retrieved",
            new VtdXml("<doc><node attr='value'>text</node></doc>")
                .child("doc")
                .child("node")
                .node()
                .isEqualNode(
                    new StringNode("<node attr='value'>text</node>").toNode().getFirstChild()
                ),
            Matchers.is(true)
        );
    }

    @Test
    void retrievesTextFromSeveralNodes() {
        final Xml child = new VtdXml(
            "<doc>",
            "  <node>first </node>",
            "  <node>second</node>",
            "</doc>"
        ).child("doc");
        MatcherAssert.assertThat(
            "Text is not retrieved from several nodes",
            child.text().orElseThrow(),
            Matchers.equalTo("  first   second")
        );
    }

    @Test
    void retrievesDocName() {
        MatcherAssert.assertThat(
            "We expect to find the correct document name",
            new VtdXml(
                "<o base='bytes'>",
                "  <o base='bytes'>2-bytes-</o>",
                "  <o base='bytes'><o base='bytes'>content</o></o>",
                "</o>"
            ).name(),
            Matchers.equalTo("o")
        );
    }

    @Test
    void retrievesChildNames() {
        final Xml child = new VtdXml(
            "<o base='child'>",
            "  <a base='bytes'>3-bytes-</a>",
            "  <b base='bytes'><c base='bytes'>4</c></b>",
            "</o>"
        ).child("o");
        MatcherAssert.assertThat(
            "We expect to find the correct child names",
            child.children().map(Xml::name).collect(Collectors.toList()),
            Matchers.hasItems("a", "b")
        );
    }

    @Test
    void retrievesObjects() {
        MatcherAssert.assertThat(
            "Objects are not retrieved",
            new VtdXml(
                String.join(
                    "\n",
                    "<o>",
                    "  <o color='red'>red</o>",
                    "  <o color='blue'>blue</o>",
                    "</o>"
                )
            ).child("o").children().filter(Filter.withName("o")).collect(Collectors.toList()),
            Matchers.hasItems(
                new VtdXml("<o color='red'>red</o>").child("o"),
                new VtdXml("<o color='blue'>blue</o>").child("o")
            )
        );
    }

    @Test
    void retrievesText() {
        MatcherAssert.assertThat(
            "Retrieved text is not correct",
            new VtdXml("<o><o>yellow</o></o>")
                .child("o")
                .children()
                .flatMap(Xml::children)
                .findFirst()
                .orElseThrow().text().orElseThrow(),
            Matchers.equalTo("yellow")
        );
    }

    @Test
    void retrievesSeveralChildren() {
        MatcherAssert.assertThat(
            "We expect to retrieve exactly two children",
            new VtdXml("<all><o color='yellow'>yellow</o>", "<o color='green'>green</o></all>")
                .child("all")
                .children()
                .flatMap(Xml::children)
                .collect(Collectors.toList()),
            Matchers.hasSize(2)
        );
    }

    @Test
    void retrievesChildrenConcurrently() {
        final Xml xml = new VtdXml(
            "<colors><o color='yellow'>yellow</o>",
            "<o color='green'>green</o></colors>"
        );
        final int threads = 10;
        MatcherAssert.assertThat(
            "We expect to retrieve all children concurrently",
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