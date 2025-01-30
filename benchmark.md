
# Benchmark Results


## XPath vs Navigation

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/@name` | 255634872 | 255.634872 | j$Collections |
| Jaxen | `/program/@name` | 46568672 | 46.568672 | j$Collections |
| JAXP | `/program/@name` | 75097159 | 75.097159 | j$Collections |
| Xnav | `.element('program').attribute('name')` | 2801441 | 2.801441 | j$Collections |



## /program/@name

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/@name` | 564686 | 0.564686 | j$Collections |
| Jaxen | `/program/@name` | 245547 | 0.245547 | j$Collections |
| JAXP | `/program/@name` | 31218813 | 31.218813 | j$Collections |
| Xnav | `/program/@name` | 14974886 | 14.974886 | j$Collections |



## /program/objects/o/@base

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/objects/o/@base` | 529216 | 0.529216 | jeo.class |
| Jaxen | `/program/objects/o/@base` | 2018911 | 2.018911 | jeo.class |
| JAXP | `/program/objects/o/@base` | 33283360 | 33.28336 | jeo.class |
| Xnav | `/program/objects/o/@base` | 301154 | 0.301154 | jeo.class |



## /program/objects/o/o/o/@base

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/objects/o/o/o/o/@base` | 23503428 | 23.503428 | org.eolang.bytes |
| Jaxen | `/program/objects/o/o/o/o/@base` | 20821581 | 20.821581 | org.eolang.bytes |
| JAXP | `/program/objects/o/o/o/o/@base` | 36426896 | 36.426896 | org.eolang.bytes |
| Xnav | `/program/objects/o/o/o/o/@base` | 334407 | 0.334407 | org.eolang.bytes |


