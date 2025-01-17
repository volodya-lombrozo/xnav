package com.github.lombrozo.xnav;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * XML navigator.
 * Allows navigating through an XML document.
 * @since 0.1
 */
@ToString
@EqualsAndHashCode
public final class Navigator {

    /**
     * Actual XML document node.
     */
    private final Xml node;

    /**
     * Ctor.
     * @param join XML document as a string.
     */
    public Navigator(final String join) {
        this(new Xml(join));
    }

    /**
     * Ctor.
     * @param xml XML document node.
     */
    public Navigator(final Xml xml) {
        this.node = xml;
    }

    /**
     * Get a child node by its name.
     * @param element Element name.
     * @return Navigator for the child.
     */
    public Navigator child(final String element) {
        return new Navigator(this.node.child(element));
    }

    /**
     * Get an attribute by its name.
     * @param name Attribute name.
     * @return Navigator for the attribute.
     */
    public Navigator attribute(final String name) {
        return new Navigator(this.node.attribute(name));
    }

    /**
     * Get the text of the current node.
     * @return Text of the node.
     */
    public Optional<String> text() {
        return this.node.text();
    }
}
