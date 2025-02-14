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
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        MatcherAssert.assertThat(
            "Text is not retrieved from several nodes",
            new VtdXml(
                "<doc>",
                "  <fnode>first </fnode>",
                "  <snode>second</snode>",
                "</doc>"
            ).child("doc").text().orElseThrow(),
            Matchers.equalTo("\n  first \n  second\n")
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
    void printsAllAttributes() {
        final String same = "<colors base='bytes' color='red'></colors>";
        MatcherAssert.assertThat(
            "We expect to find all attributes",
            new VtdXml(same).child("colors").toString(),
            Matchers.equalTo(same)
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
        final Stream<Xml> children = new VtdXml(
            "<all><o color='beautiful'>yellow</o>",
            "<o color='stylish'>green</o></all>"
        )
            .child("all")
            .children();
        final List<Xml> first = children.collect(Collectors.toList());
//        final Xml xml = first.get(1);
//        final List<Xml> collect1 = xml.children().collect(Collectors.toList());
        final List<Xml> collect = first.stream()
            .flatMap(Xml::children)
            .collect(Collectors.toList());
        MatcherAssert.assertThat(
            "We expect to retrieve exactly two children",
            collect,
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

    @Test
    public void hugeManyWithVtdXml() {
        Object[][] queries = {
            {"/program/@name", "j$Collections"},
            {"/program/objects/o/@base", "jeo.class"},
            {"/program/objects/o/o/o/o/@base", "org.eolang.bytes"},
        };
        final Xml xml = new VtdXml(XmlBenchmark.generateXml());
        Random random = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            final int request = random.nextInt(queries.length);
            String query = queries[request][0].toString();
            String expected = queries[request][1].toString();
            MatcherAssert.assertThat(
                new Xpath(xml, query)
                    .nodes()
                    .findFirst()
                    .map(Xml::text).get().get().equals(expected),
                Matchers.is(true)
            );
        }
    }
}