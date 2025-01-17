package com.github.lombrozo.xnav;

import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test case for {@link Navigator}.
 * @since 0.1
 */
class NavigatorTest {

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
            Arguments.of(
                xml,
                ""
            ),
            Arguments.of(
                xml
                    .child(program)
                    .child(metas),
                "version\n1.2.3"
            ),
            Arguments.of(
                xml
                    .child(program)
                    .child(metas)
                    .child("meta"),
                "version\n1.2.3"
            ),
            Arguments.of(
                xml
                    .child(program)
                    .child(metas)
                    .child("meta")
                    .child("head"),
                "version"
            ),
            Arguments.of(
                xml
                    .child(program)
                    .child(metas)
                    .child("meta")
                    .child("tail"),
                "1.2.3"
            )
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
            Arguments.of(
                xml
                    .child("prog")
                    .attribute("progattr"),
                "1"
            ),
            Arguments.of(
                xml
                    .child("prog")
                    .child("m")
                    .attribute("metarg"),
                "2"
            ),
            Arguments.of(
                xml
                    .child("prog")
                    .child("m")
                    .child("h")
                    .attribute("harg"),
                "3"
            ),
            Arguments.of(
                xml
                    .child("prog")
                    .child("m")
                    .child("t")
                    .attribute("targ"),
                "4"
            )
        );
    }

}