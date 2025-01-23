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

import java.nio.file.*

def testFilePath = "src/test/java/ExampleTest.java"
def readmeFilePath = "README.md"

// Read the test file
def testFile = Paths.get(testFilePath)
def lines = Files.readAllLines(testFile)

// Extract the example from the test class
def exampleStart = lines.indexOf(lines.find { it.contains("void exampleUsage()") }) + 1
def exampleEnd = lines.indexOf(lines.find { it.trim() == "}" }, exampleStart)
def exampleCode = lines[exampleStart..exampleEnd]
  .collect { it.replaceFirst(/\s{8}/, "") } // Remove indentation
  .join("\n")

// Format the extracted example as a code block
def exampleBlock = """```java
${exampleCode}
```"""

// Update the README file
def readmeFile = Paths.get(readmeFilePath)
def readmeContent = new String(Files.readAllBytes(readmeFile))
def updatedReadme = readmeContent.replaceAll(
  /(?s)(<!-- EXAMPLE START -->).*?(<!-- EXAMPLE END -->)/,
  "\$1\n${exampleBlock}\n\$2"
)

// Write the updated content back to the README file
Files.write(readmeFile, updatedReadme.bytes)

println "README.md has been updated with the latest example!"
