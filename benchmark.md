
# Benchmark Results



| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/@name` | 99894475 | 99.894475 | j$Collections |
| Jaxen | `/program/@name` | 46709078 | 46.709078 | j$Collections |
| JAXP | `/program/@name` | 58332277 | 58.332277 | j$Collections |
| Xnav | `.element('program').attribute('name')` | 3050378 | 3.050378 | j$Collections |
