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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * XPath's abstraction over an XML document.
 * This class is thread-safe.
 *
 * @since 0.1
 */
final class Xpath {

    private final Xml root;
    private final String path;

    Xpath(final Xml root, final String path) {
        this.path = path;
        this.root = root;
    }

    Stream<Xml> nodes() {
        new XPathLexer(this.path)
            .tokens()
            .forEach(System.out::println);
        final XpathNode xpath = new XPathParser(new XPathLexer(this.path).tokens())
            .relativePath();
        System.out.println(xpath);
        return xpath.nodes(this.root);
    }

    private class XPathParser {
        private final List<Token> tokens;
        private int pos = 0;

        public XPathParser(final Stream<Token> tokens) {
            this.tokens = tokens.collect(Collectors.toList());
        }

        private Token consume() {
            final Token token = this.tokens.get(this.pos);
            this.pos++;
            return token;
        }


        public XpathNode relativePath() {
            List<XpathNode> steps = new ArrayList<>();
            while (!this.eof() && this.tokens.get(this.pos).type == Type.SLASH) {
                this.consume(); // consume slash
                steps.add(this.step());
            }
            return new RelativePath(steps);
        }

        private boolean eof() {
            return this.pos >= this.tokens.size();
        }

        public XpathNode step() {
            final Token token = this.consume();
            if (token.type == Type.NAME) {
                return new Step(token.lexeme());
            }
            throw new IllegalStateException(String.format("Expected step, but got %s", token));
        }


    }

    @ToString
    @EqualsAndHashCode
    private class RelativePath implements XpathNode {
        private final List<XpathNode> steps;

        public RelativePath(final List<XpathNode> steps) {
            this.steps = steps;
        }

        @Override
        public Stream<Xml> nodes(final Xml xml) {
            Xml current = xml;
            for (int indx = 0; indx < this.steps.size(); indx++) {
                current = this.steps.get(indx).nodes(current)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No nodes found"));
            }
            return Stream.of(current);
        }
    }

    @ToString
    @EqualsAndHashCode
    private class Step implements XpathNode {

        private final String name;

        public Step(final String name) {
            this.name = name;
        }

        @Override
        public Stream<Xml> nodes(final Xml xml) {
            return Stream.of(xml.child(this.name));
        }
    }


    private interface XpathNode {
        Stream<Xml> nodes(Xml xml);
    }


    private class XPathLexer {
        private final String path;

        public XPathLexer(final String path) {
            this.path = path;
        }

        public Stream<Token> tokens() {
            final Pattern pattern = Type.pattern();
            final Matcher matcher = pattern.matcher(this.path);
            List<Token> tokens = new ArrayList<>(0);
            while (matcher.find()) {
                Type type = this.type(matcher);
                final String value = matcher.group();
                tokens.add(new Token(type, value));
            }
            return tokens.stream();
        }

        private Type type(final Matcher matcher) {
            for (final Type type : Type.values()) {
                if (matcher.group(type.name()) != null) {
                    return type;
                }
            }
            throw new IllegalStateException("Unknown token type");
        }

    }

    @ToString
    @EqualsAndHashCode
    private class Token {
        private final Type type;
        private final String lexeme;

        public Token(final Type type, final String lexeme) {
            this.type = type;
            this.lexeme = lexeme;
        }

        public String lexeme() {
            return this.lexeme;
        }
    }

    private enum Type {

        SLASH("/"),

        NAME("[a-zA-Z_][a-zA-Z0-9_]+");
        private final String lexeme;

        Type(final String lexeme) {
            this.lexeme = lexeme;
        }

        public String lexeme() {
            return this.lexeme;
        }

        static Pattern pattern() {
            return Pattern.compile(Arrays.stream(Type.values())
                .map((a) -> String.format("(?<%s>%s)", a.name(), a.lexeme()))
                .collect(Collectors.joining("|")));
        }
    }

}
