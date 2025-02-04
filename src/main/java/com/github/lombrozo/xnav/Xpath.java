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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
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
 * @todo #39:60min Refactor the Xpath class.
 *  The Xpath class is too big and has too many responsibilities.
 *  We should refactor it to have a better design.
 *  Don't forget to remove the PMD suppression statements.
 *  Also there are some suppression statements in pom.xml
 * @checkstyle FileLengthCheck (2000 lines)
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
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
    private static final class XPathParser {

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
                    step = new Sequence(step, this.parseStep());
                } else if (current.type == Type.NAME) {
                    step = new Sequence(step, this.parseStep());
                } else if (current.type == Type.DSLASH) {
                    this.consume(Type.DSLASH);
                    step = new Sequence(step, new RecursivePath(this.parsePath()));
                } else if (current.type == Type.LPAREN) {
                    step = new Sequence(step, this.parseParenthesizedPath());
                } else {
                    break;
                }
            }
            return step;
        }

        /**
         * Parse the path in parentheses.
         *
         * @return Path in parentheses.
         */
        private XpathNode parseParenthesizedPath() {
            this.consume(Type.LPAREN);
            final XpathNode step = this.parsePath();
            this.consume(Type.RPAREN);
            return this.parsePredicate(step);
        }

        /**
         * Parse the next step with a predicate.
         *
         * @return Step with a predicate.
         */
        private XpathNode parsePredicatedStep() {
            return this.parsePredicate(new Step(this.consume().lexeme()));
        }

        /**
         * Parse the predicate.
         *
         * @param target Predicate target.
         * @return Parsed predicate.
         */
        private XpathNode parsePredicate(final XpathNode target) {
            XpathNode res = target;
            while (!this.eof() && this.peek().type == Type.LBRACKET) {
                this.consume(Type.LBRACKET);
                if (this.peek().type == Type.NUMBER) {
                    res = new NumberExpression(
                        res, Integer.parseInt(this.consume(Type.NUMBER).lexeme())
                    );
                } else {
                    res = new Predicated(res, this.parseExpression());
                }
                this.consume(Type.RBRACKET);
            }
            return res;
        }

        /**
         * Parse the next step.
         *
         * @return Next step.
         */
        private XpathNode parseStep() {
            final XpathNode result;
            final Token current = this.peek();
            if (current.type == Type.NAME) {
                result = this.parsePredicatedStep();
            } else if (current.type == Type.AT) {
                this.consume(Type.AT);
                result = new Attribute(this.consume().text);
            } else {
                throw new IllegalStateException(
                    String.format("Expected one more step, but got %s", current)
                );
            }
            return result;
        }

        /**
         * Parse the expression.
         *
         * @return Parsed expression.
         */
        private XpathFunction parseExpression() {
            XpathFunction left = this.parseSingleExpression();
            while (!this.eof()) {
                final Type current = this.peek().type;
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

        /**
         * Parse the single expression.
         *
         * @return Parsed single expression.
         */
        private XpathFunction parseSingleExpression() {
            final XpathFunction result;
            final Token current = this.peek();
            if (current.type == Type.AT) {
                this.consume(Type.AT);
                result = this.parseAttributeExpression();
            } else if (current.type == Type.NAME) {
                result = this.parseClause();
            } else if (current.type == Type.LPAREN) {
                this.consume(Type.LPAREN);
                final XpathFunction expr = this.parseExpression();
                this.consume(Type.RPAREN);
                result = expr;
            } else {
                throw new IllegalStateException(
                    String.format("Expected number, but got %s", current)
                );
            }
            return result;
        }

        /**
         * Parse the clause.
         *
         * @return Parsed clause.
         */
        private XpathFunction parseClause() {
            final XpathFunction result;
            if (this.peek().type == Type.NAME) {
                if (this.tokens.get(this.pos + 1).type == Type.LPAREN) {
                    result = this.parseFunction();
                } else {
                    result = new SubpathExpression(this.parsePath());
                }
            } else {
                throw new IllegalStateException(
                    String.format("Expected name, but got %s", this.peek())
                );
            }
            return result;
        }

        /**
         * Parse the function.
         *
         * @return Parsed function.
         */
        private XpathFunction parseFunction() {
            final Token token = this.consume();
            final String name = token.text;
            final XpathFunction function;
            if ("text".equals(name)) {
                this.consume(Type.LPAREN);
                this.consume(Type.RPAREN);
                function = new Text();
            } else if ("not".equals(name)) {
                this.consume(Type.LPAREN);
                final XpathFunction original = this.parseExpression();
                this.consume(Type.RPAREN);
                function = new Not(original);
            } else if ("string-length".equals(name)) {
                this.consume(Type.LPAREN);
                final XpathFunction arg = this.parseExpression();
                this.consume(Type.RPAREN);
                function = new StringLength(arg);
            } else if ("normalize-space".equals(name)) {
                this.consume(Type.LPAREN);
                final XpathFunction arg = this.parseExpression();
                this.consume(Type.RPAREN);
                function = new NormalizeSpace(arg);
            } else {
                throw new IllegalStateException(
                    String.format("Unknown function '%s'", name)
                );
            }
            final XpathFunction res;
            if (this.peek().type == Type.EQUALS) {
                res = this.parseEqExpression(function);
            } else if (this.peek().type == Type.GT) {
                res = this.parseGtExpression(function);
            } else if (this.peek().type == Type.LT) {
                res = this.parseLtExpression(function);
            } else {
                res = function;
            }
            return res;
        }

        /**
         * Parse less than expression.
         *
         * @param result Function to compare
         * @return Less than expression
         */
        private XpathFunction parseLtExpression(final XpathFunction result) {
            final XpathFunction res;
            final Token less = this.consume(Type.LT);
            if (less.type == Type.LT) {
                res = new LtExpression(result, Integer.parseInt(this.consume(Type.NUMBER).text));
            } else {
                throw new IllegalStateException(
                    String.format("Expected '<', but got %s", less)
                );
            }
            return res;
        }

        /**
         * Parse greater than expression.
         *
         * @param result Function to compare.
         * @return Greater than expression.
         */
        private XpathFunction parseGtExpression(final XpathFunction result) {
            final Token great = this.consume(Type.GT);
            if (great.type == Type.GT) {
                return new GtExpression(
                    result,
                    Integer.parseInt(this.consume(Type.NUMBER).text)
                );
            } else {
                throw new IllegalStateException(
                    String.format("Expected '>', but got %s", great)
                );
            }
        }

        /**
         * Parse equality expression.
         *
         * @param original Function to compare.
         * @return Equality expression.
         */
        private XpathFunction parseEqExpression(final XpathFunction original) {
            this.consume(Type.EQUALS);
            final Token value = this.consume();
            final Object comparable;
            if (value.type == Type.VALUE) {
                comparable = value.text.substring(1, value.text.length() - 1);
            } else if (value.type == Type.NUMBER) {
                comparable = Integer.parseInt(value.text);
            } else {
                throw new IllegalStateException(
                    String.format("Expected a value or a number, but got %s", value)
                );
            }
            return new EqualityExpression(original, comparable);
        }

        /**
         * Parse the attribute expression.
         *
         * @return Parsed attribute expression.
         */
        private XpathFunction parseAttributeExpression() {
            final Token name = this.consume(Type.NAME);
            final XpathFunction result;
            if (this.eof() || this.tokens.get(this.pos).type != Type.EQUALS) {
                result = new AttributeExpession(name.text);
            } else {
                this.consume(Type.EQUALS);
                final Token value = this.consume(Type.VALUE);
                result = new AttributeEqualityExperssion(
                    name.text,
                    value.text.substring(1, value.text.length() - 1)
                );
            }
            return result;
        }

        /**
         * Consume the next token with an expected type.
         *
         * @param type Type of the token.
         * @return Consumed token.
         */
        private Token consume(final Type type) {
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

    /**
     * Sequence of nodes.
     * This is a sequence of nodes in the XPath.
     *
     * @since 0.1
     */
    private static final class Sequence implements XpathNode {

        /**
         * First step.
         */
        private final XpathNode first;

        /**
         * Next step.
         */
        private final XpathNode next;

        /**
         * Constructor.
         *
         * @param first First step.
         * @param next Next step.
         */
        private Sequence(final XpathNode first, final XpathNode next) {
            this.first = first;
            this.next = next;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            return this.next.nodes(this.first.nodes(xml));
        }
    }

    /**
     * Recursive path //.
     *
     * @since 0.1
     */
    private static final class RecursivePath implements XpathNode {

        /**
         * Path to search.
         */
        private final XpathNode subpath;

        /**
         * Constructor.
         *
         * @param path Path to search.
         */
        private RecursivePath(final XpathNode path) {
            this.subpath = path;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            //@checkstyle MethodBodyCommentsCheck (30 lines)
            //@todo #39:90min Travers Recursive Path in a More Efficient Way.
            // Currently we get all the possible nodes that match the path and then
            // we sort them by the order they appear in the document. This is not
            // efficient and we should find a better way to traverse the document.
            final AtomicInteger integer = new AtomicInteger(0);
            final Map<Xml, Integer> ordered = xml.flatMap(this::recursive)
                .collect(Collectors.toMap(x -> x, x -> integer.incrementAndGet()));
            return ordered.keySet().stream()
                .flatMap(x -> this.subpath.nodes(Stream.of(x)))
                .sorted(Comparator.comparingInt(ordered::get));
        }

        @Override
        public String toString() {
            return String.format("rec://%s", this.subpath);
        }

        /**
         * Recursive stream of Xml nodes.
         *
         * @param current Xml node.
         * @return Flat stream of Xml nodes.
         */
        private Stream<Xml> recursive(final Xml current) {
            return Stream.concat(
                Stream.of(current),
                current.children().flatMap(this::recursive)
            );
        }
    }

    /**
     * Predicated node.
     *
     * @since 0.1
     */
    private static final class Predicated implements XpathNode {

        /**
         * Original node.
         */
        private final XpathNode original;

        /**
         * Predicate to apply.
         */
        private final XpathFunction predicate;

        /**
         * Constructor.
         *
         * @param original Xpath node
         * @param predicate Predicate to apply
         */
        private Predicated(final XpathNode original, final XpathFunction predicate) {
            this.original = original;
            this.predicate = predicate;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            return this.original.nodes(xml).filter(x -> (boolean) this.predicate.execute(x));
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", this.original, this.predicate);
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
            return String.format("step:%s", this.name);
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
         * Original node.
         */
        private final XpathNode original;

        /**
         * Index starting from 1.
         */
        private final int index;

        /**
         * Constructor.
         *
         * @param original Original node that will be limited.
         * @param index Index starting from 1.
         */
        private NumberExpression(final XpathNode original, final int index) {
            this.index = index;
            this.original = original;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            return this.original.nodes(xml).skip(this.index - 1).findFirst().stream();
        }

        @Override
        public String toString() {
            return String.format("%s[%d]", this.original, this.index);
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
     * Attribute expression.
     *
     * @since 0.1
     */
    private static final class AttributeExpession implements XpathFunction {

        /**
         * Attribute name.
         */
        private final String name;

        /**
         * Constructor.
         *
         * @param name Attribute name.
         */
        private AttributeExpession(final String name) {
            this.name = name;
        }

        @Override
        public Object execute(final Xml xml) {
            return xml.attribute(this.name).isPresent();
        }

        @Override
        public String toString() {
            return String.format("@%s", this.name);
        }
    }

    /**
     * The traced step.
     * This class is used for debugging purposes.
     * It prints the steps that are being processed with initial and final results.
     *
     * @since 0.1
     */
    private static final class Traced implements XpathNode {

        /**
         * Tabs.
         * Number of tabs to pretty print indentation.
         */
        private static final AtomicInteger TABS = new AtomicInteger(0);

        /**
         * Stack.
         * Stack of the nodes is being processed.
         */
        private static final Deque<String> STACK = new ArrayDeque<>(0);

        /**
         * Original node.
         */
        private final XpathNode origin;

        /**
         * Constructor.
         *
         * @param origin Original node.
         */
        private Traced(final XpathNode origin) {
            this.origin = origin;
        }

        @Override
        public Stream<Xml> nodes(final Stream<Xml> xml) {
            Traced.print("{ '%s'", this.origin.toString());
            final List<Xml> collect = xml.collect(Collectors.toList());
            collect.stream().map(Objects::toString).map(Traced::clean).forEach(Traced::print);
            this.inc();
            final List<Xml> after = this.origin.nodes(collect.stream())
                .collect(Collectors.toList());
            Traced.dec();
            if (!after.isEmpty()) {
                Traced.print("______");
            }
            after.stream().map(Objects::toString).map(Traced::clean).forEach(Traced::print);
            Traced.print("'%s' }", this.origin.toString());
            return after.stream();
        }

        @Override
        public String toString() {
            return this.origin.toString();
        }

        /**
         * Increment the tabs.
         * This is used for pretty print.
         */
        private void inc() {
            Traced.TABS.incrementAndGet();
            Traced.STACK.push(this.origin.toString());
        }

        /**
         * Decrement the tabs.
         * This is used for pretty print.
         */
        private static void dec() {
            Traced.TABS.decrementAndGet();
            Traced.STACK.pop();
        }

        /**
         * Print the message.
         *
         * @param str String to clean.
         * @return Cleaned string.
         */
        private static String clean(final String str) {
            return str.replaceAll("\n", "")
                .replaceAll("\r", "")
                .trim();
        }

        /**
         * Print the message.
         *
         * @param message Message to print.
         * @param args Arguments.
         */
        private static void print(final String message, final Object... args) {
            Logger.getLogger(Traced.class.getSimpleName()).info(
                String.format("%s%s", " ".repeat(Traced.TABS.get()), String.format(message, args))
            );
        }
    }

    /**
     * Xpath function.
     * This is a function that can be applied to the XML node.
     *
     * @since 0.1
     */
    private interface XpathFunction {

        /**
         * Execute the function.
         *
         * @param xml XML node.
         * @return Result of the function.
         */
        Object execute(Xml xml);

    }

    /**
     * Subpath expression.
     * This is a subpath in the XPath.
     *
     * @since 0.1
     */
    private static final class SubpathExpression implements XpathFunction {

        /**
         * Subpath.
         */
        private final XpathNode subpath;

        /**
         * Constructor.
         *
         * @param subpath Subpath.
         */
        private SubpathExpression(final XpathNode subpath) {
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

    /**
     * Not function.
     *
     * @since 0.1
     */
    private static final class Not implements XpathFunction {

        /**
         * Original function.
         */
        private final XpathFunction original;

        /**
         * Constructor.
         *
         * @param original Original function.
         */
        private Not(final XpathFunction original) {
            this.original = original;
        }

        @Override
        public Object execute(final Xml xml) {
            return !((boolean) this.original.execute(xml));
        }
    }

    /**
     * String length function.
     *
     * @since 0.1
     */
    private static final class StringLength implements XpathFunction {

        /**
         * Original function.
         */
        private final XpathFunction original;

        /**
         * Constructor.
         *
         * @param original Original function.
         */
        private StringLength(final XpathFunction original) {
            this.original = original;
        }

        @Override
        public Object execute(final Xml xml) {
            return String.valueOf(this.original.execute(xml)).length();
        }
    }

    /**
     * Normalize space function.
     *
     * @since 0.1
     */
    private static final class NormalizeSpace implements XpathFunction {

        /**
         * Original function.
         */
        private final XpathFunction original;

        /**
         * Constructor.
         *
         * @param original Original function.
         */
        private NormalizeSpace(final XpathFunction original) {
            this.original = original;
        }

        @Override
        public Object execute(final Xml xml) {
            return String.valueOf(this.original.execute(xml)).trim().replaceAll(" +", " ");
        }
    }

    /**
     * Text function.
     *
     * @since 0.1
     */
    private static final class Text implements XpathFunction {

        @Override
        public String execute(final Xml xml) {
            return xml.text().orElseThrow(
                () -> new IllegalStateException("Text not found")
            );
        }
    }

    /**
     * Equality expression.
     *
     * @since 0.1
     */
    private static final class EqualityExpression implements XpathFunction {

        /**
         * Function to compare.
         */
        private final XpathFunction function;

        /**
         * Value to compare.
         */
        private final Object value;

        /**
         * Constructor.
         *
         * @param function Function to compare
         * @param value Value to compare
         */
        private EqualityExpression(final XpathFunction function, final Object value) {
            this.function = function;
            this.value = value;
        }

        @Override
        public Object execute(final Xml xml) {
            return this.function.execute(xml).equals(this.value);
        }
    }

    /**
     * Greater than expression.
     *
     * @since 0.1
     */
    private static final class GtExpression implements XpathFunction {

        /**
         * Left function.
         */
        private final XpathFunction left;

        /**
         * Right value.
         */
        private final int right;

        /**
         * Constructor.
         *
         * @param left Left function
         * @param right Right value
         */
        private GtExpression(final XpathFunction left, final int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Object execute(final Xml xml) {
            return (int) this.left.execute(xml) > this.right;
        }
    }

    /**
     * Less than expression.
     *
     * @since 0.1
     */
    private static final class LtExpression implements XpathFunction {

        /**
         * Left function.
         */
        private final XpathFunction left;

        /**
         * Right value.
         */
        private final int right;

        /**
         * Constructor.
         *
         * @param left Left
         * @param right Right
         */
        private LtExpression(final XpathFunction left, final int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Object execute(final Xml xml) {
            return (int) this.left.execute(xml) < this.right;
        }
    }

    /**
     * Attribute equality expression.
     *
     * @since 0.1
     */
    private static final class AttributeEqualityExperssion implements XpathFunction {

        /**
         * Attribute name.
         */
        private final String attribute;

        /**
         * Value to compare.
         */
        private final String value;

        /**
         * Constructor.
         *
         * @param attribute Attribute name
         * @param value Value to compare
         */
        private AttributeEqualityExperssion(final String attribute, final String value) {
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

    /**
     * And expression.
     *
     * @since 0.1
     */
    private static final class AndExpression implements XpathFunction {

        /**
         * Left function.
         */
        private final XpathFunction left;

        /**
         * Right function.
         */
        private final XpathFunction right;

        /**
         * Constructor.
         *
         * @param left Left
         * @param right Right
         */
        private AndExpression(final XpathFunction left, final XpathFunction right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Object execute(final Xml xml) {
            return (boolean) this.left.execute(xml) && (boolean) this.right.execute(xml);
        }
    }

    /**
     * Or expression.
     *
     * @since 0.1
     */
    private static final class OrExpression implements XpathFunction {

        /**
         * Left function.
         */
        private final XpathFunction left;

        /**
         * Right function.
         */
        private final XpathFunction right;

        /**
         * Constructor.
         *
         * @param left Left
         * @param right Right
         */
        private OrExpression(final XpathFunction left, final XpathFunction right) {
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
                final Logger logger = Logger.getLogger(XPathLexer.class.getSimpleName());
                logger.info("Tokens:");
                tokens.stream().map(Token::toString).forEach(logger::info);
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
