
# Benchmark Results


## XPath vs Navigation

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/@name` | 110584153 | 110.584153 | j$Collections |
| Jaxen | `/program/@name` | 49069702 | 49.069702 | j$Collections |
| JAXP | `/program/@name` | 55676732 | 55.676732 | j$Collections |
| Xnav | `.element('program').attribute('name')` | 2868098 | 2.868098 | j$Collections |



## /program/@name

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/@name` | 510590 | 0.51059 | j$Collections |
| Jaxen | `/program/@name` | 189891 | 0.189891 | j$Collections |
| JAXP | `/program/@name` | 24261965 | 24.261965 | j$Collections |
| Xnav | `/program/@name` | 12924630 | 12.92463 | j$Collections |


