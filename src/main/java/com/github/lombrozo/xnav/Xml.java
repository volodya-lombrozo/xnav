package com.github.lombrozo.xnav;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XML abstraction over XML document.
 * @since 0.1
 */
final class Xml {

    /**
     * Document factory.
     */
    private static final DocumentBuilderFactory DFACTORY = DocumentBuilderFactory.newInstance();

    /**
     * Transformer factory.
     */
    private static final TransformerFactory TFACTORY = TransformerFactory.newInstance();

    /**
     * Actual XML document node.
     */
    private final Node node;

    /**
     * Ctor.
     * @param xml XML document as a string.
     */
    Xml(final String xml) {
        this(Xml.fromString(xml));
    }

    /**
     * Ctor.
     * @param node XML document node.
     */
    Xml(final Node node) {
        this.node = node;
    }

    /**
     * Get a child node by its name.
     * @param element Element name.
     * @return child.
     */
    Xml child(final String element) {
        final NodeList nodes = this.node.getChildNodes();
        for (int idx = 0; idx < nodes.getLength(); ++idx) {
            final Node child = nodes.item(idx);
            if (child.getNodeType() == Node.ELEMENT_NODE
                && child.getNodeName().equals(element)) {
                return new Xml(child);
            }
        }
        throw new IllegalStateException(
            String.format("Element '%s' not found in '%s'", element, this)
        );
    }

    /**
     * Get an attribute by its name.
     * @param name Attribute name.
     * @return attribute.
     */
    Xml attribute(final String name) {
        final Node item = this.node.getAttributes().getNamedItem(name);
        if (Objects.nonNull(item)) {
            return new Xml(item);
        }
        throw new IllegalStateException(
            String.format("Attribute '%s' not found in '%s'", name, this)
        );
    }

    /**
     * Get the text of the current node.
     * @return Text of the node.
     */
    Optional<String> text() {
        final Optional<String> result;
        if (this.node.getNodeType() == Node.DOCUMENT_NODE) {
            result = Optional.of("");
        } else {
            result = Optional.of(this.node).map(Node::getTextContent);
        }
        return result;
    }

    /**
     * Create XML node from a string.
     * @param xml XML as a string.
     * @return XML node.
     */
    private static Node fromString(final String xml) {
        try {
            return Xml.DFACTORY.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (final SAXException | IOException | ParserConfigurationException exception) {
            throw new IllegalArgumentException(
                String.format("Failed to parse XML: %s", xml),
                exception
            );
        }
    }

    @Override
    public String toString() {
        try {
            final Transformer transformer = Xml.TFACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            if(!(this.node instanceof Document)){
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(this.node), new StreamResult(writer));
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
