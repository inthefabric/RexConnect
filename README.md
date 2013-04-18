## RexConnect
RexConnect is simple Java application that wraps [Rexster](https://github.com/tinkerpop/rexster)'s [RexPro](https://github.com/tinkerpop/rexster/wiki/RexPro) with a server layer and some custom data formatting. The [RexConnectServer](https://github.com/inthefabric/RexConnect/blob/master/src/main/java/com/fabric/rexconnect/RexConnectServer.java) accepts parameterized [Gremlin](https://github.com/tinkerpop/gremlin/wiki) scripts via TCP connection, executes them with the RexPro client, formats the results into a custom JSON format, and then responds with those results.

### Usage Scenario
The [Fabric](https://github.com/inthefabric) API, website, and other related projects are written in C#. This makes it more difficult to interact with the Rexster Server, which is a Java project. Rexster does offer a REST access (which accepts custom Gremlin code), but the goal was to remove REST/HTTP as a potential bottleneck and instead communicate at a lower level.

[More coming soon...]
