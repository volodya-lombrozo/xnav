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
            new Xpath(new Xml("<animal><cat/></animal>"), "/animal/cat")
                .nodes()
                .findFirst()
                .orElseThrow(),
            Matchers.equalTo(new Xml("<cat/>").child("cat"))
        );
    }

    @Test
    void retrievesAttribute() {
        MatcherAssert.assertThat(
            "We expect to retrieve the attribute correctly",
            new Xpath(
                new Xml("<building><room number='100'/></building>"),
                "/building/room/@number"
            ).nodes().findFirst().orElseThrow(),
            Matchers.equalTo(
                new Xml("<room number='100'/>")
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
            new Xpath(new Xml("<animal><cat/></animal>"), "/program/objects/o/@base")
                .nodes()
                .findFirst()
                .isEmpty(),
            Matchers.is(true)
        );
    }

    @ParameterizedTest
    @MethodSource("xpaths")
    void checksManyXpaths(final String xpath, final Xml xml, final String expected) {
        MatcherAssert.assertThat(
            "We expect to retrieve the xpath text correctly",
            new Xpath(xml, xpath).nodes().findFirst().orElseThrow().text().orElseThrow(),
            Matchers.equalTo(expected)
        );
    }

    /**
     * Arguments for the test.
     *
     * @return Arguments for the test.
     */
    private static Object[][] xpaths() {
        final Xml xml = new Xml(
            "<zoo><animal><cat legs='4'/></animal><animal><dog>4</dog></animal><animal><bird legs='2'/></animal></zoo>"
        );
        return new Object[][]{
            {"/zoo/animal/cat/@legs", xml, "4"},
            {"/zoo/animal/dog", xml, "4"},
            {"/zoo/animal/bird/@legs", xml, "2"},
        };
    }
}
