
# Benchmark Results


## XPath vs Navigation

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | `/program/@name` | 113551162 | 113.551162 | j$Collections |
| Jaxen | `/program/@name` | 48297871 | 48.297871 | j$Collections |
| JAXP | `/program/@name` | 56986195 | 56.986195 | j$Collections |
| Xnav | `.element('program').attribute('name')` | 2549444 | 2.549444 | j$Collections |


