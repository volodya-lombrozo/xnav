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

import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Benchmark for Xnav.
 * @since 0.1
 * @checkstyle HideUtilityClassConstructor (500 lines)
 */
@SuppressWarnings({"PMD.ProhibitPublicStaticMethods", "PMD.UseUtilityClass"})
public class XnavBenchmark {

    /**
     * Benchmark starting point.
     * @param args Arguments.
     * @throws RunnerException If something goes wrong.
     */
    public static void main(final String[] args) throws RunnerException {
        new Runner(
            new OptionsBuilder()
                .include(XnavBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(2))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(3))
                .build()
        ).run();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public static void xpath() {
        final String acutal = new Xnav("<root><a><b><c>text</c></b></a></root>")
            .path("/root/a/b/c")
            .findFirst()
            .orElseThrow()
            .text()
            .orElseThrow();
        MatcherAssert.assertThat(
            "Xpath failed",
            acutal,
            Matchers.equalTo("text")
        );
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public static void element() {
        final String acutal = new Xnav("<root><a><b><c>deep</c></b></a></root>")
            .element("root")
            .element("a")
            .element("b")
            .element("c")
            .text()
            .orElseThrow();
        MatcherAssert.assertThat(
            "Element failed",
            acutal,
            Matchers.equalTo("deep")
        );
    }
}
