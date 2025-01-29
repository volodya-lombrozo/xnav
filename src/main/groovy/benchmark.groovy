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
import java.nio.file.Files
import java.nio.file.Path;
import java.nio.file.Paths;

def xml = prepareXml();
def results = []
results << measureSaxon(xml, "/program/@name")
results << measureJaxen(xml, "/program/@name")
results << measureJaxp(xml, "/program/@name")
results << measureXnav(xml)
updateReadme(results)

def tables = []
tables << buildSingleTable("XPath vs Navigation", results)
tables << buildSingleTable("/program/@name", compareXpaths(xml, "/program/@name"))
def report = buildFullReport(tables)
saveFullResults(report)

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

def compareXpaths(xml, xpath) {
    def saxon = measureSaxon(xml, xpath)
    def jaxen = measureJaxen(xml, xpath)
    def jaxp = measureJaxp(xml, xpath)
    def xnav = measureXnav(xml, xpath)
    return [saxon, jaxen, jaxp, xnav]
}

static def measureExecutionTime(label, operation, closure) {
    def start = System.nanoTime()
    def result = closure.call()
    def end = System.nanoTime()
    return [label: label, operation: operation, time: end - start, result: result]
}

// Saxon
static def measureSaxon(xml, xpath) {
    Processor processor = new Processor(false);
    XdmNode xdm = processor.newDocumentBuilder().build(new StreamSource(new StringReader(xml)));
    def compiler = processor.newXPathCompiler()
    return measureExecutionTime(
      "Saxon",
      xpath,
      { compiler.evaluate(xpath, xdm).getTypedValue() }
    )
}

// JAXP
static def measureJaxp(xml, xpath) {
    def factory = DocumentBuilderFactory.newInstance()
    def builder = factory.newDocumentBuilder()
    def doc = builder.parse(new ByteArrayInputStream(xml.bytes))
    def xPathFactory = XPathFactory.newInstance()
    def xPath = xPathFactory.newXPath()
    return measureExecutionTime(
      "JAXP",
      xpath,
      {
          def nodeList = (xPath.evaluate(xpath, doc, XPathConstants.NODESET) as NodeList)
          nodeList.item(0).textContent
      }
    )
}

// Dom4j + Jaxen
def measureJaxen(xml, xpath) {
    Document document = DocumentHelper.parseText(xml);
    return measureExecutionTime(
      "Jaxen",
      "/program/@name",
      { document.selectSingleNode(xpath).getText() }
    )
}

static def measureXnav(xml, xpath) {
    def xnav = new Xnav(xml)
    return measureExecutionTime(
      "Xnav",
      xpath,
      {
          xnav.path(xpath)
            .findFirst()
            .orElseThrow()
            .text()
            .orElse("No child")
      }
    )
}

static def measureXnav(xml) {
    def xnav = new Xnav(xml)
    return measureExecutionTime(
      "Xnav",
      ".element('program').attribute('name')",
      {
          xnav.element("program")
            .attribute("name")
            .text()
            .orElse("No child")
      }
    )
}

static buildSingleTable(title, results) {
    def content = """
## ${title}

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
"""
    results.each {
        content += "| ${it.label} | `${it.operation}` | ${it.time} | ${it.time / 1_000_000} | ${it.result} |\n"
    }
    content += "\n"
    return content;
}

static buildFullReport(tables) {
    def content = """
# Benchmark Results

"""
    tables.each {
        content += "${it}\n"
    }
    return content;
}

static saveFullResults(content) {
    def path = Paths.get("benchmark.md")
    Files.write(path, content.getBytes(StandardCharsets.UTF_8))
    println "Benchmark results written to $path"
}

static updateReadme(results) {
    Path readme = Paths.get(System.getProperty("user.dir")).resolve("README.md")
    def block = """
| Library | XPath Expression | Execution Time (ms) |
|---------|------------------|---------------------|
"""
    results.each {
        block += "| ${it.label} | `${it.operation}` | ${String.format("%.2f", it.time / 1_000_000)} |\n"
    }
    String updated = new String(Files.readAllBytes(readme))
      .replaceAll(/(?s)(<!-- BENCHMARK START -->).*?(<!-- BENCHMARK END -->)/, "\$1\n${block}\n\$2")
    Files.write(readme, updated.bytes)
}