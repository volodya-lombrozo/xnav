
# Benchmark Results

| Library | XPath Expression | Execution Time (ns) | Execution Time (ms) | Result |
|---------|------------------|---------------------|---------------------|--------|
| Saxon | /program/@name | 131492947 | 131.492947 | j$Collections |
| Jaxen | /program/@name | 34600863 | 34.600863 | j$Collections |
| JAXP | /program/@name | 63219987 | 63.219987 | j$Collections |
| Xnav | .element('program').attribute('name') | 3059589 | 3.059589 | j$Collections |
