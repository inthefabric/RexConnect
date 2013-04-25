package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fabric.rexconnect.main.RexConnectServer;
import com.fabric.rexconnect.session.RexsterClient;
import com.fabric.rexconnect.session.RexsterClientFactory;

/*================================================================================================*/
public class GremlinSessionExecutor extends GremlinExecutor {
	
	private RexsterClient vClient;
	

    ////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	protected List<Object> getList(String pScript, Map<String, Object> pParamMap) throws Exception {
		vClient = RexsterClientFactory.open(RexConnectServer.RexConfig);
		return vClient.execute(pScript, pParamMap);
	}

	/*--------------------------------------------------------------------------------------------*/
	protected void closeClient() throws IOException {
		vClient.close();
	}
	
}