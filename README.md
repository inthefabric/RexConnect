# RexConnect

RexConnect is Java application that provides access to a [Rexster](https://github.com/tinkerpop/rexster)-supported graph database. RexConnect adds a powerful, language-agnostic service layer upon Rexster's [RexPro](https://github.com/tinkerpop/rexster/wiki/RexPro) interface. It provides methods of data access: server and console. The server mode responds to incoming TCP requests; the console mode offers an interactive interface.
- Version: 0.3.0
- License: Apache

#### Current Use Cases
The [Fabric](https://github.com/inthefabric) API, website, and other related projects are written in C#. This makes it more difficult to interact with the Rexster Server, which is a Java project. RexConnect allows these projects to avoid REST/HTTP as a bottleneck, communicating at a lower level instead. Fabric currently uses three distinct graph database scenarios. These scenarios all currently use Titan 0.3.0 with Rexster 2.3.0.

- **Cluster:** The primary scenario uses a graph database spread across a multi-node server cluster. On each node in the cluster, Rexster Server runs with Titan+Cassandra using `storage.backend=embeddedcassandra` (see [this diagram](https://github.com/thinkaurelius/titan/wiki/Using-Cassandra#titan-embedded-mode)). A `RexConnectServer` runs on each node, connecting to Rexster Server via localhost. Fabric's API and Web applications can send Gremlin queries and other commands (via TCP connection) to any cluster node's RexConnect server.

- **Integration Tests:** The second scenario uses a graph database running on a computer in a local network. Rexster Server runs with Titan+Local using `storage.backend=local`. A `RexConnectServer` runs on this same computer, connecting to Rexster Server via localhost. From another development computer in the local network, debug builds and integration tests communicate with the RexConnect server. Fabric integration tests frequently send Gremlin commands like "g.V.remove()" and "g.loadGraphSON('data/FabricTest.json')" to quickly clear and rebuild the local graph database.

- **Remote Dev/Debug:** The first examples use a `RexConnectServer` running on the same computer as Rexster, but this is not necessary. During development or debugging, the RexConnect can connect to Rexster Server using the IP address of the Rexster Server computer. RexConnect (server and console) can accept multiple IP addresses for nodes in a graph cluster; these IPs are all passed to the internal RexPro client.

## Server
The [RexConnectServer](https://github.com/inthefabric/RexConnect/blob/master/src/main/java/com/fabric/rexconnect/main/RexConnectServer.java) responds to TCP requests. These requests are send as JSON data, using RexConnect's [server request format](https://github.com/inthefabric/RexConnect/wiki/RexConnectServer-Request-Format). Requests can contain one or more commands, which execute sequentially. After the request's execution completes, RexConnect responds with JSON data in the [server response format](https://github.com/inthefabric/RexConnect/wiki/RexConnectServer-Response-Format). Responses include details and data for each command in the request.

## Console
[...]

## Documentation
Please see the following Wiki pages for detailed RexConnect usage information:
- [Server Request Format](https://github.com/inthefabric/RexConnect/wiki/RexConnectServer-Request-Format)
- [Server Response Format](https://github.com/inthefabric/RexConnect/wiki/RexConnectServer-Response-Format)
- [Request Commands](https://github.com/inthefabric/RexConnect/wiki/RexConnect-Commands)
- [Usage Examples](https://github.com/inthefabric/RexConnect/wiki/RexConnect-Usage-Examples)
- [RexConnect C# Client](https://github.com/inthefabric/RexConnect/wiki/RexConnect-C%23-Client)

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/9211f0a014961bb25e6329b4a86fc16a "githalytics.com")](http://githalytics.com/inthefabric/RexConnect)
