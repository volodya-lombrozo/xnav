
# Benchmark Results


## XPath vs Navigation

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/@name` | 151562270 | 151.56227 | j$Collections |
| Jaxen | `/program/@name` | 47037981 | 47.037981 | j$Collections |
| JAXP | `/program/@name` | 62629052 | 62.629052 | j$Collections |
| Xnav | `.element('program').attribute('name')` | 3963254 | 3.963254 | j$Collections |



## /program/objects/o/@base

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/objects/o/@base` | 522288 | 0.522288 | jeo.class |
| Jaxen | `/program/objects/o/@base` | 2495899 | 2.495899 | jeo.class |
| JAXP | `/program/objects/o/@base` | 94524467 | 94.524467 | jeo.class |
| Xnav | `/program/objects/o/@base` | 22815038 | 22.815038 | jeo.class |



## /program/objects/o/o/o/@base

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/objects/o/o/o/o/@base` | 12328695 | 12.328695 | org.eolang.bytes |
| Jaxen | `/program/objects/o/o/o/o/@base` | 18278175 | 18.278175 | org.eolang.bytes |
| JAXP | `/program/objects/o/o/o/o/@base` | 56327028 | 56.327028 | org.eolang.bytes |
| Xnav | `/program/objects/o/o/o/o/@base` | 335511 | 0.335511 | org.eolang.bytes |


