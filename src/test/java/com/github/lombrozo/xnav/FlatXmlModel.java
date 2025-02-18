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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This model is used to store XML data in a flat structure.
 * This class helps to avoid rapid memory allocation and deallocation.
 * Alternative approach is to store all the nodes as objects:
 * see {@link ObjectXml}
 * @since 0.1
 */
@ToString
@EqualsAndHashCode
public final class FlatXmlModel {

    /**
     * First child map.
     */
    private final Map<Integer, Integer> first;

    /**
     * Next sibling map.
     */
    private final Map<Integer, Integer> sibling;

    /**
     * Attached text.
     */
    private final List<Integer> texts;

    /**
     * Node type.
     */
    private final List<Type> types;

    /**
     * Pool of strings.
     */
    private final StringPool pool;

    /**
     * Constructor.
     */
    FlatXmlModel() {
        this.first = new HashMap<>(0);
        this.sibling = new HashMap<>(0);
        this.texts = new ArrayList<>(0);
        this.types = new ArrayList<>(0);
        this.pool = new StringPool();
    }

    /**
     * Get child by id.
     * @param identifier Child id.
     * @return Child.
     */
    public Xml child(final int identifier) {
        final Xml result;
        switch (this.types.get(identifier)) {
            case DOCUMENT:
            case ELEMENT:
                result = new FlatXmlElement(identifier, this);
                break;
            case CONTENT:
                result = new FlatXmlContent(identifier, this);
                break;
            case CHARDATA:
                result = new FlatXmlChardata(identifier, this);
                break;
            case ATTRIBUTE:
                result = new FlatXmlAttribute(identifier, this);
                break;
            default:
                throw new IllegalStateException("Unknown type");
        }
        return result;
    }

    /**
     * Get children of the node.
     * @param identifier Node id.
     * @return Children.
     */
    public Stream<Xml> children(final int identifier) {
        final Stream<Xml> res;
        final int child = this.first.getOrDefault(identifier, -1);
        if (child == -1) {
            res = Stream.empty();
        } else if (this.sibling.containsKey(child)) {
            final Collection<Xml> result = new ArrayList<>(0);
            Integer next = this.first.get(identifier);
            while (next != null && next != -1) {
                result.add(this.child(next));
                next = this.sibling.get(next);
            }
            res = result.stream();
        } else {
            res = Stream.of(this.child(child));
        }
        return res;
    }

    /**
     * Get text by id.
     * @param identifier Id
     * @return Text.
     */
    public String content(final int identifier) {
        return this.pool.string(this.texts.get(identifier));
    }

    /**
     * Add node to the model.
     * @param parent Parent id.
     * @param current Current id.
     * @param type Node type.
     * @param content Node text.
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    void add(final int parent, final int current, final Type type, final String content) {
        this.types.add(type);
        if (content != null) {
            this.texts.add(this.pool.identifier(content));
        } else {
            this.texts.add(-1);
        }
        if (parent != -1) {
            final int child = this.first.getOrDefault(parent, -1);
            if (child == -1) {
                this.first.put(parent, current);
            } else {
                int next = child;
                while (this.sibling.containsKey(next)) {
                    next = this.sibling.get(next);
                }
                this.sibling.put(next, current);
            }
        }
    }

    /**
     * Node type.
     * @since 0.1
     */
    enum Type {

        /**
         * Document.
         */
        DOCUMENT,

        /**
         * Element.
         */
        ELEMENT,
        /**
         * Element content.
         * Might contain text or child elements.
         */
        CONTENT,

        /**
         * Attribute.
         */
        ATTRIBUTE,

        /**
         * Text.
         */
        CHARDATA;
    }

    /**
     * String pool.
     * @since 0.1
     */
    @ToString
    @EqualsAndHashCode
    static class StringPool {
        /**
         * String to id map.
         */
        private final Map<String, Integer> map = new HashMap<>(0);

        /**
         * Indexed strings.
         */
        private final List<String> indexed = new ArrayList<>(0);

        /**
         * Get identifier for the string.
         * @param string String.
         * @return Identifier.
         */
        int identifier(final String string) {
            return this.map.computeIfAbsent(string, this::add);
        }

        /**
         * Get string by id.
         * @param identifier Id.
         * @return String.
         */
        String string(final int identifier) {
            String result = "";
            if (identifier != -1) {
                result = this.indexed.get(identifier);
            }
            return result;
        }

        /**
         * Add string to the pool.
         * @param text String.
         * @return Identifier.
         */
        private int add(final String text) {
            this.indexed.add(text);
            return this.indexed.size() - 1;
        }
    }
}
