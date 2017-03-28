Java implementation of the Validation Transformation Language
# Status

[codacy-link]: https://www.codacy.com/app/hadrien-kohl/ssb-java-vtl/dashboard
[travis-link]: https://travis-ci.org/statisticsnorway/java-vtl/branches
[gitter-link]: https://gitter.im/java-vtl/Lobby?utm_source=share-link&utm_medium=link&utm_campaign=share-link

[codacy-img]: https://img.shields.io/codacy/grade/e187c210f99b4c179550b9bcb1c92860/develop.svg
[codacy-cov-img]: https://img.shields.io/codacy/coverage/e187c210f99b4c179550b9bcb1c92860/develop.svg
[travis-img]: https://img.shields.io/travis/statisticsnorway/java-vtl/develop.svg
[gitter-img]: https://img.shields.io/gitter/room/java-vtl/Lobby.svg


[![Build Status][travis-img]][travis-link]
[![Codacy Badge][codacy-img]][codacy-link]
[![Codacy coverage][codacy-cov-img]][codacy-link]
[![Gitter][gitter-img]][gitter-link]

# Implementation roadmap


[done]: http://progressed.io/bar/100?title=done "Done"

Group|Operators|Progress|Comment
---|---|---|---
General purpose|round parenthesis|
General purpose|assignment|![done][done]
General purpose|membership|![done][done]
General purpose|get|![usable](http://progressed.io/bar/20)|The keep, filter and aggregate are not yet reflected in the connector interface.
General purpose|put|![usable](http://progressed.io/bar/90)|The Connector interface is defined but expressions are not recognized yet.
Clauses|rename|![done][done]
Clauses|filter|
Clauses|keep|
Clauses|calc|
Clauses|attrcalc|
Clauses|aggregate|
Conditional|if-then-else|
Conditional|nvl|![usable](http://progressed.io/bar/50)|Dataset as input not implemented.
Validation|Comparisons (>,<,>=,<=,=,<>)|
Validation|in,not in, between|
Validation|isnull|![done][done]|Implemented syntax are `isnull(value)`, `value is null` and `value is not null`|
Validation|exist_in, not_exist_in|
Validation|exist_in_all, not_exist_in_all|
Validation|check|
Validation|match_characters|
Validation|match_values|
Statistical|min, max|
Statistical|hierarchy|
Statistical|aggregate|
Relational|union|
Relational|intersect|
Relational|symdiff|
Relational|setdiff|
Relational|merge|
Boolean|and|
Boolean|or|
Boolean|xor|
Boolean|not|
Mathematical|unary plus and minus|
Mathematical|addition, substraction|
Mathematical|multiplication, division|
Mathematical|round|
Mathematical|abs|
Mathematical|trunc|
Mathematical|power, exp, nroot|
Mathematical|in, log|
Mathematical|mod|
String|length|
String|concatenation|
String|trim|
String|upper/lower case|
String|substring|
String|indexof|
String|date_from_string|![usable](http://progressed.io/bar/25)|Dataset as input not implemented. Only YYYY date format accepted.


















