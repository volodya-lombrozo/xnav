package com.github.lombrozo.xnav;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * XML navigator.
 * Allows navigating through an XML document.
 * @since 0.33
 */
@ToString
@EqualsAndHashCode
public final class XmlNavigator implements Navigator {


    /**
     * Actual XML document node.
     */
    private final Xml node;

    /**
     * Ctor.
     * @param join XML document as a string.
     */
    public XmlNavigator(final String join) {
        this(new Xml(join));
    }

    /**
     * Ctor.
     * @param xml XML document node.
     */
    public XmlNavigator(final Xml xml) {
        this.node = xml;
    }

    @Override
    public Navigator child(final String element) {
        return new XmlNavigator(this.node.child(element));
    }

    @Override
    public Navigator attribute(final String name) {
        return new XmlNavigator(this.node.attribute(name));
    }

    @Override
    public Optional<String> text() {
        return this.node.text();
    }
}
