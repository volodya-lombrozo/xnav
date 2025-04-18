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
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link Xpath}.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
final class XpathTest {

    /**
     * Root start tag.
     */
    private static final String ROOT_START = "<root>";

    /**
     * Root end tag.
     */
    private static final String ROOT_END = "</root>";

    @Test
    void retrievesElement() {
        MatcherAssert.assertThat(
            "We expect to retrieve the child element correctly",
            new Xpath(XpathTest.xml("<animal><cat/></animal>"), "/animal/cat")
                .nodes()
                .findFirst()
                .orElseThrow(),
            Matchers.equalTo(XpathTest.xml("<cat/>").child("cat"))
        );
    }

    @Test
    void findsElementWithAndOperator() {
        final Xml xml = XpathTest.xml(
            XpathTest.ROOT_START,
            "  <o name='foo'>",
            "    <o name='at'>",
            "      <o name='λ'/>",
            "    </o>",
            "  </o>",
            XpathTest.ROOT_END
        );
        MatcherAssert.assertThat(
            "We expect to find the element using the 'and' operator",
            new Xpath(xml, "//o[@name='at' and o[@name='λ']]")
                .nodes()
                .findFirst()
                .isPresent(),
            Matchers.is(true)
        );
    }

    @Test
    void retrievesAttribute() {
        MatcherAssert.assertThat(
            "We expect to retrieve the attribute correctly",
            new Xpath(
                XpathTest.xml("<building><room number='100'/></building>"),
                "/building/room/@number"
            ).nodes().findFirst().orElseThrow(),
            Matchers.equalTo(
                XpathTest.xml("<room number='100'/>")
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
            new Xpath(XpathTest.xml("<animal><dog/></animal>"), "/program/objects/o/@base")
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
                XpathTest.xml("<zoo><animal>cat</animal><animal>dog</animal></zoo>"),
                "/zoo/animal[2]"
            ).nodes().findFirst().orElseThrow(),
            Matchers.equalTo(XpathTest.xml("<animal>dog</animal>").child("animal"))
        );
    }

    @Test
    void doesNotFindFirstElement() {
        MatcherAssert.assertThat(
            "We expect to not find the second element",
            new Xpath(
                XpathTest.xml("<zoo><animal><cat/></animal><animal><pig/></animal></zoo>"),
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
                XpathTest.xml("<zoo><animal>rabbit</animal><animal>elephant</animal></zoo>"),
                "/zoo/animal[3]"
            ).nodes().collect(Collectors.toList()),
            Matchers.empty()
        );
    }

    @Test
    void findsHex() {
        MatcherAssert.assertThat(
            "We expect to find the hex value",
            new Xpath(
                XpathTest.xml(
                    "<p>",
                    "   <o base='Q.org.eolang.number'>",
                    "      <o base='Q.org.eolang.bytes' hex=''>1</o>",
                    "   </o>",
                    "</p>"
                ),
                "//o[@hex]"
            ).nodes().findFirst().isPresent(),
            Matchers.is(true)
        );
    }

    @Test
    void retrievesAttributeValuesCorrectly() {
        final Xml xml = XpathTest.xml(
            XpathTest.ROOT_START,
            "  <item name='apple'>Fruit</item>",
            "  <item name='application'>Software</item>",
            XpathTest.ROOT_END
        );
        MatcherAssert.assertThat(
            "First 'item' name should be 'apple'",
            new Xpath(xml, "/root/item[1]/@name")
                .nodes()
                .findFirst()
                .orElseThrow()
                .text()
                .orElseThrow(),
            Matchers.equalTo("apple")
        );
        MatcherAssert.assertThat(
            "Second 'item' name should be 'application'",
            new Xpath(xml, "/root/item[2]/@name")
                .nodes()
                .findFirst()
                .orElseThrow()
                .text()
                .orElseThrow(),
            Matchers.equalTo("application")
        );
    }

