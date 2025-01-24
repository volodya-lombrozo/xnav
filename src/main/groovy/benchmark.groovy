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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

def xml = "<root><child>hello</child></root>"

// saxon
Processor processor = new Processor(false);
XdmNode xdm = processor.newDocumentBuilder().build(new StreamSource(new StringReader(xml)));
XdmValue result = processor.newXPathCompiler().evaluate("/root/child/text()", xdm);
println result;

// dom4j + Jaxen
Document document = DocumentHelper.parseText(xml);
println document.selectSingleNode("/root/child/text()").getText()

// xnav
println new Xnav(xml)
  .element("root")
  .element("child")
  .text()
  .orElse("No child")

// JAXP
def factory = DocumentBuilderFactory.newInstance()
def builder = factory.newDocumentBuilder()
def doc = builder.parse(new ByteArrayInputStream(xml.bytes))
def xPathFactory = XPathFactory.newInstance()
def xPath = xPathFactory.newXPath()
def expression = "/root/child/text()"
def nodeList = xPath.evaluate(expression, doc, XPathConstants.NODESET) as NodeList
println nodeList.item(0).textContent;
