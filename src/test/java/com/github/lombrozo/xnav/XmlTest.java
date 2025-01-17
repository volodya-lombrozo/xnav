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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Navigator}.
 * @since 0.1
 */
final class XmlTest {

    @Test
    void convertsDocumentToString() {
        MatcherAssert.assertThat(
            "Document is not converted to string",
            new Xml("<doc></doc>").toString(),
            Matchers.equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><doc/>")
        );
    }

    @Test
    void convertsNodeToString() {
        MatcherAssert.assertThat(
            "Node is not converted to string",
            new Xml("<doc><node>text</node></doc>").child("doc").child("node").toString(),
            Matchers.equalTo("<node>text</node>")
        );
    }

    @Test
    void failsToCreateCorruptedDocument() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Xml("<doc..."),
            "Corrupted document is not created, exception is expected"
        );
    }
}
