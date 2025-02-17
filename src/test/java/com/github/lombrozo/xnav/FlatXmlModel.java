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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public final class FlatXmlModel {

    private final Map<Integer, Integer> firstChild;
    private final Map<Integer, Integer> nextSibling;
    private final List<Integer> tag;
    private final List<Type> type;
    private final StringPool pool;

    FlatXmlModel() {
        this.firstChild = new HashMap<>(0);
        this.nextSibling = new HashMap<>(0);
        this.tag = new ArrayList<>(0);
        this.type = new ArrayList<>(0);
        this.pool = new StringPool();
    }

    public Xml child(final int id) {
        switch (this.type.get(id)) {
            case DOCUMENT:
            case ELEMENT:
                return new FlatXmlElement(id, this);
            case CONTENT:
                return new FlatXmlContent(id, this);
            case CHARDATA:
                return new FlatXmlChardata(id, this);
            case ATTRIBUTE:
                return new FlatXmlAttribute(id, this);
            default:
                throw new IllegalStateException("Unknown type");
        }
    }

    public Stream<Xml> children(final int id) {
        final int first = this.firstChild.getOrDefault(id, -1);
        if (first == -1) {
            return Stream.empty();
        } else {
            if (!this.nextSibling.containsKey(first)) {
                return Stream.of(this.child(first));
            } else {
                List<Xml> result = new ArrayList<>(0);
                Integer child = this.firstChild.get(id);
                while (child != null && child != -1) {
                    result.add(this.child(child));
                    child = this.nextSibling.get(child);
                }
                return result.stream();
            }
        }
    }

    public void addElement(
        final int parent,
        final int current,
        final Type type,
        final String text
    ) {
        this.type.add(type);
        if (text != null) {
            this.tag.add(this.pool.id(text));
        } else {
            this.tag.add(-1);
        }
        if (parent != -1) {
            final int first = this.firstChild.getOrDefault(parent, -1);
            if (first == -1) {
                this.firstChild.put(parent, current);
            } else {
                int next = first;
                while (this.nextSibling.containsKey(next)) {
                    next = this.nextSibling.get(next);
                }
                this.nextSibling.put(next, current);
            }
        }
    }

    public String content(final int id) {
        return this.pool.string(this.tag.get(id));
    }

    enum Type {

        DOCUMENT,
        ELEMENT,
        CONTENT,
        ATTRIBUTE,
        CHARDATA

    }

    @ToString
    @EqualsAndHashCode
    static class StringPool {
        private final Map<String, Integer> map = new HashMap<>(0);
        private final List<String> indexed = new ArrayList<>(0);

        int id(final String string) {
            return this.map.computeIfAbsent(string, this::add);
        }

        String string(int id) {
            if (id == -1) {
                return "";
            }
            return this.indexed.get(id);
        }

        private int add(final String k) {
            this.indexed.add(k);
            return this.indexed.size() - 1;
        }
    }
}
