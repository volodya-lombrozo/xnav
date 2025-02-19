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
import java.io.StringReader;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.eolang.jeo.representation.BytecodeRepresentation;
import org.eolang.jeo.representation.bytecode.Bytecode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Comprehensive XML benchmark.
 * Here we compare different XML implementations and their performance.
 * @since 0.1
 * @checkstyle HideUtilityClassConstructorCheck (500 lines)
 */
@SuppressWarnings({"PMD.UseUtilityClass", "PMD.ProhibitPublicStaticMethods"})
public class XmlBenchmark {

    /**
     * DOM implementation tag.
     * This implementation uses DOM to parse XML files.
     * It doesn't create any XML mapping and just allows to travers XML
     * on demand.
     * See {@link DomXml} for more info.
     */
    private static final String DOM = "dom-xml";

    /**
     * VTD implementation tag.
     * This implementation uses VTD-XML to parse XML files.
     * It doesn't create any XML mapping and just allows to travers XML
     * on demand.
     * See {@link VtdXml} for more info.
     */
    private static final String VTD = "vtd-xml";

    /**
     * ANTLR implementation tag.
     * This implementation uses ANTLR to parse XML files and then
     * creates a tree of objects.
     * It's rather simple, slow and memory-consuming.
     * See {@link ObjectXml} for more info.
     */
    private static final String ANTLR_OBJECT = "antlr-object-xml";

    /**
     * Flat DOM implementation tag.
     * This implementation uses DOM to parse XML files and then
     * creates a flat structure of objects.
     * See {@link FlatXmlModel} for more info.
     */
    private static final String FLAT_DOM = "flat-dom-xml";

    /**
     * Flat ANTLR implementation tag.
     * This implmenetation uses ANTLR to parse XML files and then
     * creates a flat structure of objects.
     * See {@link FlatXmlModel} for more info.
     */
    private static final String FLAT_ANTLR = "flat-antlr-xml";

    /**
     * Saxon implementation tag.
     * This implementation is based on Saxon without Xnav abstraction at all.
     * We need to assess the performance of the Saxon library itself
     * without any additional overhead.
     */
    private static final String SAXON = "saxon";

