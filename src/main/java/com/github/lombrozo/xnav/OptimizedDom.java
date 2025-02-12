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

import java.util.concurrent.atomic.AtomicInteger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class OptimizedDom {

    private final Node node;

    public OptimizedDom(final Node node) {
        this.node = node;
    }

    public OptimizedXml parse() {
        final OptimizedXml xml = new OptimizedXml();
        parse(-1, this.node, xml);
        return xml;
    }

    private final AtomicInteger index = new AtomicInteger(-1);

    private void parse(int parent, Node node, OptimizedXml xml) {
        if (node == null) {
            return;
        }
        final int id = this.index.incrementAndGet();
        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                xml.addElement(parent, id, OptimizedXml.Type.DOCUMENT, "");
                final NodeList nodes = node.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    this.parse(id, nodes.item(i), xml);
                }
                break;
            case Node.ELEMENT_NODE:
                final String name = node.getNodeName();
                xml.addElement(parent, id, OptimizedXml.Type.ELEMENT, name);
                final NamedNodeMap attributes = node.getAttributes();
                if (attributes != null) {
                    for (int i = 0; i < attributes.getLength(); i++) {
                        final Node attr = attributes.item(i);
                        final int attrId = this.index.incrementAndGet();
                        final String text = attr.getNodeName() + "='" + attr.getNodeValue() + "'";
                        xml.addElement(
                            id,
                            attrId,
                            OptimizedXml.Type.ATTRIBUTE,
                            text
                        );
                    }
                }
                final int contentId = this.index.incrementAndGet();
                xml.addElement(id, contentId, OptimizedXml.Type.CONTENT, null);
                final NodeList children = node.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    this.parse(contentId, children.item(i), xml);
                }
                break;
            case Node.TEXT_NODE:
                final String value = node.getNodeValue();
                xml.addElement(parent, id, OptimizedXml.Type.CHARDATA, value);
                break;
        }

    }

}
