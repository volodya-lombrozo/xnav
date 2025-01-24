import com.github.lombrozo.xnav.Xnav

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
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import javax.xml.transform.stream.StreamSource;

import javax.xml.xpath.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import org.eolang.jeo.representation.BytecodeRepresentation;
import org.eolang.jeo.representation.bytecode.Bytecode;

import org.dom4j.Document;
import org.dom4j.DocumentHelper

import java.nio.charset.StandardCharsets;


def xml = "<root><child>hello</child></root>"
prepareXml();
println measureSaxon(xml)
println measureJaxen(xml)
println measureJaxp(xml)
println measureXnav(xml)

def prepareXml() {
    def clazz = Collections.class.name.replace('.', '/') + '.class'
    def input = Thread.currentThread().getContextClassLoader().getResourceAsStream(clazz)
    if (input) {
        def res = new BytecodeRepresentation(new Bytecode(input.bytes)).toEO().toString()
        def kb = res.getBytes(StandardCharsets.UTF_8).length / 1024.0;
        println String.format("Size of XML: %.2f KB", kb)
        return res
    } else {
        throw new IllegalStateException("Could not find class file for ${Collections.class.name}")
    }
}

// Timing function
def measureExecutionTime(closure) {
    def start = System.nanoTime()
    def result = closure.call()
    def end = System.nanoTime()
    return [time: end - start, result: result]
}

// Saxon
def measureSaxon(xml) {
    Processor processor = new Processor(false);
    XdmNode xdm = processor.newDocumentBuilder().build(new StreamSource(new StringReader(xml)));
    def compiler = processor.newXPathCompiler()
    return measureExecutionTime(
      { compiler.evaluate("/root/child/text()", xdm) }
    )
}

// Dom4j + Jaxen
def measureJaxen(xml) {
    Document document = DocumentHelper.parseText(xml);
    return measureExecutionTime(
      { document.selectSingleNode("/root/child/text()").getText() }
    )
}

// JAXP
def measureJaxp(xml) {
    def factory = DocumentBuilderFactory.newInstance()
    def builder = factory.newDocumentBuilder()
    def doc = builder.parse(new ByteArrayInputStream(xml.bytes))
    def xPathFactory = XPathFactory.newInstance()
    def xPath = xPathFactory.newXPath()
    return measureExecutionTime(
      {
          def nodeList = (xPath.evaluate("/root/child/text()", doc, XPathConstants.NODESET) as NodeList)
          nodeList.item(0).textContent
      }
    )
}

def measureXnav(xml) {
    def xnav = new Xnav(xml)
    return measureExecutionTime(
      {
          xnav.element("root")
            .element("child")
            .text()
            .orElse("No child")
      }
    )
}