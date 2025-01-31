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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link Xpath}.
 *
 * @since 0.1
 */
final class XpathTest {

    @Test
    void retrievesElement() {
        MatcherAssert.assertThat(
            "We expect to retrieve the child element correctly",
            new Xpath(new DomXml("<animal><cat/></animal>"), "/animal/cat")
                .nodes()
                .findFirst()
                .orElseThrow(),
            Matchers.equalTo(new DomXml("<cat/>").child("cat"))
        );
    }

    @Test
    void retrievesAttribute() {
        MatcherAssert.assertThat(
            "We expect to retrieve the attribute correctly",
            new Xpath(
                new DomXml("<building><room number='100'/></building>"),
                "/building/room/@number"
            ).nodes().findFirst().orElseThrow(),
            Matchers.equalTo(
                new DomXml("<room number='100'/>")
                    .child("room")
                    .attribute("number")
                    .orElseThrow()
            )
        );
    }

    @Test
    void doesNotFindElement() {
        MatcherAssert.assertThat(
            "We expect to not find the element",
            new Xpath(new DomXml("<animal><dog/></animal>"), "/program/objects/o/@base")
                .nodes()
                .findFirst()
                .isEmpty(),
            Matchers.is(true)
        );
    }

    @Test
    void findsSecondElement() {
        MatcherAssert.assertThat(
            "We expect to find the second element",
            new Xpath(
                new DomXml("<zoo><animal>cat</animal><animal>dog</animal></zoo>"),
                "/zoo/animal[2]"
            ).nodes().findFirst().orElseThrow(),
            Matchers.equalTo(new DomXml("<animal>dog</animal>").child("animal"))
        );
    }

    @Test
    void doesNotFindFirstElement() {
        MatcherAssert.assertThat(
            "We expect to not find the second element",
            new Xpath(
                new DomXml("<zoo><animal><cat/></animal><animal><pig/></animal></zoo>"),
                "/zoo/animal[1]/pig"
            ).nodes().findFirst().isPresent(),
            Matchers.is(false)
        );
    }

    @Test
    void doesNotFindThirdElement() {
        MatcherAssert.assertThat(
            "We expect to not find the third element",
            new Xpath(
                new DomXml("<zoo><animal>rabbit</animal><animal>elephant</animal></zoo>"),
                "/zoo/animal[3]"
            ).nodes().collect(Collectors.toList()),
            Matchers.empty()
        );
    }

    @Test
    void retrievesSingleElementWithAttribute() {
        MatcherAssert.assertThat(
            "We expect to retrieve the single element with attribute",
            new Xpath(
                new DomXml("<parking><car/><car number='1'/><car/></parking>"),
                "/parking/car[@number]"
            ).nodes().count(),
            Matchers.equalTo(1L)
        );
    }

    @Test
    void retrievesSecondEelementWithAttribute() {
        MatcherAssert.assertThat(
            "We expect to retrieve the second element with attribute",
            new Xpath(
                new DomXml(
                    "<parking><car>audi</car><car number='1'>bmw</car><car number='1'>kia</car></parking>"
                ),
                "/parking/car[@number][2]"
            ).nodes().findFirst().map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo("kia")
        );
    }

    @Test
    void findsByAttributeEquality() {
        MatcherAssert.assertThat(
            "We expect to find the element by attribute equality expression",
            new Xpath(
                new DomXml(
                    "<parking><car number='1'>audi</car><car number='2'>bmw</car></parking>"
                ),
                "/parking/car[@number='2']"
            ).nodes().findFirst().map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo("bmw")
        );
    }


    @ParameterizedTest
    @MethodSource({"xpaths", "attributeFilters"})
    void checksManyXpaths(final String xpath, final Xml xml, final String expected) {
        MatcherAssert.assertThat(
            "We expect to retrieve the xpath text correctly",
            new Xpath(xml, xpath).nodes().findFirst()
                .map(Xml::text)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(""),
            Matchers.equalTo(expected)
        );
    }

    /**
     * Arguments for the test.
     *
     * @return Arguments for the test.
     */
    private static Object[][] xpaths() {
        final Xml xml = new DomXml(
            "<zoo><animal><cat legs='4'/></animal><animal><dog>4</dog></animal><animal><bird legs='2'>eagle</bird></animal></zoo>"
        );
        return new Object[][]{
            {"/zoo/animal/cat/@legs", xml, "4"},
            {"/zoo/animal/dog", xml, "4"},
            {"/zoo/animal/bird/@legs", xml, "2"},
            {"/zoo/animal[3]/bird", xml, "eagle"},
            {"/zoo/animal/bird", xml, "eagle"},
            {"/zoo/animal[2]/dog", xml, "4"},
            {"/zoo/animal[1]/cat/@legs", xml, "4"},
            {"/zoo/animal[1]/cat", xml, ""},
            {"/zoo/animal[1]/dog", xml, ""},
            {"/zoo/animal[1]/bird", xml, ""},
            {"/zoo/animal[2]/cat", xml, ""},
            {"/zoo/animal[2]/bird", xml, ""},
            {"/zoo/animal[3]/cat", xml, ""},
            {"/zoo/animal[3]/dog", xml, ""},
        };
    }

    /**
     * Arguments for the test.
     *
     * @return Arguments for the test.
     */
    private static Object[][] attributeFilters() {
        final Xml xml = new DomXml(
            String.join(
                "\n",
                "<zoo>",
                "  <animal legs='4'><elephant>big</elephant></animal>",
                "  <animal>bacteria</animal>",
                "  <animal legs='2'><bird>eagle</bird></animal>",
                "</zoo>"
            )
        );
        return new Object[][]{
            {"/zoo/animal[@legs][1]", xml, "big"},
            {"/zoo/animal[@legs][2]", xml, "eagle"},
            {"/zoo/animal[@legs][3]", xml, ""},
            {"/zoo/animal[@legs='4']/elephant", xml, "big"},
            {"/zoo/animal[@legs='2']/bird", xml, "eagle"},
            {"/zoo/animal[@legs='3']/bird", xml, ""},
            {"/zoo/animal[@legs='4']/bird", xml, ""},
            {"/zoo/animal[@legs='0']/bird", xml, ""},
            {"/zoo/animal[@legs='2']/elephant", xml, ""},
            {"/zoo/animal[@legs='3']/elephant", xml, ""},
            {"/zoo/animal[@legs='4']", xml, "<elephant>big</elephant>"},
            {"/zoo/animal[@legs='2']", xml, "<bird>eagle</bird>"},
            {"/zoo/animal[@legs='3']", xml, ""},
        };
    }
}
