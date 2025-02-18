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

import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOM implementation of XML.
 * XML abstraction over an XML document.
 * This class is thread-safe.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
final class DomXml implements Xml {

    /**
     * Transformer factory.
     */
    private static final TransformerFactory TFACTORY = TransformerFactory.newInstance();

    /**
     * Actual XML document node.
     */
    private final Node inner;

    private final Object sync;

    /**
     * Ctor.
     *
     * @param xml XML document as a string.
     */
    DomXml(final String xml) {
        this(new StringNode(xml).toNode());
    }

    /**
     * Ctor.
     *
     * @param node XML document node.
     */
    DomXml(final Node node) {
        this(node, new Object());
    }

    /**
     * Constructor.
     * @param inner Inner node.
     * @param sync Synchronization object.
     */
    public DomXml(final Node inner, final Object sync) {
        this.inner = inner;
        this.sync = sync;
    }

    @Override
    public Xml child(final String element) {
        synchronized (this.syn()) {
            final NodeList nodes = this.inner.getChildNodes();
            final int length = nodes.getLength();
            Xml res = new Empty();
            for (int idx = 0; idx < length; ++idx) {
                final Node child = nodes.item(idx);
                if (child.getNodeType() == Node.ELEMENT_NODE
                    && child.getNodeName().equals(element)) {
                    res = new DomXml(child, this.sync);
                    break;
                }
            }
            return res;
        }
    }

    @Override
    public Optional<Xml> attribute(final String name) {
        synchronized (this.syn()) {
            final NamedNodeMap attributes = this.inner.getAttributes();
            final Optional<Xml> result;
            if (Objects.isNull(attributes)) {
                result = Optional.empty();
            } else {
                final Node item = attributes.getNamedItem(name);
                if (Objects.nonNull(item)) {
                    result = Optional.of(new DomXml(item, this.sync));
                } else {
                    result = Optional.empty();
                }
            }
            return result;
        }
    }

    @Override
    public Optional<String> text() {
        synchronized (this.syn()) {
            final Optional<String> result;
            if (this.inner.getNodeType() == Node.DOCUMENT_NODE) {
                result = Optional.ofNullable(this.inner.getFirstChild().getTextContent());
            } else if (this.inner.getNodeType() == Node.ATTRIBUTE_NODE) {
                result = Optional.of(this.inner.getNodeValue());
            } else {
                result = Optional.of(this.inner).map(Node::getTextContent);
            }
            return result;
        }
    }

    @Override
    public Stream<Xml> children() {
        synchronized (this.syn()) {
            final Stream<Xml> result;
            if (this.inner.getNodeType() == Node.ATTRIBUTE_NODE) {
                result = Stream.empty();
            } else {
                final NodeList nodes = this.inner.getChildNodes();
                final int length = nodes.getLength();
                result = Stream.iterate(0, idx -> idx + 1)
                    .limit(length)
                    .map(nodes::item)
                    .filter(Objects::nonNull)
                    .map(node -> new DomXml(node, this.sync));
            }
            return result;
        }
    }

    @Override
    public String name() {
        synchronized (this.syn()) {
            return Optional.ofNullable(this.inner).map(Node::getNodeName).orElse("");
        }
    }

    @Override
    public DomXml copy() {
        return new DomXml(this.inner.cloneNode(true), this.sync);
    }

    @Override
    public Node node() {
        return this.inner;
    }

    @Override
    public String toString() {
        synchronized (this.syn()) {
            try {
                final Transformer transformer = DomXml.TFACTORY.newTransformer();
                transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                if (!(this.inner instanceof Document)) {
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                }
                final StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(this.inner), new StreamResult(writer));
                return writer.toString();
            } catch (final TransformerConfigurationException econf) {
                throw new IllegalStateException(
                    String.format(
                        "Failed to configure Transformer for printing XML to a string: %s", this
                    ),
                    econf
                );
            } catch (final TransformerException exception) {
                throw new IllegalStateException(
                    String.format("Failed to transform XML: %s to a human-readable string", this),
                    exception
                );
            }
        }
    }

    @Override
    public boolean equals(final Object obj) {
        synchronized (this.syn()) {
            final boolean result;
            if (this == obj) {
                result = true;
            } else if (obj == null || getClass() != obj.getClass()) {
                result = false;
            } else {
                final DomXml other = (DomXml) obj;
                result = this.inner.isEqualNode(other.inner);
            }
            return result;
        }
    }

    @Override
    public int hashCode() {
        synchronized (this.syn()) {
            return Objects.hashCode(this.inner);
        }
    }

    /**
     * Synchronize target.
     * @return Target to synchronize.
     */
    private Object syn() {
        return this.sync;
    }

}
