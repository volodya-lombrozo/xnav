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

import com.jcabi.log.Logger;
import com.yegor256.Together;
import java.io.IOException;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import org.eolang.jeo.representation.BytecodeRepresentation;
import org.eolang.jeo.representation.bytecode.Bytecode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class XmlBenchmark {

    private static final String DOM = "dom-xml";

    private static final String VTD = "vtd-xml";

    private static final String ANTLR_OBJECT = "antlr-object-xml";

    private static final String FLAT_DOM = "flat-dom-xml";

    private static final String FLAT_ANTLR = "flat-antlr-xml";

    private static final String HUGE_XML = XmlBenchmark.generateXml();

    private static final String SIMPLE_XML = "<root><child>text</child></root>";

    private static final String SIMPLE = "simpleXmlOneQuery";
    private static final String HUGE = "hugeXmlOneQuery";

    private static final String HUGE_MANY = "hugeXmlManyQueries";

    private static final String HUGE_PARALLEL = "hugeXmlParallelQueries";

    private static final Random random = new SecureRandom();


    /**
     * State with huge xml file.
     * @since 0.1
     */
    @State(Scope.Benchmark)
    public static class HugeXml {

        @Param({
            XmlBenchmark.DOM,
            XmlBenchmark.VTD,
            XmlBenchmark.ANTLR_OBJECT,
            XmlBenchmark.FLAT_DOM,
            XmlBenchmark.FLAT_ANTLR
        })
        public String implementation;

        /**
         * Xml file.
         */
        String xml;

        @Setup(Level.Trial)
        public void up() {
            Logger.info(this, "Generating huge xml file");
            this.xml = XmlBenchmark.generateXml();
        }
    }

    /**
     * State with small xml file.
     * @since 0.1
     */
    @State(Scope.Benchmark)
    public static class SmallXml {

        @Param({
            XmlBenchmark.DOM,
            XmlBenchmark.VTD,
            XmlBenchmark.ANTLR_OBJECT,
            XmlBenchmark.FLAT_DOM,
            XmlBenchmark.FLAT_ANTLR
        })
        public String implementation;

        /**
         * Xml file.
         */
        String xml;

        @Setup(Level.Trial)
        public void up() {
            this.xml = "<root><child>text</child></root>";
        }

        public Xml instance() {
            switch (this.implementation) {
                case XmlBenchmark.DOM:
                    return new DomXml(xml);
                case XmlBenchmark.VTD:
                    return new VtdXml(xml);
                case XmlBenchmark.ANTLR_OBJECT:
                    return new ObjectXml(xml);
                case XmlBenchmark.FLAT_DOM:
                    return new FlatXml(xml, new FlatDom());
                case XmlBenchmark.FLAT_ANTLR:
                    return new FlatXml(xml, new FlatAntlr());
                default:
                    throw new IllegalArgumentException(
                        String.format("Unknown implementation %s", this.implementation)
                    );
            }
        }
    }


    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
            .include(XmlBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(2)
            .warmupTime(TimeValue.seconds(6))
            .measurementIterations(2)
            .measurementTime(TimeValue.seconds(10))
            .build();
        new Runner(opt).run();
    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void singleQueryForSmallXml(final SmallXml state) {
        assertTrue(
            new Xpath(state.instance(), "/root/child")
                .nodes()
                .findFirst()
                .map(Xml::text).get().get().equals("text")
        );
    }


    @Benchmark
    @Group(XmlBenchmark.SIMPLE)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void simpleWithDomXml() {
        final DomXml xml = new DomXml(XmlBenchmark.SIMPLE_XML);
        assert new Xpath(xml, "/root/child")
            .nodes()
            .findFirst()
            .map(Xml::text).get().get().equals("text");
    }

    @Benchmark
    @Group(XmlBenchmark.SIMPLE)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void simpleWithVtdXml() {
        final Xml xml = new VtdXml(XmlBenchmark.SIMPLE_XML);
        assertTrue(new Xpath(xml, "/root/child")
            .nodes()
            .findFirst()
            .map(Xml::text).get().get().equals("text"));
    }

    @Benchmark
    @Group(XmlBenchmark.SIMPLE)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void simpleWithOptXml() {
        final Xml xml = new FlatXml(XmlBenchmark.SIMPLE_XML);
        assert new Xpath(xml, "/root/child")
            .nodes()
            .findFirst()
            .map(Xml::text).get().get().equals("text");
    }

    @Benchmark
    @Group(XmlBenchmark.SIMPLE)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void simpleWithEagerXml() {
        final Xml xml = new ObjectXml(XmlBenchmark.SIMPLE_XML);
        assert new Xpath(xml, "/root/child")
            .nodes()
            .findFirst()
            .map(Xml::text).get().get().equals("text");
    }

    @Benchmark
    @Group(XmlBenchmark.SIMPLE)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void simpleWithSaxonXpath() throws SaxonApiException, XPathException {
        final Processor processor = new Processor(false);
        assert processor.newXPathCompiler().evaluate(
            "/root/child",
            processor.newDocumentBuilder()
                .build(new StreamSource(new StringReader(XmlBenchmark.SIMPLE_XML)))
        ).getUnderlyingValue().getStringValue().equals("text");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeSingleWithDomXml() {
        final DomXml xml = new DomXml(XmlBenchmark.HUGE_XML);
        assert new Xpath(xml, "/program/@name")
            .nodes()
            .findFirst()
            .map(Xml::text).get().get().equals("j$Collections");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeSingleWithVtdXml() {
        final Xml xml = new VtdXml(XmlBenchmark.HUGE_XML);
        assertTrue(new Xpath(xml, "/program/@name")
            .nodes()
            .findFirst()
            .map(Xml::text).get().get().equals("j$Collections"));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeSingleWithOptXml() {
        final Xml xml = new FlatXml(XmlBenchmark.HUGE_XML);
        assert new Xpath(xml, "/program/@name")
            .nodes()
            .findFirst()
            .map(Xml::text).get().get().equals("j$Collections");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeSingleWithEagerXml() {
        final Xml xml = new ObjectXml(XmlBenchmark.HUGE_XML);
        assert new Xpath(xml, "/program/@name")
            .nodes()
            .findFirst()
            .map(Xml::text).get().get().equals("j$Collections");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeSingleWithSaxonXpath() throws SaxonApiException, XPathException {
        final Processor processor = new Processor(false);
        assert processor.newXPathCompiler().evaluate(
            "/program/@name",
            processor.newDocumentBuilder()
                .build(new StreamSource(new StringReader(XmlBenchmark.HUGE_XML)))
        ).getUnderlyingValue().getStringValue().equals("j$Collections");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE_MANY)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeManyWithDomXml() {
        Object[][] queries = {
            {"/program/@name", "j$Collections"},
            {"/program/objects/o/@base", "jeo.class"},
            {"/program/objects/o/o/o/o/@base", "org.eolang.bytes"},
        };
        final DomXml xml = new DomXml(XmlBenchmark.HUGE_XML);
        for (int i = 0; i < 1000; i++) {
            final int request = random.nextInt(queries.length);
            String query = queries[request][0].toString();
            String expected = queries[request][1].toString();
            assert new Xpath(xml, query)
                .nodes()
                .findFirst()
                .map(Xml::text).get().get().equals(expected);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE_MANY)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeManyWithOptXml() {
        Object[][] queries = new Object[][]{
            {"/program/@name", "j$Collections"},
            {"/program/objects/o/@base", "jeo.class"},
            {"/program/objects/o/o/o/o/@base", "org.eolang.bytes"},
        };
        final Xml xml = new FlatXml(XmlBenchmark.HUGE_XML);
        for (int i = 0; i < 1000; i++) {
            final int request = random.nextInt(queries.length);
            String query = queries[request][0].toString();
            String expected = queries[request][1].toString();
            assert new Xpath(xml, query)
                .nodes()
                .findFirst()
                .map(Xml::text).get().get().equals(expected);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE_MANY)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeManyWithEagerXml() {
        Object[][] queries = new Object[][]{
            {"/program/@name", "j$Collections"},
            {"/program/objects/o/@base", "jeo.class"},
            {"/program/objects/o/o/o/o/@base", "org.eolang.bytes"},
        };
        final Xml xml = new ObjectXml(XmlBenchmark.HUGE_XML);
        for (int i = 0; i < 1000; i++) {
            final int request = random.nextInt(queries.length);
            String query = queries[request][0].toString();
            String expected = queries[request][1].toString();
            assert new Xpath(xml, query)
                .nodes()
                .findFirst()
                .map(Xml::text).get().get().equals(expected);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE_MANY)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeManyWithSaxonXpath() throws SaxonApiException, XPathException {
        Object[][] queries = new Object[][]{
            {"/program/@name", "j$Collections"},
            {"/program/objects/o/@base", "jeo.class"},
            {"/program/objects/o/o/o/o/@base", "org.eolang.bytes"},
        };
        final Processor processor = new Processor(false);
        final StreamSource source = new StreamSource(new StringReader(XmlBenchmark.HUGE_XML));
        final XdmNode node = processor.newDocumentBuilder()
            .build(source);
        for (int i = 0; i < 1000; i++) {
            final int request = random.nextInt(queries.length);
            String query = queries[request][0].toString();
            String expected = queries[request][1].toString();
            assert processor.newXPathCompiler()
                .evaluate(query, node)
                .getUnderlyingValue()
                .getStringValue().equals(expected);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE_MANY)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeManyWithVtdXml() {
        Object[][] queries = {
            {"/program/@name", "j$Collections"},
            {"/program/objects/o/@base", "jeo.class"},
            {"/program/objects/o/o/o/o/@base", "org.eolang.bytes"},
        };
        final Xml xml = new VtdXml(XmlBenchmark.HUGE_XML);
        for (int i = 0; i < 1000; i++) {
            final int request = random.nextInt(queries.length);
            String query = queries[request][0].toString();
            String expected = queries[request][1].toString();
            assertTrue(
                new Xpath(xml, query)
                    .nodes()
                    .findFirst()
                    .map(Xml::text).get().get().equals(expected)
            );
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE_PARALLEL)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeManyInParallelWithDomXml() {
        final Object[][] queries = {
            {"/program/@name", "j$Collections"},
            {"/program/objects/o/@base", "jeo.class"},
            {"/program/objects/o/o/o/o/@base", "org.eolang.bytes"},
        };
        final Xml xml = new DomXml(XmlBenchmark.HUGE_XML);
        AtomicInteger counter = new AtomicInteger(1000);
        final Random random = new Random();
        assertTrue(
            new Together<>(i -> {
                boolean res = true;
                while (counter.get() > 0) {
                    final int request = random.nextInt(queries.length);
                    String query = queries[request][0].toString();
                    String expected = queries[request][1].toString();
                    final boolean equals;
                    synchronized (xml) {
                        equals = new Xpath(xml, query)
                            .nodes()
                            .findFirst()
                            .map(Xml::text).get().get().equals(expected);
                    }
                    res = res && equals;
                    counter.decrementAndGet();
                }
                return res;
            }).asList().stream().allMatch(Boolean::booleanValue)
        );
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE_PARALLEL)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeManyInParallelWithEagerXml() {
        final Object[][] queries = {
            {"/program/@name", "j$Collections"},
            {"/program/objects/o/@base", "jeo.class"},
            {"/program/objects/o/o/o/o/@base", "org.eolang.bytes"},
        };
        final Xml xml = new ObjectXml(XmlBenchmark.HUGE_XML);
        AtomicInteger counter = new AtomicInteger(1000);
        final Random random = new Random();
        assertTrue(
            new Together<>(i -> {
                boolean res = true;
                while (counter.get() > 0) {
                    final int request = random.nextInt(queries.length);
                    String query = queries[request][0].toString();
                    String expected = queries[request][1].toString();
                    final boolean equals;
                    equals = new Xpath(xml, query)
                        .nodes()
                        .findFirst()
                        .map(Xml::text).get().get().equals(expected);
                    res = res && equals;
                    counter.decrementAndGet();
                }
                return res;
            }).asList().stream().allMatch(Boolean::booleanValue)
        );
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE_PARALLEL)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeManyInParallelWithVtdXml() {
        final Object[][] queries = {
            {"/program/@name", "j$Collections"},
            {"/program/objects/o/@base", "jeo.class"},
            {"/program/objects/o/o/o/o/@base", "org.eolang.bytes"},
        };
        final Xml xml = new VtdXml(XmlBenchmark.HUGE_XML);
        AtomicInteger counter = new AtomicInteger(1000);
        final Random random = new Random();
        assertTrue(
            new Together<>(i -> {
                boolean res = true;
                while (counter.get() > 0) {
                    final int request = random.nextInt(queries.length);
                    String query = queries[request][0].toString();
                    String expected = queries[request][1].toString();
                    final boolean equals;
                    equals = new Xpath(xml, query)
                        .nodes()
                        .findFirst()
                        .map(Xml::text).get().get().equals(expected);
                    res = res && equals;
                    counter.decrementAndGet();
                }
                return res;
            }).asList().stream().allMatch(Boolean::booleanValue)
        );
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE_PARALLEL)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeManyInParallelWithSaxon() throws SaxonApiException {
        final Object[][] queries = {
            {"/program/@name", "j$Collections"},
            {"/program/objects/o/@base", "jeo.class"},
            {"/program/objects/o/o/o/o/@base", "org.eolang.bytes"},
        };
        final Random random = new Random();
        final Processor processor = new Processor(false);
        final StreamSource source = new StreamSource(new StringReader(XmlBenchmark.HUGE_XML));
        final XdmNode node = processor.newDocumentBuilder()
            .build(source);
        final AtomicInteger counter = new AtomicInteger(1000);
        assertTrue(
            new Together<>(i -> {
                boolean res = true;
                while (counter.get() > 0) {
                    final int request = random.nextInt(queries.length);
                    String query = queries[request][0].toString();
                    String expected = queries[request][1].toString();
                    final boolean equals;
                    final String value = processor.newXPathCompiler()
                        .evaluate(query, node)
                        .getUnderlyingValue()
                        .head()
                        .getStringValue();
                    equals = value.equals(expected);
                    res = res && equals;
                    counter.decrementAndGet();
                }
                return res;
            }).asList().stream().allMatch(Boolean::booleanValue)
        );
    }


    public static String generateXml() {
        final String clazz = Collections.class.getName().replace('.', '/') + ".class";
        final BytecodeRepresentation bytecode;
        try {
            final byte[] bytes = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(clazz)
                .readAllBytes();
            bytecode = new BytecodeRepresentation(
                new Bytecode(
                    bytes
                )
            );
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
        final Random rand = new Random();
        return bytecode.toEO().toString()
            .replace(
                "74-69-6C-2F-43-6F-6C-6C",
                String.format("%d-%d", rand.nextInt(), rand.nextInt())
            );
    }

    private boolean assertTrue(final boolean assertion) {
        return assertTrue(assertion, "");
    }

    private boolean assertTrue(final boolean assertion, String msg) {
        if (!assertion) {
            throw new AssertionError(msg);
        }
        return true;
    }
}
