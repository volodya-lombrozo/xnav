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

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;
import org.eolang.jeo.representation.BytecodeRepresentation;
import org.eolang.jeo.representation.bytecode.Bytecode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class XmlBenchmark {

    private static final String HUGE_XML = XmlBenchmark.generateXml();
    private static final String SIMPLE_XML = "<root><child>text</child></root>";

    private static final String SIMPLE = "simpleXmlOneQuery";
    private static final String HUGE = "hugeXmlOneQuery";

    private static final String HUGE_MANY = "hugeXmlManyQueries";

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
            .include(XmlBenchmark.class.getSimpleName())
            .forks(1)
            .warmupTime(TimeValue.seconds(1))
            .measurementTime(TimeValue.seconds(2))
            .build();
        new Runner(opt).run();
    }

    @Benchmark
    @Group(XmlBenchmark.SIMPLE)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void simpleWithDomXml() {
        final DomXml xml = new DomXml(SIMPLE_XML);
        new Xpath(xml, "/root/child")
            .nodes()
            .findFirst()
            .map(Xml::text).get().get().equals("text");
    }

    @Benchmark
    @Group(XmlBenchmark.SIMPLE)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void simpleWithEagerXml() {
        final Xml xml = new EagerXml(XmlBenchmark.SIMPLE_XML);
        new Xpath(xml, "/root/child")
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
        processor.newXPathCompiler().evaluate(
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
        new Xpath(xml, "/program/@name")
            .nodes()
            .findFirst()
            .map(Xml::text).get().get().equals("j$Collections");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group(XmlBenchmark.HUGE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void hugeSingleWithEagerXml() {
        final Xml xml = new EagerXml(XmlBenchmark.HUGE_XML);
        new Xpath(xml, "/program/@name")
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
        processor.newXPathCompiler().evaluate(
            "/program/@name",
            processor.newDocumentBuilder()
                .build(new StreamSource(new StringReader(XmlBenchmark.HUGE_XML)))
        ).getUnderlyingValue().getStringValue().equals("j$Collections");
    }

    private static String generateXml() {
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
        final String string = bytecode.toEO().toString();
        return string;
    }
}
