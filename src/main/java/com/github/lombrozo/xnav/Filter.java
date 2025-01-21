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

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Filter extends Predicate<Xml> {

    /**
     * Filter XML nodes.
     * @param xml XML.
     * @return Stream of XML nodes.
     */
    boolean test(Xml xml);

    /**
     * Combine filters with AND.
     * @param filters Filters.
     * @return Combined filter.
     */
    static Filter all(final Filter... filters) {
        return xml -> Stream.of(filters).allMatch(filter -> filter.test(xml));
    }

    /**
     * Combine filters with OR.
     * @param filters Filters.
     * @return Combined filter.
     */
    static Filter any(final Filter... filters) {
        return xml -> Stream.of(filters).anyMatch(filter -> filter.test(xml));
    }

    /**
     * Filter XML nodes by name.
     * @param name Name.
     * @return Filter.
     */
    static Filter withName(final String name) {
        return xml -> xml.name().equals(name);
    }

    /**
     * Filter XML nodes by attribute.
     * @param name Name of the attribute.
     * @param value Value of the attribute.
     * @return Filter.
     */
    static Filter withAttribute(final String name, final String value) {
        return xml -> xml.attribute(name).map(value::equals).orElse(false);
    }

    /**
     * Filter XML nodes by attribute presence.
     * @param name Name of the attribute.
     * @return Filter.
     */
    static Filter hasAttribute(final String name) {
        return xml -> xml.attribute(name).isPresent();
    }

    /**
     * Reverse the filter.
     * @param filter Filter to reverse.
     * @return Filter.
     */
    static Filter not(final Filter filter) {
        return xml -> !filter.test(xml);
    }
}
