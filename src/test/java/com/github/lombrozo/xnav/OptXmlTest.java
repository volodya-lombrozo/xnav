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
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class OptXmlTest {


    @Test
    void convertsDocumentToString() {
        MatcherAssert.assertThat(
            "Document is not converted to string",
            new OptXml("<doc></doc>").toString(),
            Matchers.equalTo(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><doc></doc>"
            )
        );
    }

    @Test
    void convertsNodeToString() {
        MatcherAssert.assertThat(
            "Node is not converted to string",
            new OptXml("<doc><node>text</node></doc>").child("doc").child("node")
                .toString(),
            Matchers.equalTo("<node>text</node>")
        );
    }

    @Test
    void failsToCreateCorruptedDocument() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new OptXml("<doc...").children(),
            "Corrupted document is not created, exception is expected"
        );
    }

    @Test
    void retrievesChildren() {
        MatcherAssert.assertThat(
            "Children are not retrieved",
            new OptXml(
                "<doc><node>first</node><node>second</node></doc>")
                .child("doc")
                .children()
                .collect(Collectors.toList()),
            Matchers.contains(
                new OptXml("<node>first</node>").child("node"),
                new OptXml("<node>second</node>").child("node")
            )
        );
    }

    @Test
    void retrievesText() {
        MatcherAssert.assertThat(
            "Text is not retrieved",
            new OptXml("<doc><node>text</node></doc>")
                .child("doc")
                .child("node")
                .text()
                .orElseThrow(),
            Matchers.equalTo("text")
        );
    }

    @Test
    void retrievesAttribute() {
        MatcherAssert.assertThat(
            "Attribute is not retrieved",
            new OptXml("<doc><node attribute='value'>text</node></doc>")
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
    void copiesNode() {
        final Xml xml = new OptXml("<doc><node>text</node></doc>");
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
            new OptXml("<doc><node attr='value'>text</node></doc>")
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
            new OptXml(
                "<doc>",
                "  <node>first </node>",
                "  <node>second</node>",
                "</doc>"
            ).child("doc").text().orElseThrow(),
            Matchers.equalTo("  first   second")
        );
    }

    @Test
    void retrievesDocName() {
        MatcherAssert.assertThat(
            "We expect to find the correct document name",
            new OptXml(
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
        MatcherAssert.assertThat(
            "We expect to find the correct child names",
            new OptXml(
                "<o base='child'>",
                "  <o base='bytes'>3-bytes-</o>",
                "  <o base='bytes'><o base='bytes'>4</o></o>",
                "</o>"
            ).child("o").children().map(Xml::name).collect(Collectors.toList()),
            Matchers.hasItems("o", "o")
        );
    }

    @Test
    void retrievesObjects() {
        MatcherAssert.assertThat(
            "Objects are not retrieved",
            new OptXml(
                String.join(
                    "\n",
                    "<o>",
                    "  <o color='red'>red</o>",
                    "  <o color='blue'>blue</o>",
                    "</o>"
                )
            ).child("o").children().filter(Filter.withName("o")).collect(Collectors.toList()),
            Matchers.hasItems(
                new OptXml("<o color='red'>red</o>").child("o"),
                new OptXml("<o color='blue'>blue</o>").child("o")
            )
        );
    }

    //todo: remove me
    @Test
    void hugeManyWithLazyXml() {
        Object[][] queries = {
            {"/program/@name", "j$Collections"},
            {"/program/objects/o/@base", "jeo.class"},
            {"/program/objects/o/o/o/o/@base", "org.eolang.bytes"},
        };
        Random random = new SecureRandom();
        final String large = XmlBenchmark.generateXml();
        for (int j = 0; j < 100; j++) {
            final Xml xml = new OptXml(large);
            for (int i = 0; i < 100_000; i++) {
                final int request = random.nextInt(queries.length);
                final String query = queries[request][0].toString();
                final String expected = queries[request][1].toString();
//                new Xpath(xml, query)
//                    .nodes()
//                    .findFirst()
//                    .map(Xml::text).get().get().equals(expected);
            }

        }
    }


    @Test
    void retrievesChildrenConcurrently() {
        final Xml xml = new OptXml(
            String.join(
                "",
                "<ob><o color='yellow'>yellow</o>",
                "<o color='green'>green</o></ob>"
            )
        );
        final int threads = 10;
        final Together<List<Xml>> all = new Together<>(
            threads,
            indx -> {
                return xml.child("ob").children()
                    .flatMap(Xml::children)
                    .collect(Collectors.toList());
            }
        );
        MatcherAssert.assertThat(
            "Children are not retrieved concurrently",
            all.asList().stream().flatMap(List::stream).collect(Collectors.toList()),
            Matchers.hasSize(threads * 2)
        );
    }

}