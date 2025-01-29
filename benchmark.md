
# Benchmark Results


## XPath vs Navigation

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/@name` | 106244355 | 106.244355 | j$Collections |
| Jaxen | `/program/@name` | 40346159 | 40.346159 | j$Collections |
| JAXP | `/program/@name` | 71912857 | 71.912857 | j$Collections |
| Xnav | `.element('program').attribute('name')` | 3281715 | 3.281715 | j$Collections |



## /program/objects/o/@base

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/objects/o/@base` | 464026 | 0.464026 | jeo.class |
| Jaxen | `/program/objects/o/@base` | 1557593 | 1.557593 | jeo.class |
| JAXP | `/program/objects/o/@base` | 24110624 | 24.110624 | jeo.class |
| Xnav | `/program/objects/o/@base` | 11726229 | 11.726229 | jeo.class |


