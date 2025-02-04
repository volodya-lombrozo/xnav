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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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

    /**
     * Where to start.
     */
    private final Xml root;

    /**
     * The path.
     * This is the reduced version of the XPath.
     */
    private final String path;

    /**
     * Constructor.
     *
     * @param root The root XML document.
     * @param path The reduced version of the XPath.
     */
    Xpath(final Xml root, final String path) {
        this.path = path;
        this.root = root;
    }

    /**
     * Find nodes that match the XPath.
     *
     * @return The nodes that match the XPath.
     */
    Stream<Xml> nodes() {
        return new XPathParser(new XPathLexer(this.path).tokens())
            .parsePath()
            .nodes(Stream.of(this.root));
    }

    /**
     * XPath parser.
     *
     * @since 0.1
     */
    private final class XPathParser {

        /**
         * Tokens.
         */
        private final List<Token> tokens;

        /**
         * Current position.
         */
        private int pos;

        /**
         * Constructor.
         *
         * @param tokens Tokens.
         */
        private XPathParser(final Stream<Token> tokens) {
            this.tokens = tokens.collect(Collectors.toList());
            this.pos = 0;
        }

        /**
         * Parse path.
         *
         * @return Relative path.
         */
        XpathNode parsePath() {
            XpathNode step = x -> x;
            while (!this.eof()) {
                final Token current = this.peek();
                if (current.type == Type.SLASH) {
                    this.consume(Type.SLASH);
                    step = new SeqPath(step, this.parseStep());
                } else if (current.type == Type.NAME) {
                    step = new SeqPath(step, this.parseStep());
                } else if (current.type == Type.DSLASH) {
                    this.consume(Type.DSLASH);
                    step = new SeqPath(step, new RecursivePath(this.parsePath()));
                } else if (current.type == Type.LPAREN) {
                    step = new SeqPath(step, this.parseParenthesizedPath());
                } else {
                    break;
                }
            }
            return new Logged(step);
        }

        /**
         * Parse the path in parentheses.
         *
         * @return Path in parentheses.
         */
        private XpathNode parseParenthesizedPath() {
            this.consume(Type.LPAREN);
            XpathNode step = this.parsePath();
            this.consume(Type.RPAREN);
            if (this.peek().type == Type.LBRACKET) {
//              TODO: Code duplication with the parsePredicatedStep method
                this.consume(Type.LBRACKET);
                if (this.peek().type == Type.NUMBER) {
                    step = new Logged(
                        new NumberExpression(
                            Integer.parseInt(this.consume(Type.NUMBER).lexeme()),
                            step
                        )
                    );
                } else {
                    step = new Logged(new Predicated(step, this.parseExpression()));
                }
                this.consume(Type.RBRACKET);
            }
            return step;
        }

        private XpathNode parsePredicatedStep() {
            final Token token = this.consume();
            XpathNode step = new Logged(new Step(token.lexeme()));
            while (!this.eof() && this.peek().type == Type.LBRACKET) {
                this.consume(Type.LBRACKET);
                if (this.peek().type == Type.NUMBER) {
                    step = new Logged(
                        new NumberExpression(
                            Integer.parseInt(this.consume(Type.NUMBER).lexeme()),
                            step
                        )
                    );
                } else {
                    step = new Logged(new Predicated(step, this.parseExpression()));
                }
                this.consume(Type.RBRACKET);
            }
            return step;
        }

        /**
         * Parse the next step.
         *
         * @return Next step.
         */
        XpathNode parseStep() {
            final XpathNode result;
            final Token current = this.peek();
            if (current.type == Type.NAME) {
                result = this.parsePredicatedStep();
            } else if (current.type == Type.AT) {
                this.consume();
                result = new Logged(new Attribute(this.consume().text));
            } else {
                throw new IllegalStateException(
                    String.format("Expected one more step, but got %s", current)
                );
            }
            return result;
        }

        private XpathFunction parseExpression() {
            XpathFunction left = this.parseSingleExpression();
            while (!this.eof()) {
                final Type current = peek().type;
                if (current == Type.AND || current == Type.OR) {
                    final Token token = this.consume();
                    if (token.type == Type.AND) {
                        left = new AndExpression(left, this.parseExpression());
                    } else if (token.type == Type.OR) {
                        left = new OrExpression(left, this.parseExpression());
                    } else {
                        throw new IllegalStateException(
                            String.format("Expected AND or OR, but got %s", token)
                        );
                    }
                } else {
                    break;
                }
            }
            return left;
        }

        private XpathFunction parseSingleExpression() {
            final Token current = this.peek();
            if (current.type == Type.AT) {
                this.consume();
                return this.parseAttributeExpression();
            } else if (current.type == Type.NAME) {
                return this.parseClause();
            } else if (current.type == Type.LPAREN) {
                this.consume();
                XpathFunction expr = this.parseExpression();
                this.consume();
                return expr;
            } else {
                throw new IllegalStateException(
                    String.format("Expected number, but got %s", current)
                );
            }
        }

        private XpathFunction parseClause() {
            if (this.peek().type == Type.NAME) {
                if (this.tokens.get(this.pos + 1).type == Type.LPAREN) {
                    return this.parseFunction();
                } else {
                    return this.parseSubExpression();
                }
            } else {
                throw new IllegalStateException(
                    String.format("Expected name, but got %s", this.peek())
                );
            }
        }

        private XpathFunction parseSubExpression() {
            return new SubpathExpression(this.parsePath());
        }

        private XpathFunction parseFunction() {
            final Token token = this.consume();
            final String name = token.text;
            final XpathFunction result;
            if ("text".equals(name)) {
                this.consume(); // Consume '('
                this.consume(); // Consume ')'
                result = new Text();
            } else if ("not".equals(name)) {
                this.consume(); // Consume '('
                final XpathFunction original = this.parseExpression();
                this.consume(); // Consume ')'
                result = new Not(original);
            } else if ("string-length".equals(name)) {
                this.consume(); // Consume '('
                final XpathFunction arg = this.parseExpression();
                this.consume(); // Consume ')'
                result = new StringLength(arg);
            } else if ("normalize-space".equals(name)) {
                this.consume(); // Consume '('
                final XpathFunction arg = this.parseExpression();
                this.consume(); // Consume ')'
                result = new NormalizeSpace(arg);
            } else {
                throw new IllegalStateException(
                    String.format("Unknown function '%s'", name)
                );
            }
            if (this.peek().type == Type.EQUALS) {
                return this.parseEqualityExpression(result);
            } else if (this.peek().type == Type.GT) {
                return this.parseGreaterThanExpression(result);
            } else if (this.peek().type == Type.LT) {
                return this.parseLessThanExpression(result);
            } else {
                return result;
            }
        }

        private XpathFunction parseLessThanExpression(final XpathFunction result) {
            final Token lt = this.consume();
            if (lt.type == Type.LT) {
                if (this.peek().type == Type.NUMBER) {
                    final Token value = this.consume();
                    return new LessThanExpression(result, Integer.parseInt(value.text));
                } else {
                    throw new IllegalStateException(
                        String.format("Expected number, but got %s", lt)
                    );
                }
            } else {
                throw new IllegalStateException(
                    String.format("Expected '<', but got %s", lt)
                );
            }
        }

        private XpathFunction parseGreaterThanExpression(final XpathFunction result) {
            final Token gt = this.consume();
            if (gt.type == Type.GT) {
                if (this.peek().type == Type.NUMBER) {
                    final Token value = this.consume();
                    return new GreaterThanExpression(result, Integer.parseInt(value.text));
                } else {
                    throw new IllegalStateException(
                        String.format("Expected number, but got %s", gt)
                    );
                }
            } else {
                throw new IllegalStateException(
                    String.format("Expected '>', but got %s", gt)
                );
            }
        }

        private XpathFunction parseEqualityExpression(XpathFunction original) {
            final Token eq = this.consume();// consume '='
            if (eq.type == Type.EQUALS) {
                final Token value = this.consume();
                if (value.type == Type.VALUE) {
                    final String substring = value.text.substring(1, value.text.length() - 1);
                    return new EqualityExpression(
                        original,
                        substring
                    );
                } else if (value.type == Type.NUMBER) {
                    return new EqualityExpression(
                        original,
                        Integer.parseInt(value.text)
                    );
                } else {
                    throw new IllegalStateException(
                        String.format("Expected a value or a number, but got %s", value)
                    );
                }
            } else {
                throw new IllegalStateException(
                    String.format("Expected '=', but got %s", eq)
                );
            }
        }

        private XpathFunction parseAttributeExpression() {
            final Token consumed = this.consume();
            if (this.eof() || this.tokens.get(this.pos).type != Type.EQUALS) {
                return new AttributeExpession(consumed.text);
            }
            this.consume();
            final Token value = this.consume();
            if (value.type != Type.VALUE) {
                throw new IllegalStateException(
                    String.format("Expected value, but got %s", value)
                );
            }
            return new AttributeEqualityExperssion(
                consumed.text,
                value.text.substring(1, value.text.length() - 1)
            );
        }

        /**
         * Consume the next token with expected type.
         *
         * @param type Type of the token.
         * @return Consumed token.
         */
        private Token consume(Type type) {
            final Token consumed = this.consume();
            if (consumed.type != type) {
                throw new IllegalStateException(
                    String.format("Expected %s, but got %s", type, consumed)
                );
            }
            return consumed;
        }

        /**
         * Consume next token.
         *
         * @return Next token.
         */
        private Token consume() {
            final Token token = this.tokens.get(this.pos);
            this.pos = this.pos + 1;
            return token;
        }

        /**
         * Peek at the current token.
         *
         * @return Current token.
         */
        private Token peek() {
            return this.tokens.get(this.pos);
        }

        /**
         * Check if the end of the tokens.
         *
         * @return True if the end of the tokens.
         */
        private boolean eof() {
            return this.pos >= this.tokens.size();
        }
    }

    private static final class SeqPath implements XpathNode {

        private final XpathNode first;
        private final XpathNode next;

        public SeqPath(final XpathNode first, final XpathNode next) {
            this.first = first;
            this.next = next;
        }


        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            return this.next.nodes(this.first.nodes(xml));
        }
    }

    /**
     * Relative path.
     * This is a sequence of steps.
     *
     * @since 0.1
     */
    @EqualsAndHashCode
    private static final class SequentialPath implements XpathNode {

        /**
         * Steps.
         */
        private final List<XpathNode> steps;

        /**
         * Constructor.
         *
         * @param steps Steps.
         */
        private SequentialPath(final List<XpathNode> steps) {
            this.steps = steps;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            return this.steps.stream().reduce(
                xml,
                (acc, step) -> step.nodes(acc),
                Stream::concat
            );
        }


        @Override
        public String toString() {
            return "sec:" + this.steps.stream().map(Object::toString)
                .collect(Collectors.joining("/"));
        }

    }

    private static final class RecursivePath implements XpathNode {

        private final XpathNode step;

        private RecursivePath(final XpathNode step) {
            this.step = step;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            //todo: dirty hack
            AtomicInteger integer = new AtomicInteger(0);
            Map<Xml, Integer> ordered = xml.flatMap(this::recursive)
                .collect(Collectors.toMap(x -> x, x -> integer.incrementAndGet()));
            final List<Xml> found = ordered.keySet().stream()
                .flatMap(x -> this.step.nodes(Stream.of(x)))
                .collect(Collectors.toList());
            found.sort(Comparator.comparingInt(ordered::get));
            return found.stream();
        }

        private Stream<Xml> recursive(final Xml x) {
            return Stream.concat(
                Stream.of(x),
                x.children().flatMap(this::recursive)
            );
        }

//        @Override
//        public Stream<Xml> nodes(final Stream<Xml> xmls) {
//            return xmls.flatMap(this::recursive);
//        }
//
//        private Stream<Xml> recursive(final Xml x) {
//            return Stream.concat(
//                this.step.nodes(Stream.of(x)),
//                x.children().flatMap(this::recursive)

//            );


//        }


        @Override
        public String toString() {
            return "rec://" + this.step;
        }
    }

    private static class Predicated implements XpathNode {

        private final XpathNode original;
        private final XpathFunction predicate;

        public Predicated(final XpathNode original, final XpathFunction predicate) {
            this.original = original;
            this.predicate = predicate;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            return this.original.nodes(xml).filter(x -> (boolean) this.predicate.execute(x));
        }

        @Override
        public String toString() {
            return original + "[" + this.predicate + "]";
        }
    }


    /**
     * Step node.
     * This is a step in the XPath.
     *
     * @since 0.1
     */
    @EqualsAndHashCode
    private static final class Step implements XpathNode {

        /**
         * Step name.
         */
        private final String name;

        /**
         * Constructor.
         *
         * @param name Step name.
         */
        private Step(final String name) {
            this.name = name;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            return xml
                .flatMap(Xml::children)
                .filter(Filter.withName(this.name));
        }

        @Override
        public String toString() {
            return "step:" + this.name;
        }
    }

    /**
     * Number expression.
     * This is a number in the XPath.
     *
     * @since 0.1
     */
    private static final class NumberExpression implements XpathNode {

        /**
         * Index starting from 1.
         */
        private final int index;

        private final XpathNode original;

        /**
         * Constructor.
         *
         * @param number Index starting from 1.
         */
        public NumberExpression(final int index, final XpathNode original) {
            this.index = index;
            this.original = original;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            return this.original.nodes(xml).skip(this.index - 1).findFirst().stream();
        }

        @Override
        public String toString() {
            return original + "[" + this.index + "]";
        }
    }

    /**
     * Attribute node.
     *
     * @since 0.1
     */
    @ToString
    @EqualsAndHashCode
    private static final class Attribute implements XpathNode {

        /**
         * Attribute name.
         */
        private final String name;

        /**
         * Constructor.
         *
         * @param name Attribute name.
         */
        private Attribute(final String name) {
            this.name = name;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            return xml.map(x -> x.attribute(this.name))
                .filter(Optional::isPresent)
                .map(Optional::get);
        }
    }


    /**
     * Interface for a node in the XPath.
     *
     * @since 0.1
     */
    private interface XpathNode {

        /**
         * Find nodes that match the XPath.
         *
         * @param xml The XML document.
         * @return XML nodes that match the XPath.
         */
        Stream<Xml> nodes(Stream<Xml> xml);
    }

    private class AttributeExpession implements XpathFunction {

        private final String name;

        public AttributeExpession(final String name) {
            this.name = name;
        }

        @Override
        public Object execute(final Xml xml) {
            return xml.attribute(this.name).isPresent();
        }

        @Override
        public String toString() {
            return "@" + this.name;
        }
    }

    private interface XpathFunction {

        Object execute(Xml xml);

    }

    private class SubpathExpression implements XpathFunction {

        private final XpathNode subpath;

        public SubpathExpression(final XpathNode subpath) {
            this.subpath = subpath;
        }

        @Override
        public Object execute(final Xml xml) {
            return this.subpath.nodes(Stream.of(xml)).findFirst().isPresent();
        }

        @Override
        public String toString() {
            return this.subpath.toString();
        }
    }

    private class Not implements XpathFunction {

        private final XpathFunction original;

        public Not(final XpathFunction original) {
            this.original = original;
        }

        @Override
        public Object execute(final Xml xml) {
            return !((boolean) this.original.execute(xml));
        }
    }

    private class StringLength implements XpathFunction {

        private final XpathFunction original;

        public StringLength(final XpathFunction original) {
            this.original = original;
        }

        @Override
        public Object execute(final Xml xml) {
            return String.valueOf(this.original.execute(xml)).length();
        }
    }

    private class NormalizeSpace implements XpathFunction {

        private final XpathFunction original;

        public NormalizeSpace(final XpathFunction original) {
            this.original = original;
        }

        @Override
        public Object execute(final Xml xml) {
            return String.valueOf(this.original.execute(xml)).trim().replaceAll(" +", " ");
        }
    }

    private class Text implements XpathFunction {

        @Override
        public String execute(final Xml xml) {
            return xml.text().orElseThrow(
                () -> new IllegalStateException("Text not found")
            );
        }
    }

    private class EqualityExpression implements XpathFunction {

        private final XpathFunction function;
        private final Object value;

        public EqualityExpression(final XpathFunction function, final Object value) {
            this.function = function;
            this.value = value;
        }

        @Override
        public Object execute(final Xml xml) {
            final Object execute = this.function.execute(xml);
            return execute.equals(this.value);
        }
    }

    private class GreaterThanExpression implements XpathFunction {

        private final XpathFunction left;
        private final int right;

        public GreaterThanExpression(final XpathFunction left, final int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Object execute(final Xml xml) {
            return (int) this.left.execute(xml) > this.right;
        }
    }

    private class LessThanExpression implements XpathFunction {

        private final XpathFunction left;
        private final int right;

        public LessThanExpression(final XpathFunction left, final int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Object execute(final Xml xml) {
            return (int) this.left.execute(xml) < right;
        }
    }

    private class AttributeEqualityExperssion implements XpathFunction {

        private final String attribute;
        private final String value;

        public AttributeEqualityExperssion(final String attribute, final String value) {
            this.attribute = attribute;
            this.value = value;
        }

        @Override
        public Object execute(final Xml xml) {
            return xml.attribute(this.attribute)
                .map(Xml::text)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(v -> v.equals(this.value))
                .orElse(false);
        }
    }

    private static class AndExpression implements XpathFunction {

        private final XpathFunction left;
        private final XpathFunction right;

        public AndExpression(final XpathFunction left, final XpathFunction right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Object execute(final Xml xml) {
            return (boolean) this.left.execute(xml) && (boolean) this.right.execute(xml);
        }
    }

    private static class OrExpression implements XpathFunction {

        private final XpathFunction left;
        private final XpathFunction right;

        public OrExpression(final XpathFunction left, final XpathFunction right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Object execute(final Xml xml) {
            return (boolean) this.left.execute(xml) || (boolean) this.right.execute(xml);
        }
    }

    /**
     * XPath lexer.
     * It is used to tokenize the XPath.
     *
     * @since 0.1
     */
    private static final class XPathLexer {

        /**
         * Lexer pattern.
         */
        private static final Pattern PATTERN = Type.pattern();

        /**
         * String to tokenize.
         */
        private final String path;

        /**
         * Trace flag.
         */
        private final boolean trace;

        /**
         * Constructor.
         *
         * @param path String to tokenize.
         */
        private XPathLexer(final String path) {
            this.path = path;
            this.trace = false;
        }

        /**
         * Tokenize the string.
         *
         * @return Tokens.
         */
        Stream<Token> tokens() {
            final Matcher matcher = XPathLexer.PATTERN.matcher(this.path);
            final List<Token> tokens = new ArrayList<>(0);
            while (matcher.find()) {
                final Type type = XPathLexer.type(matcher);
                final String value = matcher.group();
                tokens.add(new Token(type, value, matcher.start()));
            }
            if (this.trace) {
                tokens.forEach(System.out::println);
            }
            return tokens.stream();
        }

        /**
         * Understand the token type.
         *
         * @param matcher Matcher.
         * @return Token type.
         */
        private static Type type(final Matcher matcher) {
            for (final Type type : Type.values()) {
                if (matcher.group(type.name()) != null) {
                    return type;
                }
            }
            throw new IllegalStateException(
                String.format("Unknown token type for %s", matcher.group())
            );
        }

    }

    private static class Logged implements XpathNode {

        private final XpathNode origin;
        private static final AtomicInteger tabs = new AtomicInteger(0);
        private static final Deque<String> stack = new ArrayDeque<>(0);

        public Logged(final XpathNode origin) {
            this.origin = origin;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            return this.origin.nodes(xml);
//            this.print("{ '%s'", path());
//            final List<Xml> collect = xml.collect(Collectors.toList());
//            collect.stream().map(Objects::toString).map(Logged::clean).forEach(this::print);
//            inc();
//            final List<Xml> after = origin.nodes(collect.stream()).collect(Collectors.toList());
//            dec();
//            if (!after.isEmpty()) {
//                print("______");
//            }
//            after.stream().map(Objects::toString).map(Logged::clean).forEach(this::print);
//            this.print("'%s' }", path());
//            return after.stream();
        }

        private String path() {
            return this.origin.toString();
        }

        private void inc() {
            Logged.tabs.incrementAndGet();
            stack.push(this.origin.toString());
        }

        private void dec() {
            Logged.tabs.decrementAndGet();
            stack.pop();
        }


        private static String clean(String str) {
            return str.replaceAll("\n", "").replaceAll("\r", "").trim();
        }

        private void print(final String message, Object... args) {
            if (false) {
                System.out.println(" ".repeat(tabs.get()) + String.format(message, args));
            }
        }

        @Override
        public String toString() {
            return this.origin.toString();
        }
    }

    /**
     * Token.
     * This is a token for the XPath lexer.
     *
     * @since 0.1
     */
    @ToString
    @EqualsAndHashCode
    private static final class Token {

        /**
         * Token type.
         */
        private final Type type;

        /**
         * Lexeme.
         */
        private final String text;

        /**
         * Position.
         */
        private final int position;

        /**
         * Constructor.
         *
         * @param type Type of the token.
         * @param lexeme Lexeme of the token.
         * @param position Position of the token.
         */
        private Token(final Type type, final String lexeme, final int position) {
            this.type = type;
            this.text = lexeme;
            this.position = position;
        }

        /**
         * Get the lexeme.
         *
         * @return The lexeme.
         */
        String lexeme() {
            return this.text;
        }
    }

    /**
     * Token types.
     * This is the token types for the XPath lexer.
     *
     * @since 0.1
     */
    private enum Type {

        /**
         * Double slash.
         */
        DSLASH("//"),

        /**
         * Slash.
         */
        SLASH("/"),

        /**
         * At.
         */
        AT("@"),

        /**
         * Left parenthesis.
         */
        LPAREN("\\("),

        /**
         * Right parenthesis.
         */
        RPAREN("\\)"),

        /**
         * Left bracket.
         */
        LBRACKET("\\["),

        /**
         * Right bracket.
         */
        RBRACKET("\\]"),

        /**
         * Number.
         */
        NUMBER("[0-9]+"),

        /**
         * Less than sign.
         */
        LT("\\<"),

        /**
         * Greater than sign.
         */
        GT("\\>"),

        /**
         * Equals sign.
         */
        EQUALS("="),

        /**
         * Quoted value.
         */
        VALUE("'[^']*'|\"[^\"]*\""),

        /**
         * And operator.
         */
        AND("and|AND"),

        /**
         * Or operator.
         */
        OR("or|OR"),

        /**
         * Name.
         */
        NAME("[a-zA-Z_][a-zA-Z0-9_-]*");

        /**
         * Token pattern.
         */
        private final String subpattern;

        /**
         * Constructor.
         *
         * @param pattern Token pattern.
         */
        Type(final String pattern) {
            this.subpattern = pattern;
        }

        /**
         * Get the lexeme.
         *
         * @return The lexeme.
         */
        String lexeme() {
            return this.subpattern;
        }

        /**
         * Get the pattern.
         *
         * @return The pattern.
         */
        private static Pattern pattern() {
            return Pattern.compile(
                Arrays.stream(Type.values())
                    .map(a -> String.format("(?<%s>%s)", a.name(), a.lexeme()))
                    .collect(Collectors.joining("|"))
            );
        }
    }
}
