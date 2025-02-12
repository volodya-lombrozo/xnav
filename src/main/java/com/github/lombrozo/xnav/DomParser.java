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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DomParser {

    private final Node root;

    public DomParser(final Node root) {
        this.root = root;
    }

    int numberElements() {
        return numberElements(this.root);
    }


    private int numberElements(Node node) {
        if (node == null) {
            return 0;
        }
        int child = 0;
        final NodeList nodes = node.getChildNodes();
        final int length = nodes.getLength();

        for (int i = 0; i < length; i++) {
            child += this.numberElements(nodes.item(i));
        }
        return 1 + child + this.numberAttributes(node);
    }

    private int numberAttributes(Node node) {
        if (node == null) {
            return 0;
        }
        final NamedNodeMap attrs = node.getAttributes();
        if (attrs == null) {
            return 0;
        }
        return attrs.getLength();
    }

}
