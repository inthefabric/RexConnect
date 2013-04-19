# RexConnect
## Overview
RexConnect is simple Java application that wraps [Rexster](https://github.com/tinkerpop/rexster)'s [RexPro](https://github.com/tinkerpop/rexster/wiki/RexPro) with a server layer and some custom data formatting. It offers two modes: server and console. The server listens for incoming Gremlin scripts and returns the results. The console mode provides a simple, interactive way to execute Gremlin queries.

### Development Considerations
RexConnect is intended to be fast, lightweight, and easy to maintain. It began as a very minimal experiment to see if this approach would suit Fabric's needs. It did the job well, and has become slightly less minimal with the inclusion of the console mode.

One alternative to the RexConnect approach would be to port the RexPro Java code to C#. The brand-new [RexPro-Client](https://github.com/dkuppitz/rexpro-client) project seems to take this approach -- it may actually be very useful for Fabric. Implementing a C# port would be much larger undertaking than the simpler RexConnect. Also important -- the Rexster and Titan projects are continually changing and growing. For Fabric's purposes, it seemed ideal to utilize Rexster's own RexPro client, essentially allowing the Rexster team to keep RexConnect up-to-date with all the latest changes and features. 

### Gremlin Transactions
RexConnect uses the sessionless RexPro client. Each Gremlin request exists within its own scope, and represents a single database transaction. If a variable is created in a request (such as "a=g.V[0]"), that variable will *not* be available in subsequent requests.

### Current Use Cases
The [Fabric](https://github.com/inthefabric) API, website, and other related projects are written in C#. This makes it more difficult to interact with the Rexster Server, which is a Java project. RexConnect allows these projects to avoid REST/HTTP as a potential bottleneck, and instead, communicate at a lower level. Fabric currently uses three graph database scenarios. Both currently use Titan 0.3.0 with Rexster 2.3.0.

- **Cluster:** The primary scenario uses a graph database spread across a multi-node server cluster. On each node in the cluster, Rexster Server runs with Titan+Cassandra using storage.backend=embeddedcassandra (see [this diagram](https://github.com/thinkaurelius/titan/wiki/Using-Cassandra#titan-embedded-mode)). RexConnectServer also runs on each node, and connects to Rexster Server via localhost. The Fabric API and Web applications can (via TCP connection) send Gremlin scripts to a RexConnectServer on any of the cluster nodes.

- **Integration Tests:** The second scenario uses a graph database running on a computer in a local network. Rexster Server runs with Titan+Local using storage.backend=local. RexConnectServer runs on this same computer, connecting to Rexster Server via localhost. From another development computer in the local network, debug builds and integration tests communicate with RexConnectServer. Fabric integration tests frequently send Gremlin commands like "g.V.remove()" and "g.loadGraphSON('data/FabricTest.json')" to quickly clear and rebuild the local graph database.

- **Remote Dev/Debug:** The first examples use RexConnectServer running on the same computer as Rexster, but this is not necessary. During development or debugging, the RexConnectServer can connect to Rexster Server using the IP address of the Rexster Server computer. RexConnect (server and console) can accept multiple IP addresses for nodes in a graph cluster; these IPs are all passed to the internal RexPro client.

## Server
The [RexConnectServer](https://github.com/inthefabric/RexConnect/blob/master/src/main/java/com/fabric/rexconnect/RexConnectServer.java) accepts parameterized [Gremlin](https://github.com/tinkerpop/gremlin/wiki) scripts via TCP connection, executes them with the RexPro client, formats the results into a custom JSON format, and then responds with those results.

RexConnectServer expects requests in the following format:

```
<Request ID>#<Gremlin Script>#<Parameter JSON>
```

Example request string:

```
A1234#g.V[x]#{"x":0}
```

Start the server by executing the "rexConnectServer" script (.sh or .bat) from the project's root directory.

```
/RexConnect> bin/rexConnectServer.sh

          ---===##\    
              --==##\  
---===################>
              --==##/  
          ---===##/    

RexConnect Server 0.2.0
{rexpro_port=8184, rexpro_graph_name=FabricTest, rexconnect_port=8185, rexpro_hosts=localhost}

-------------------------------------------------------------

Loading QuickServer v1.4.5 ... Done

-------------------------------
 Name : RexConnectServer
 Port : 8185
-------------------------------

Server started.

Attempting to connect to RexPro...
Connected!

------------- Timer: 0.000000 days / Heartbeat: 434ms
------------- Timer: 0.000121 days / Heartbeat: 111ms
```

Implement a function for connecting to RexConnectServer and executing a parameterized Gremlin query. Example C# implementation:

```c#
public static string ExecuteGremlin(string pId, string pQuery, string pParamJson=null) {
	string request = pId+'#'+pQuery+(pParamJson == null ? "" : '#'+pParamJson);
	byte[] data = Encoding.ASCII.GetBytes(request);
	
	TcpClient tcp = new TcpClient("localhost", 8185);
	NetworkStream stream = tcp.GetStream();
	stream.Write(data, 0, data.Length);

	int size = tcp.ReceiveBufferSize;
	int bytes = size;
	string response = "";

	while ( bytes == size ) {
		data = new byte[size];
		bytes = stream.Read(data, 0, data.Length);
		response += Encoding.ASCII.GetString(data, 0, bytes);
	}

	tcp.Close();
	return response;
}
```

Execute some queries! Example C# implementation:

```c#
string id = "A1234";

string graphInfo = ExecuteGremlin(id, "g");
string withScript = ExecuteGremlin(id, "g.V[0]");
string withParams = ExecuteGremlin(id, "g.V[x]", "{\"x\": 0}");

string newVert = ExecuteGremlin(id,
	"v1=g.addVertex([x:p0, y:p1, z:p2]); v0=g.V[0].next(); g.addEdge(v0,v1,edge); v1;",
	"{\"p0\": 123, \"p1\": 234, \"p2\": 345, \"edge\": \"has\"}"
);
```

## Console
To use the [RexConnectConsole](https://github.com/inthefabric/RexConnect/blob/master/src/main/java/com/fabric/rexconnect/RexConnectConsole.java), execute the "rexConnectConsole" script (.sh or .bat) from the project's root directory.

Executing a Gremlin query requires two input prompts: "script" and "params". The "script" is a Gremlin script, which may include parameters. If the "script" does not use parameters, the "params" input can be empty. Otherwise, the "params" should be provided in JSON format.

```
/RexConnect> bin/rexConnectConsole.sh

          ---===##\    
              --==##\  
---===################>
              --==##/  
          ---===##/    

RexConnect Console 0.2.0
{rexpro_port=8184, rexpro_graph_name=FabricTest, rexconnect_port=8185, rexpro_hosts=localhost}

-------------------------------------------------------------

RexConn script> g
        params>

... ["titangraph[local:data/FabricTest]"]
... 426ms


RexConn script> g.V.count()
        params>

... [263]
... 190ms


RexConn script> g.V[0]
        params>

... [{"_properties":{"E_Ve":635008745014706824,"E_Cr":635008745004706824,"N_FT":4000,"E_Ad":"test@test.com","E_Co":"17fb6aae419f42d19a887bd9636ce52a","E_Id":1},"_id":"531208","_type":"vertex"}]
... 71ms


RexConn script> g.V[x.toInteger()]
        params> {"x":0}

... [{"_properties":{"E_Ve":635008745014706824,"E_Cr":635008745004706824,"N_FT":4000,"E_Ad":"test@test.com","E_Co":"17fb6aae419f42d19a887bd9636ce52a","E_Id":1},"_id":"531208","_type":"vertex"}]
... 39ms

```

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/9211f0a014961bb25e6329b4a86fc16a "githalytics.com")](http://githalytics.com/inthefabric/RexConnect)
