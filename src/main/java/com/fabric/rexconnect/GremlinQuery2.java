package com.fabric.rexconnect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.msgpack.type.Value;

import com.tinkerpop.rexster.client.RexsterClient;
import com.tinkerpop.rexster.client.RexsterClientFactory;
import com.tinkerpop.rexster.protocol.RemoteRexsterSession;
import com.tinkerpop.rexster.protocol.RexsterBindings;
import com.tinkerpop.rexster.protocol.msg.ConsoleScriptResponseMessage;
import com.tinkerpop.rexster.protocol.msg.ErrorResponseMessage;
import com.tinkerpop.rexster.protocol.msg.MessageFlag;
import com.tinkerpop.rexster.protocol.msg.RexProMessage;
import com.tinkerpop.rexster.protocol.msg.ScriptRequestMessage;

/*================================================================================================*/
public class GremlinQuery2 {

    private static final String HOST = "node3.inthefabric.com"; //"127.0.0.1";
    private static final int HOST_PORT = 8184;

    private RexsterClient vClient;
    private String vResult;
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public GremlinQuery2() throws Exception {
    	vClient = RexsterClientFactory.getInstance().createClient(HOST, HOST_PORT);
		executeInner("g = rexster.getGraph('Fabric')");
    }

	/*--------------------------------------------------------------------------------------------*/
	public void execute(String pScript) throws Exception {
		executeInner(pScript);
	}

	/*--------------------------------------------------------------------------------------------*/
	public String getResultListJson() {
		return "["+vResult+"]";
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private void executeInner(final String pScript) throws Exception {
		List<Map<String, Value>> result = vClient.execute(pScript);
		vResult = "";
		
		for ( Map<String, Value> m : result ) {
			for ( Map.Entry<String, Value> e : m.entrySet() ) {
				System.out.println(" - Entry "+e.getKey()+": "+e.getValue());
			}
		}
	}

}
