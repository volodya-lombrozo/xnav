
# Benchmark Results


## XPath vs Navigation

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/@name` | 139088961 | 139.088961 | j$Collections |
| Jaxen | `/program/@name` | 36199422 | 36.199422 | j$Collections |
| JAXP | `/program/@name` | 60288334 | 60.288334 | j$Collections |
| Xnav | `.element('program').attribute('name')` | 2989579 | 2.989579 | j$Collections |



## /program/objects/o/@base

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/objects/o/@base` | 657223 | 0.657223 | jeo.class |
| Jaxen | `/program/objects/o/@base` | 1479386 | 1.479386 | jeo.class |
| JAXP | `/program/objects/o/@base` | 45647136 | 45.647136 | jeo.class |
| Xnav | `/program/objects/o/@base` | 20812415 | 20.812415 | jeo.class |