    /**
     * All XPath queries with expected results.
     */
    private static final String[][] QUERIES = {
        {"/program/@name", "j$Collections"},
        {"/program/objects/o/@base", "Q.jeo.class"},
        {"/program/objects/o/o/o/o/@base", "Q.org.eolang.bytes"},
    };

    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static void main(final String[] args) throws RunnerException {
        new Runner(
            new OptionsBuilder()
                .include(XmlBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(2))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(3))
                .build()
        ).run();
    }

    /**
     * Single query.
     * Here we run a single query.
     * @param state Benchmark state.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void singleQuery(final BenchmarkState state) {
        final String[] query = XmlBenchmark.QUERIES[0];
        final String actual = state.implementation().apply(query[0]);
        final String expected = query[1];
        XmlBenchmark.assertTrue(
            actual.equals(expected),
            String.format(
                "Can't find correct nodes by path %s, actual: %s, expected: %s with implementation %s",
                query[0], actual, expected, state.impl
            )
        );
    }

    /**
     * Many queries.
     * Here we run many queries one by one
     * @param state Benchmark state.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void manyQueries(final BenchmarkState state) {
        final Random random = new Random();
        final Function<String, String> impl = state.implementation();
        for (int iteration = 0; iteration < 1000; ++iteration) {
            final int request = random.nextInt(XmlBenchmark.QUERIES.length);
            final String query = XmlBenchmark.QUERIES[request][0];
            final String expected = XmlBenchmark.QUERIES[request][1];
            final String actual = impl.apply(query);
            XmlBenchmark.assertTrue(
                actual.equals(expected),
                String.format(
                    "Can't find correct nodes by path %s, actual: %s, expected: %s with implementation %s",
                    query, actual, expected, state.impl
                )
            );
        }
    }

    /**
     * Parallel queries.
     * Here we run many queries in parallel.
     * @param state Benchmark state.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void parallelQueries(final BenchmarkState state) {
        final Random random = new Random();
        final AtomicInteger counter = new AtomicInteger(1000);
        final Function<String, String> impl = state.implementation();
        XmlBenchmark.assertTrue(
            new Together<>(
                number -> {
                    boolean res = true;
                    while (counter.get() > 0) {
                        final int request = random.nextInt(XmlBenchmark.QUERIES.length);
                        final String query = XmlBenchmark.QUERIES[request][0];
                        final String expected = XmlBenchmark.QUERIES[request][1];
                        final boolean equals = impl.apply(query).equals(expected);
                        res = res && equals;
                        counter.decrementAndGet();
                    }
                    return res;
                }
            ).asList().stream().allMatch(Boolean::booleanValue),
            "Can't find correct nodes by path in parallel"
        );
    }

    /**
     * Small XML.
     * This method generates a small XML document.
     * This XML is always the same.
     * @return Small XML.
     */
    private static String small() {
        return String.join(
            "\n",
            "<program name=\"j$Collections\">",
            "    <objects>",
            "        <o base=\"Q.jeo.class\">",
            "            <o>",
            "                <o>",
            "                    <o base=\"Q.org.eolang.bytes\"/>",
            "                </o>",
            "            </o>",
            "        </o>",
            "    </objects>",
            "</program>"
        );
    }

    /**
     * Large XML.
     * This method generates a large XML document.
     * Moreover, this XML is always different to avoid caching.
     * @return Large XML.
     */
    private static String large() {
        try {
            final Random rand = new Random();
            return new BytecodeRepresentation(
                new Bytecode(
                    Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(
                            String.format(
                                "%s.class",
                                Collections.class.getName().replace('.', '/')
                            )
                        ).readAllBytes()
                )
            ).toEO().toString().replace(
                "74-69-6C-2F-43-6F-6C-6C",
                String.format("%d-%d", rand.nextInt(), rand.nextInt())
            );
        } catch (final IOException exception) {
            throw new IllegalStateException("Can't generate large XML document", exception);
        }
    }

    /**
     * Assert that assertion is true.
     * @param assertion Assertion.
     * @return True if assertion is true.
     */
    private static boolean assertTrue(final boolean assertion, final String msg) {
        if (assertion) {
            return true;
        }
        throw new AssertionError(String.format("Assertion failed: %s", msg));
    }

    /**
     * State with small xml file.
     * @since 0.1
     */
    @State(Scope.Benchmark)
    public static class BenchmarkState {

        /**
         * Current implementation.
         * @checkstyle VisibilityModifierCheck (10 lines)
         */
        @Param({
            XmlBenchmark.DOM,
//            XmlBenchmark.VTD,
//            XmlBenchmark.ANTLR_OBJECT,
//            XmlBenchmark.FLAT_DOM,
//            XmlBenchmark.FLAT_ANTLR,
//            XmlBenchmark.SAXON
        })
        String impl;

        /**
         * Xml file size.
         * @checkstyle VisibilityModifierCheck (3 lines)
         */
        @Param({"small", "large"})
        String size;

        /**
         * Current Xml file.
         */
        private String xml;

        /**
         * Set up the state.
         */
        @Setup(Level.Trial)
        public void init() {
            switch (this.size) {
                case "small":
                    this.xml = XmlBenchmark.small();
                    break;
                case "large":
                    this.xml = XmlBenchmark.large();
                    break;
                default:
                    throw new IllegalArgumentException(
                        String.format("Unknown size %s", this.size)
                    );
            }
        }

        /**
         * Xpath implementation to use in benchmarks.
         * @return Xpath implementation.
         */
        Function<String, String> implementation() {
            final Function<String, String> res;
            if (XmlBenchmark.SAXON.equals(this.impl)) {
                res = this.saxon();
            } else {
                final Xml instance = this.instance();
                res = path -> new Xpath(instance, path)
                    .nodes()
                    .findFirst()
                    .map(Xml::text)
                    .orElseThrow(
                        () -> new IllegalStateException(
                            String.format("Can't find any nodes by path '%s'", path)
                        )
                    ).get();
            }
            return res;
        }

        /**
         * Saxon implementation.
         * @return Saxon implementation.
         */
        private Function<String, String> saxon() {
            try {
                final Processor processor = new Processor(false);
                final XdmNode node = processor.newDocumentBuilder().build(
                    new StreamSource(new StringReader(this.xml))
                );
                return path -> {
                    try {
                        return processor.newXPathCompiler()
                            .evaluate(path, node)
                            .getUnderlyingValue()
                            .head()
                            .getStringValue();
                    } catch (final SaxonApiException exception) {
                        throw new IllegalStateException(
                            String.format("Can't evaluate XPath expression '%s'", path),
                            exception
                        );
                    }
                };
            } catch (final SaxonApiException exception) {
                throw new IllegalStateException(
                    "Can't prepare Saxon processor",
                    exception
                );
            }
        }

        /**
         * Create one of the Xnav Xml implementations.
         * @return Xml implementation.
         */
        private Xml instance() {
            final Xml result;
            switch (this.impl) {
                case XmlBenchmark.DOM:
                    result = new DomXml(this.xml);
                    break;
                case XmlBenchmark.VTD:
                    result = new VtdXml(this.xml);
                    break;
                case XmlBenchmark.ANTLR_OBJECT:
                    result = new ObjectXml(this.xml);
                    break;
                case XmlBenchmark.FLAT_DOM:
                    result = new FlatXml(this.xml, new FlatDom());
                    break;
                case XmlBenchmark.FLAT_ANTLR:
                    result = new FlatXml(this.xml, new FlatAntlr());
                    break;
                default:
                    throw new IllegalArgumentException(
                        String.format("Unknown implementation %s", this.impl)
                    );
            }
            return result;
        }
    }
}