    @Test
    void retrievesSingleElementWithAttribute() {
        MatcherAssert.assertThat(
            "We expect to retrieve the single element with attribute",
            new Xpath(
                XpathTest.xml("<parking><car/><car number='1'/><car/></parking>"),
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
                XpathTest.xml(
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
                XpathTest.xml(
                    "<parking><car number='1'>audi</car><car number='2'>bmw</car></parking>"
                ),
                "/parking/car[@number='2']"
            ).nodes().findFirst().map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo("bmw")
        );
    }

    @Test
    void findsByAndOperator() {
        MatcherAssert.assertThat(
            "We expect to find the element by AND operator",
            new Xpath(
                XpathTest.xml(
                    "<parking><car number='1'>audi</car><car number='1' wheels='4'>bmw</car></parking>"
                ),
                "/parking/car[@number='1' and @wheels]"
            ).nodes().findFirst().map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo("bmw")
        );
    }

    @Test
    void findsByText() {
        MatcherAssert.assertThat(
            "We expect to find the element by text",
            new Xpath(
                XpathTest.xml(
                    "<films><film>matrix</film><film>inception</film><film>interstellar</film></films>"
                ),
                "/films/film[text()='inception']"
            ).nodes().findFirst().map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo("inception")
        );
    }

    @Test
    void findsByInversion() {
        MatcherAssert.assertThat(
            "We expect to find the element by inversion",
            new Xpath(
                XpathTest.xml(
                    "<films><film>inception</film><film>interstellar</film></films>"
                ),
                "/films/film[not(text()='inception')]"
            ).nodes().findFirst().map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo("interstellar")
        );
    }

    @Test
    void findsByStringLength() {
        MatcherAssert.assertThat(
            "We expect to find the element where string-length is greater than 3",
            new Xpath(
                XpathTest.xml(
                    "<words><word>abc</word><word>hello</word><word>xy</word></words>"
                ),
                "/words/word[string-length(text()) > 3]"
            ).nodes().findFirst().map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo("hello")
        );
    }

    @Test
    void findsByExactStringLength() {
        final Optional<Xml> first = new Xpath(
            XpathTest.xml(
                "<words><word>hello</word><word>abc</word><word>12345</word></words>"
            ),
            "/words/word[string-length(text()) = 5]"
        ).nodes().findFirst();
        MatcherAssert.assertThat(
            "We expect to find the element where string-length is exactly 5",
            first.map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo("hello")
        );
    }

    @Test
    void findsByNormalizeSpace() {
        MatcherAssert.assertThat(
            "We expect to find the element after removing leading and trailing spaces",
            new Xpath(
                XpathTest.xml(
                    "<messages><message> hel   lo </message><message>world</message><message>   trimmed   </message></messages>"
                ),
                "/messages/message[normalize-space(text()) = 'hel lo']"
            ).nodes().findFirst().map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo(" hel   lo ")
        );
    }

    @Test
    void findsByNormalizeSpaceAndStringLength() {
        MatcherAssert.assertThat(
            "We expect to find elements with non-empty normalized text",
            new Xpath(
                XpathTest.xml(
                    "<messages><message> </message><message> nonempty </message><message>   </message></messages>"
                ),
                "/messages/message[string-length(normalize-space(text())) > 0]"
            ).nodes().findFirst().map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo(" nonempty ")
        );
    }

    @Test
    void findsByParenthesesWithOrOperator() {
        MatcherAssert.assertThat(
            "We expect to find the correct element using parentheses with OR operator",
            new Xpath(
                XpathTest.xml(
                    "<values><val type='X'>apple</val><val type='Y'>banana</val><val type='X'>cherry</val></values>"
                ),
                "/values/val[(text()='apple' or text()='banana') and @type='X']"
            ).nodes().findFirst().map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo("apple")
        );
    }

    @Test
    void retrievesRecursiveElement() {
        final Xml xml = XpathTest.xml(
            "<root><level1><level2><target/></level2></level1></root>"
        );
        MatcherAssert.assertThat(
            "We expect to retrieve the element recursively",
            new Xpath(xml, "//target")
                .nodes()
                .findFirst()
                .orElseThrow(),
            Matchers.equalTo(XpathTest.xml("<target/>").child("target"))
        );
    }

    @Test
    void selectsTopFirst() {
        final List<Xnav> all = new Xnav(
            XpathTest.xml(
                "<o base='bytes'>",
                "  <o base='bytes'>2-bytes-</o>",
                "  <o base='bytes'><o base='bytes'>content</o></o>",
                "</o>"
            )
        ).path("//o[@base='bytes']").collect(Collectors.toList());
        MatcherAssert.assertThat(
            "We expect to find the correct number of elements",
            all,
            Matchers.hasSize(4)
        );
        MatcherAssert.assertThat(
            "We expect to find the correct first text",
            all.get(0).text().get(),
            Matchers.equalTo("\n  2-bytes-\n  content\n")
        );
    }

    @Test
    void findsCorrectTextFromNestedPredicate() {
        final String xml = String.join(
            "\n",
            "<o>",
            "  <o><o color='red'>red</o></o>",
            "  <o color='blue'>blue</o>\n",
            "</o>"
        );
        MatcherAssert.assertThat(
            "We expect to find the correct first text from nested XML",
            new Xpath(XpathTest.xml(xml), "//o[o[@color]]").nodes()
                .findFirst()
                .map(Xml::text)
                .orElseThrow()
                .orElseThrow(),
            Matchers.equalTo("\n  red\n  blue\n\n")
        );
    }

    @Test
    void findsCorrectTextFromSteppedXpath() {
        final List<Xml> collect = new Xpath(
            XpathTest.xml(
                "<o>",
                "  <o><o color='red'>red</o></o>",
                "  <o color='blue'>blue</o>\n",
                "</o>"
            ),
            "//o/o[@color]"
        ).nodes().collect(Collectors.toList());
        MatcherAssert.assertThat(
            "We expect to find the correct first text from nested XML",
            collect.stream()
                .findFirst()
                .map(Xml::text)
                .orElseThrow()
                .orElseThrow(),
            Matchers.equalTo("red")
        );
    }

    @Test
    void findsByElementTextEquality() {
        MatcherAssert.assertThat(
            "We expect to find the element by text equality in child element",
            new Xpath(
                XpathTest.xml(
                    "<program><metas><meta><head>alias</head><tail>1.2.3</tail></meta></metas></program>"
                ),
                "/program/metas/meta[head='alias']"
            ).nodes().findFirst().map(Xml::text).orElseThrow().orElseThrow(),
            Matchers.equalTo("alias1.2.3")
        );
    }

    @Test
    void doesNotFindByElementTextEquality() {
        MatcherAssert.assertThat(
            "We expect to not find the element by text equality in child element",
            new Xpath(
                XpathTest.xml(
                    "<program><metas><meta><head>version</head><tail>1.2.3</tail></meta></metas></program>"
                ),
                "/program/metas/meta[tail='3.2.1']"
            ).nodes().findFirst().isPresent(),
            Matchers.is(false)
        );
    }

    @Test
    void findsFqn() {
        final Xnav xml = new Xnav(
            "<something>",
            "  <o fqn='Class1'/>",
            "  <o fqn='Class2'/>",
            "  <o fqn='Class3'/>",
            "</something>"
        );
        MatcherAssert.assertThat(
            "We expect to retrieve the fqn correctly",
            xml.path("//o[@fqn]")
                .map(o -> o.attribute("fqn").text().orElseThrow())
                .collect(Collectors.toList()),
            Matchers.containsInAnyOrder("Class1", "Class2", "Class3")
        );
    }

    @Test
    void findsFqnsConcurrently() {
        final Xnav xml = new Xnav(
            "<program>",
            "  <metas>",
            "    <meta>",
            "      <head>package</head>",
            "      <tail>com.example</tail>",
            "    </meta>",
            "  </metas>",
            "  <o fqn='Class4'/>",
            "  <o fqn='Class5'/>",
            "  <o fqn='Class6'/>",
            "</program>"
        );
        MatcherAssert.assertThat(
            "We expect the fqns method to work correctly in a multi-threaded environment",
            new Together<>(
                10,
                idx -> xml.path("//o[@fqn]")
                    .map(o -> o.attribute("fqn").text().orElseThrow())
                    .collect(Collectors.toList())
            ).asList().stream().flatMap(List::stream).collect(Collectors.toList()),
            Matchers.hasItems("Class4", "Class5", "Class6")
        );
    }

    @Test
    void throwsExceptionWhenUnexpectedOperator() {
        final String invalid = "/values/val[@active='true' && @type='A']";
        final Xml xml = XpathTest.xml(
            "<values>",
            "  <val type='A' active='true'>one</val>",
            "  <val type='B' active='false'>two</val>",
            "</values>"
        );
        MatcherAssert.assertThat(
            "We expect a human-readable error message",
            Assertions.assertThrows(
                IllegalStateException.class,
                () -> new Xpath(xml, invalid).nodes().findFirst().orElseThrow(),
                "We expect an exception when an unexpected operator is used"
            ).getMessage(),
            Matchers.containsString("Expected ']', but got '@' in position: 30")
        );
    }

    @Test
    void throwsExceptionWithCompletelyInvalidToken() {
        final String invalid = "/lib/novel[title='1984' @author]";
        final Xml xml = XpathTest.xml(
            "<lib>",
            "  <novel genre='fiction'>",
            "    <title>1984</title>",
            "    <author>George Orwell</author>",
            "  </novel>",
            "</lib>"
        );
        MatcherAssert.assertThat(
            "We expect human-readable error message",
            Assertions.assertThrows(
                IllegalStateException.class,
                () -> new Xpath(xml, invalid).nodes().findFirst().orElseThrow(),
                "We expect an exception when an invalid token is used"
            ).getMessage(),
            Matchers.containsString("Expected ']', but got '@' in position: 24")
        );
    }

    @ParameterizedTest
    @MethodSource({
        "xpaths",
        "attributeFilters",
        "binaryOperators",
        "inversion",
        "stringLength",
        "normalizeSpace",
        "parentheses",
        "predicatesOverResults",
        "recursivePaths",
        "subpathExpressions",
        "textEquality",
        "complexXpaths",
        "startsWithTests",
        "complexStartsWithTests"
    })
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
     * Arguments for the starts-with() function tests.
     *
     * @return Arguments for the test.
     */
    private static Object[][] xpaths() {
        final Xml xml = XpathTest.xml(
            "<zoo><animal><cat legs='4'/></animal><animal><dog>4</dog></animal><animal><bird legs='2'>eagle</bird></animal></zoo>"
        );
        final String eagle = "eagle";
        return new Object[][]{
            {"/zoo/animal/cat/@legs", xml, "4"},
            {"/zoo/animal/dog", xml, "4"},
            {"/zoo/animal/bird/@legs", xml, "2"},
            {"/zoo/animal[3]/bird", xml, eagle},
            {"/zoo/animal/bird", xml, eagle},
            {"/zoo/animal[2]/dog", xml, "4"},
            {"/zoo/animal[1]/cat/@legs", xml, "4"},
            {"/zoo/animal[1]/cat", xml, ""},
            {"/zoo/animal[1]/dog", xml, ""},
            {"/zoo/animal[1]/bird", xml, ""},
            {"/zoo/animal[2]/bird", xml, ""},
            {"/zoo/animal[2]/cat", xml, ""},
            {"/zoo/animal[3]/cat", xml, ""},
            {"/zoo/animal[3]/dog", xml, ""},
        };
    }

    /**
     * Arguments for the starts-with() function tests.
     *
     * @return Arguments for the test.
     */
    private static Object[][] startsWithTests() {
        final Xml fruits = xml(
            XpathTest.ROOT_START,
            "  <item name='apple'>Fruit</item>",
            "  <item name='apricot'>Fruit</item>",
            "  <item name='banana'>Fruit</item>",
            "  <item name='application'>Software</item>",
            XpathTest.ROOT_END
        );
        final Xml products = xml(
            "<catalog>",
            "  <product code='ABC123'>Laptop</product>",
            "  <product code='ABD456'>Smartphone</product>",
            "  <product code='XYZ789'>Tablet</product>",
            "  <product code='ABF000'>Monitor</product>",
            "</catalog>"
        );
        final Xml employees = xml(
            "<employees>",
            "  <employee id='E001'>Alice</employee>",
            "  <employee id='E002'>Bob</employee>",
            "  <employee id='A003'>Charlie</employee>",
            "  <employee id='A004'>David</employee>",
            "</employees>"
        );
        final String apple = "apple";
        final String app = "application";
        return new Object[][]{
            {"/root/item[starts-with(@name, 'app')]/@name", fruits, apple},
            {"/root/item[starts-with(@name, 'apr')]/@name", fruits, "apricot"},
            {"/root/item[starts-with(@name, 'ban')]/@name", fruits, "banana"},
            {"/root/item[starts-with(@name, 'app') and text()='Fruit']/@name", fruits, apple},
            {"/root/item[starts-with(@name, 'app') and text()='Software']/@name", fruits, app},
            {"/catalog/product[starts-with(@code, 'AB')]", products, "Laptop"},
            {"/catalog/product[starts-with(@code, 'ab')]/text()", products, ""},
            {"/employees/employee[starts-with(@id, 'A')]", employees, "Charlie"},
            {"/employees/employee[starts-with(@id, 'A')][2]", employees, "David"},
            {"/root/item[starts-with(@name, 'xyz')]", fruits, ""},
            {"/catalog/product[starts-with(@code, 'DEF')]", products, ""},
            {"/employees/employee[starts-with(@id, 'Z')]/text()", employees, ""},
            {"/root/item[starts-with(@name, '')]/@name", fruits, apple},
            {"/catalog/product[starts-with(@code, '')]/@code", products, "ABC123"},
            {"/employees/employee[starts-with(@id, '')]/@id", employees, "E001"},
            {"/root/item[starts-with(@name, 'app')]", fruits, "Fruit"},
            {"/employees/employee[starts-with(@id, 'E')]", employees, "Alice"},
        };
    }

    /**
     * Arguments for the complex starts-with() function tests.
     *
     * @return Arguments for the test.
     */
    private static Object[][] complexStartsWithTests() {
        final Xml xml = xml(
            "<program>",
            "  <metas>",
            "    <meta>",
            "      <head>unlint</head>",
            "      <tail>lname</tail>",
            "    </meta>",
            "    <meta>",
            "      <head>unlint</head>",
            "      <tail>lname:extra</tail>",
            "    </meta>",
            "    <meta>",
            "      <head>other</head>",
            "      <tail>something</tail>",
            "    </meta>",
            "  </metas>",
            "</program>"
        );
        return new Object[][]{
            {
                "/program/metas/meta[head='unlint' and (tail='lname' or starts-with(tail, 'lname:'))]/tail[2]",
                xml,
                "lname:extra",
            },
            {
                "/program/metas/meta[starts-with(tail, 'ln') and head='unlint']/head",
                xml,
                "unlint",
            },
            {
                "/program/metas/meta[starts-with(tail, 'lm') or starts-with(tail, 'ln')]/tail[2]",
                xml,
                "lname:extra",
            },
            {
                "/program/metas/meta[head='unlint' and (starts-with(tail, 'lname') or starts-with(tail, 'other'))]/tail[1]",
                xml,
                "lname",
            },
            {
                "/program/metas/meta[starts-with(head, 'un') and (starts-with(tail, 'lname') or starts-with(tail, 'something'))]/head[2]",
                xml,
                "unlint",
            },
            {
                "/program/metas/meta[head='unlint' and (tail='unknown' or starts-with(tail, 'unknown:'))]/tail",
                xml,
                "",
            },
        };
    }

    /**
     * Arguments for the test.
     *
     * @return Arguments for the test.
     */
    private static Object[][] attributeFilters() {
        final Xml xml = XpathTest.xml(
            "<zoo>",
            "  <animal legs='4'><elephant>big</elephant></animal>",
            "  <animal>bacteria</animal>",
            "  <animal legs='2'><bird>eagle</bird></animal>",
            "</zoo>"
        );
        final String eagle = "eagle";
        return new Object[][]{
            {"/zoo/animal[@legs][1]", xml, "big"},
            {"/zoo/animal[@legs='2']", xml, eagle},
            {"/zoo/animal[@legs='3']", xml, ""},
            {"/zoo/animal[@legs='4']/elephant", xml, "big"},
            {"/zoo/animal[@legs='2']/bird", xml, eagle},
            {"/zoo/animal[@legs='3']/bird", xml, ""},
            {"/zoo/animal[@legs='4']/bird", xml, ""},
            {"/zoo/animal[@legs='0']/bird", xml, ""},
            {"/zoo/animal[@legs='2']/elephant", xml, ""},
            {"/zoo/animal[@legs='3']/elephant", xml, ""},
            {"/zoo/animal[@legs='4']", xml, "big"},
            {"/zoo/animal[@legs][2]", xml, eagle},
            {"/zoo/animal[@legs][3]", xml, ""},
        };
    }

    /**
     * Arguments for the test.
     *
     * @return Arguments for the test.
     */
    private static Object[][] binaryOperators() {
        final Xml xml = XpathTest.xml(
            "<building>",
            "  <room height='400' width='600'>canteen</room>",
            "  <room height='300' width='700'>bedroom</room>",
            "  <room height='200' width='800'>pantry</room>",
            "</building>"
        );
        final String canteen = "canteen";
        final String bedroom = "bedroom";
        final String pantry = "pantry";
        return new Object[][]{
            {"/building/room[@height='400' and @width='600']", xml, canteen},
            {"/building/room[@height='300' and @width='700']", xml, bedroom},
            {"/building/room[@height='200' and @width='800']", xml, pantry},
            {"/building/room[@height='300' and @width='600']", xml, ""},
            {"/building/room[@height='400' and @width='700']", xml, ""},
            {"/building/room[@height='200' and @width]", xml, pantry},
            {"/building/room[@height='400' or @width='600']", xml, canteen},
            {"/building/room[@height='300' or @width='700']", xml, bedroom},
            {"/building/room[@height='200' or @width='800']", xml, pantry},
            {"/building/room[@height='300' or @width='600']", xml, canteen},
            {"/building/room[@height='400' or @width='700']", xml, canteen},
            {"/building/room[@height='200' or @width]", xml, canteen},
            {"/building/room[@height='200' or @length]", xml, pantry},
            {"/building/room[@height='400' and text()='canteen']", xml, canteen},
            {"/building/room[@height='300' and text()='bedroom']", xml, bedroom},
            {"/building/room[@height='200' and text()='pantry']", xml, pantry},
            {"/building/room[@height='400' and text()='bedroom']", xml, ""},
            {"/building/room[@height='300' and text()='canteen']", xml, ""},
            {"/building/room[@height='200' and text()='canteen']", xml, ""},
            {"/building/room[@height='400' and text()='pantry']", xml, ""},
            {"/building/room[@height='300' and text()='pantry']", xml, ""},
            {"/building/room[@height='200' and text()='bedroom']", xml, ""},
            {"/building/room[@height='500' or text()='canteen']", xml, canteen},
            {"/building/room[@height='300' or text()='balcony']", xml, bedroom},
            {"/building/room[@height='200' or text()='pantry']", xml, pantry},
            {"/building/room[@height='100' or text()='balcony']", xml, ""},
        };
    }

    /**
     * Arguments for the test.
     *
     * @return Arguments for the test.
     */
    private static Object[][] inversion() {
        final Xml xml = XpathTest.xml(
            "<school>",
            "  <class people='23'>A</class>",
            "  <class people='30'>B</class>",
            "  <class people='25' ill='1'>C</class>",
            "</school>"
        );
        return new Object[][]{
            {"/school/class[not(@ill)]", xml, "A"},
            {"/school/class[not(@people)]", xml, ""},
            {"/school/class[not(@ill='1')]", xml, "A"},
            {"/school/class[not(text()='A' or text()='B')]", xml, "C"},
        };
    }

    /**
     * Arguments for string-length() tests.
     *
     * @return Arguments for the test.
     */
    private static Object[][] stringLength() {
        final Xml xml = XpathTest.xml(
            "<words>  <word>hi</word>  <word>hello</word>  <word>bye</word>  <word>greetings</word></words>"
        );
        return new Object[][]{
            {"/words/word[string-length(text()) > 3]", xml, "hello"},
            {"/words/word[string-length(text()) = 2]", xml, "hi"},
            {"/words/word[string-length(text()) < 4]", xml, "hi"},
            {"/words/word[string-length(text()) = 9]", xml, "greetings"},
            {"/words/word[string-length(text()) > 10]", xml, ""},
        };
    }

    /**
     * Arguments for normalize-space() tests.
     *
     * @return Arguments for the test.
     */
    private static Object[][] normalizeSpace() {
        final Xml xml = XpathTest.xml(
            "<messages>",
            "  <message> hello </message>",
            "  <message>  world   </message>",
            "  <message> </message>",
            "  <message>   trimmed   </message>",
            "</messages>"
        );
        return new Object[][]{
            {"/messages/message[normalize-space(text()) = 'hello']", xml, " hello "},
            {"/messages/message[normalize-space(text()) = 'world']", xml, "  world   "},
            {"/messages/message[normalize-space(text()) = '']", xml, " "},
            {"/messages/message[string-length(normalize-space(text())) > 0]", xml, " hello "},
        };
    }

    /**
     * Arguments for parentheses () precedence tests.
     *
     * @return Arguments for the test.
     */
    private static Object[][] parentheses() {
        final Xml xml = XpathTest.xml(
            "<values>",
            "  <val type='A' active='true'>one</val>",
            "  <val type='B' active='false'>two</val>",
            "  <val type='A' active='false'>three</val>",
            "  <val type='C' active='true'>four</val>",
            "  <val type='C' active='false'>five</val>",
            "</values>"
        );
        final String one = "one";
        return new Object[][]{
            {"/values/val[(string-length(text()) > 3) and @active='false']", xml, "three"},
            {"/values/val[(@active='true') and (@type='A')]", xml, one},
            {"/values/val[(text()='one' or text()='three') and @type='A']", xml, one},
            {"/values/val[(text()='one' or text()='three') and @type='B']", xml, ""},
            {"/values/val[(text()='four' or text()='five') and @type='C']", xml, "four"},
            {"/values/val[(text()='four' or text()='five') and @active='false']", xml, "five"},
            {"/values/val[(text()='two' or text()='three') and @active='false']", xml, "two"},
            {"/values/val[(text()='one' or text()='four') and @active='true']", xml, one},
            {"/values/val[(@active='false') and (text()='four' or text()='five')]", xml, "five"},
            {"/values/val[@active='false' and (text()='two' or text()='three')]", xml, "two"},
            {"/values/val[(@active='true') and (text()='one' or text()='four')]", xml, one},
        };
    }

    /**
     * Arguments for predicates [].
     *
     * @return Arguments for the test.
     */
    private static Object[][] predicatesOverResults() {
        final Xml xml = XpathTest.xml(
            "<locations>",
            "  <o name='one' atom='true' loc='here'>first</o>",
            "  <o name='two' atom='true' base='yes' loc='there'>two</o>",
            "  <o name='three' atom='true' loc='everywhere' lambda='no'>three</o>",
            "  <o name='four' atom='true' loc='nowhere'>four</o>",
            "</locations>"
        );
        return new Object[][]{
            {
                "(/locations/o[@name and @atom and not(@base) and @loc and not(@lambda)])[1]",
                xml,
                "first",
            },
            {
                "(/locations/o[@name and @atom and not(@base) and @loc and not(@lambda)])[2]",
                xml,
                "four",
            },
            {
                "(/locations/o[@name and @atom and not(@base) and @loc and not(@lambda)])[3]",
                xml,
                "",
            },
        };
    }

    /**
     * Arguments for the test with recursive paths.
     *
     * @return Arguments for the test.
     */
    private static Object[][] recursivePaths() {
        final Xml xml = XpathTest.xml(
            "<languages>",
            "  <language name='Java' type='OOP'>",
            "    <feature>Cross-platform</feature>",
            "    <feature>Robust</feature>",
            "  </language>",
            "  <language name='Python' type='Scripting'>",
            "    <feature>Easy to learn</feature>",
            "    <feature>Versatile</feature>",
            "  </language>",
            "  <language name='C++' type='OOP'>",
            "    <feature>Performance</feature>",
            "    <feature>Complex</feature>",
            "  </language>",
            "</languages>"
        );
        return new Object[][]{
            {"//feature[text()='Cross-platform']", xml, "Cross-platform"},
            {"//language[@name='Python']//feature[2]", xml, "Versatile"},
            {"//language[@type='OOP']//feature[text()='Performance']", xml, "Performance"},
            {"//language[@type='Scripting']//feature[1]", xml, "Easy to learn"},
            {"//language[@name='Java']//feature[1]", xml, "Cross-platform"},
            {"//language[@name='C++']//feature[2]", xml, "Complex"},
            {"//language[@name='Python']//feature[text()='Versatile']", xml, "Versatile"},
            {"//language[@name='Java' and @type='OOP']//feature[2]", xml, "Robust"},
            {"//language[@name='C++' and @type='OOP']//feature[1]", xml, "Performance"},
            {"//language[@name='Python' and not(@type='OOP')]//feature[1]", xml, "Easy to learn"},
        };
    }

    /**
     * Arguments for subpath tests.
     *
     * @return Arguments for the test.
     */
    private static Object[][] subpathExpressions() {
        final Xml xml = XpathTest.xml(
            XpathTest.ROOT_START,
            "  <o><o base='true'><o>basetrue</o></o></o>",
            "  <o base='false'><o>other</o></o>",
            "  <o><o base='true'>nested</o></o>",
            "  <o><o base='false'><o>other</o></o></o>",
            XpathTest.ROOT_END
        );
        final String content = "basetrue";
        final String other = "other";
        return new Object[][]{
            {"//o[o[@base='true']]", xml, content},
            {"//o[@base='false']]", xml, other},
            {"//o[o[@base='false']]", xml, other},
            {"//o[o[@base='true'] and text()='nested']", xml, "nested"},
            {"//o[o[@base='true'] and not(text()='nested')]", xml, content},
            {"//o[o[@base='false'] and text()='other']", xml, other},
        };
    }

    /**
     * Arguments for subpath tests.
     *
     * @return Arguments for the test.
     */
    private static Object[][] textEquality() {
        final Xml xml = XpathTest.xml(
            "<library>",
            "  <book genre='fiction'>",
            "    <title>The Great Gatsby</title>",
            "    <author>F. Scott Fitzgerald</author>",
            "  </book>",
            "  <book genre='non-fiction'>",
            "    <title>Sapiens</title>",
            "    <author>Yuval Noah Harari</author>",
            "  </book>",
            "  <book genre='fiction'>",
            "    <title>1984</title>",
            "    <author>George Orwell</author>",
            "  </book>",
            "  <book genre='fiction'>",
            "    <title>To Kill a Mockingbird</title>",
            "    <author>Harper Lee</author>",
            "  </book>",
            "</library>"
        );
        final String orwell = "George Orwell";
        final String fitzgerald = "F. Scott Fitzgerald";
        final String mockingbid = "To Kill a Mockingbird";
        final String sapiens = "Sapiens";
        final String harari = "Yuval Noah Harari";
        return new Object[][]{
            {"/library/book[title='1984']/author", xml, orwell},
            {"/library/book[author='Harper Lee']/title", xml, mockingbid},
            {"/library/book[@genre='fiction'][title='The Great Gatsby']/author", xml, fitzgerald},
            {"/library/book[@genre='non-fiction'][author='Yuval Noah Harari']/title", xml, sapiens},
            {"/library/book[title='Unknown']/author", xml, ""},
            {"/library/book[@genre='fiction'][title='1984']/author", xml, orwell},
            {"/library/book[@genre='fiction'][author='Harper Lee']/title", xml, mockingbid},
            {"/library/book[@genre='non-fiction'][title='Sapiens']/author", xml, harari},
            {"/library/book[author='Unknown']/title", xml, ""},
        };
    }

    /**
     * Complex Xpath expressions.
     *
     * @return Arguments for the test.
     */
    private static Object[][] complexXpaths() {
        final Xml xml = XpathTest.xml(
            XpathTest.ROOT_START,
            "  <o name='one' atom='true' loc='here'>one</o>",
            "  <o name='two' atom='true' base='org.eolang.two' loc='there'><o base='bytes'>two</o></o>",
            "  <o name='three' atom='true' loc='everywhere' lambda='no'>three</o>",
            "  <o name='four' atom='true' loc='nowhere'>four</o>",
            "  <x a='true'>x1</x>",
            "  <x a='true' b='false'>x2</x>",
            "  <o base='Q.org.eolang.number'><o base='Q.org.eolang.bytes'>1-hex-content</o></o>",
            "  <o base='org.eolang.bytes'><o base='bytes'>2-bytes-</o><o base='org.eolang.bytes'><o base='bytes'>content</o></o></o>",
            "  <o><o base='Q.org.eolang.string'><o base='Q.org.eolang.bytes'>first-</o><o>content</o></o></o>",
            "  <o><o><o base='Q.org.eolang.string'><o base='Q.org.eolang.bytes'>second-</o><o>content</o></o></o></o>",
            "  <o base='Q.org.eolang.string'><o base='Q.org.eolang.bytes'>third-</o><o>content</o></o>",
            XpathTest.ROOT_END
        );
        return new Object[][]{
            {"(//o[@name and @atom and not(@base) and @loc and not(@lambda)])[1]", xml, "one"},
            {
                "(//o[(@base='org.eolang.two' or @base='org.eolang.org.eolang.two') and(not(@skip)) and o[not(o) and string-length(normalize-space(text()))>0 and (@base='bytes' or @base='org.eolang.bytes')]])[1]",
                xml,
                "two",
            },
            {"(//x[@a and not(@b)])[1]", xml, "x1"},
            {"(//x[@a and not(@b)])[2]", xml, ""},
            {"(//x[@a and @b])[1]", xml, "x2"},
            {
                "(//o[(@base='org.eolang.bytes' or @base='org.eolang.org.eolang.bytes') and(not(@skip)) and o[not(o) and string-length(normalize-space(text()))>0 and (@base='bytes' or @base='org.eolang.bytes')]])[1]",
                xml,
                "2-bytes-content",
            },
            {
                "(//o[@base='Q.org.eolang.number' and(not(@skip)) and o[1][@base='Q.org.eolang.bytes' and not(o) and string-length(normalize-space(text()))>0]])[1]",
                xml,
                "1-hex-content",
            },
            {
                "//o[@base='Q.org.eolang.string' and(not(@skip)) and o[1][@base='Q.org.eolang.bytes' and not(o) and string-length(normalize-space(text()))>0]]",
                xml,
                "first-content",
            },
        };
    }

    /**
     * Creates an XML from the lines.
     * @param lines Lines of XML.
     * @return XML.
     */
    private static Xml xml(final String... lines) {
        return new DomXml(String.join("\n", lines));
    }

}
