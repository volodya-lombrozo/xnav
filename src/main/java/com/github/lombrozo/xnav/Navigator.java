package com.github.lombrozo.xnav;

import java.util.Optional;

/**
 * XML navigator.
 * Allows navigating through an XML document.
 * @since 0.1
 */
public interface Navigator {

    /**
     * Get a child node by its name.
     * @param element Element name.
     * @return Navigator for the child.
     */
    Navigator child(String element);

    /**
     * Get an attribute by its name.
     * @param name Attribute name.
     * @return Navigator for the attribute.
     */
    Navigator attribute(String name);

    /**
     * Get the text of the current node.
     * @return Text of the node.
     */
    Optional<String> text();
}