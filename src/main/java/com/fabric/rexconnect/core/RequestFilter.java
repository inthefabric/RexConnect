package com.fabric.rexconnect.core;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpResponse;
import com.fabric.rexconnect.main.RexConnectServer;

public class RequestFilter extends BaseFilter {

    private static final Logger vLog = Logger.getLogger(RequestFilter.class);
    
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
    public NextAction handleRead(final FilterChainContext pFilterCtx) throws IOException {
		final String reqJson = pFilterCtx.getMessage();
		SessionContext sessCtx = new SessionContext(RexConnectServer.RexConfig);
		
		TcpResponse resp = CommandHandler.getResponse(sessCtx, reqJson);
		String respJson = PrettyJson.getJson(resp, sessCtx.getConfigPrettyMode());
		pFilterCtx.write(pFilterCtx.getAddress(), respJson, null);
		
		vLog.info(
			"Resp "+resp.reqId+"  --  "+
			(resp.err == null ? "success" : "FAILURE")+
			",  in "+reqJson.length()+
			",  out "+respJson.length()+
			",  cmd "+resp.cmdList.size()+
			",  t "+resp.timer+"ms");
		
		if ( sessCtx.getConfigDebugMode() ) {
			vLog.debug("Response "+resp.reqId+" JSON:\n"+respJson);
		}
		
        return pFilterCtx.getStopAction();
	}
    
}