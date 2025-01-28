
# Benchmark Results



| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/@name` | 112523710 | 112.52371 | j$Collections |
| Jaxen | `/program/@name` | 57733669 | 57.733669 | j$Collections |
| JAXP | `/program/@name` | 61652097 | 61.652097 | j$Collections |
| Xnav | `.element('program').attribute('name')` | 2978079 | 2.978079 | j$Collections |
