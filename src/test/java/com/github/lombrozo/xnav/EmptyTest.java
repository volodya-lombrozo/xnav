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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link Empty}.
 * @since 0.1
 */
final class EmptyTest {

    @Test
    void retrievesChild() {
        final Empty empty = new Empty();
        MatcherAssert.assertThat(
            "Child method should return the same instance",
            empty.child("any"),
            Matchers.is(Matchers.sameInstance(empty))
        );
    }

    @Test
    void retrievesAttribute() {
        MatcherAssert.assertThat(
            "Attribute method should return an empty Optional",
            new Empty().attribute("any"),
            Matchers.is(Optional.empty())
        );
    }

    @Test
    void retrievesText() {
        MatcherAssert.assertThat(
            "Text method should return an empty Optional", new Empty().text(),
            Matchers.is(Optional.empty())
        );
    }

    @Test
    void retrievesChildren() {
        MatcherAssert.assertThat(
            "Children method should return an empty Stream",
            new Empty().children().collect(Collectors.toList()),
            Matchers.empty()
        );
    }

    @Test
    void getsName() {
        MatcherAssert.assertThat(
            "Name method should return an empty string",
            new Empty().name(),
            Matchers.is(Matchers.emptyString())
        );
    }

    @Test
    void copies() {
        final Empty empty = new Empty();
        MatcherAssert.assertThat(
            "Copy method should return the same instance", empty.copy(),
            Matchers.is(Matchers.sameInstance(empty))
        );
    }

    @Test
    void convertsToNode() {
        final Empty empty = new Empty();
        Assertions.assertThrows(
            UnsupportedOperationException.class,
            empty::node,
            "Node method should throw UnsupportedOperationException"
        );
    }
}
