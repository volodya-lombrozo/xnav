
# Benchmark Results


## XPath vs Navigation

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/@name` | 143292749 | 143.292749 | j$Collections |
| Jaxen | `/program/@name` | 43459456 | 43.459456 | j$Collections |
| JAXP | `/program/@name` | 71695550 | 71.69555 | j$Collections |
| Xnav | `.element('program').attribute('name')` | 2983505 | 2.983505 | j$Collections |



## /program/objects/o/@base

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/objects/o/@base` | 494260 | 0.49426 | jeo.class |
| Jaxen | `/program/objects/o/@base` | 1391679 | 1.391679 | jeo.class |
| JAXP | `/program/objects/o/@base` | 39471169 | 39.471169 | jeo.class |
| Xnav | `/program/objects/o/@base` | 11619555 | 11.619555 | jeo.class |



## /program/objects/o/o/o/@base

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/objects/o/o/o/o/@base` | 6305469 | 6.305469 | org.eolang.bytes |
| Jaxen | `/program/objects/o/o/o/o/@base` | 21423653 | 21.423653 | org.eolang.bytes |
| JAXP | `/program/objects/o/o/o/o/@base` | 63131563 | 63.131563 | org.eolang.bytes |
| Xnav | `/program/objects/o/o/o/o/@base` | 474086 | 0.474086 | org.eolang.bytes |


