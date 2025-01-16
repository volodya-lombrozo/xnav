package com.github.lombrozo.xnav;

import java.util.Objects;
import java.util.Optional;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML navigator.
 * Allows navigating through an XML document.
 * @since 0.33
 */
public final class XmlNavigator implements Navigator {


    /**
     * Actual XML document node.
     */
    private final Node node;


    /**
     * Ctor.
     * @param xml XML document node.
     */
    XmlNavigator(final Node xml) {
        this.node = xml;
    }

    public XmlNavigator(final String join) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Navigator child(final String element) {
        final NodeList nodes = this.node.getChildNodes();
        for (int idx = 0; idx < nodes.getLength(); ++idx) {
            final Node child = nodes.item(idx);
            if (child.getNodeType() == Node.ELEMENT_NODE
                && child.getNodeName().equals(element)) {
                return new XmlNavigator(child);
            }
        }
        throw new IllegalStateException(
            String.format("Element '%s' not found in '%s'", element, this)
        );
    }

    @Override
    public Navigator attribute(final String name) {
        final Node item = this.node.getAttributes().getNamedItem(name);
        if (Objects.nonNull(item)) {
            return new XmlNavigator(item);
        }
        throw new IllegalStateException(
            String.format("Attribute '%s' not found in '%s'", name, this)
        );
    }

    @Override
    public Optional<String> text() {
        final Optional<String> result;
        if (this.node.getNodeType() == Node.DOCUMENT_NODE) {
            result = Optional.of("");
        } else {
            result = Optional.of(this.node).map(Node::getTextContent);
        }
        return result;
    }

    @Override
    public String toString() {
        return "";
    }
}
