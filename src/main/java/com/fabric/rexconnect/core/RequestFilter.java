package com.fabric.rexconnect.core;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpResponse;

public class RequestFilter extends BaseFilter {

    private static final Logger vLog = Logger.getLogger(RequestFilter.class);
    
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
    public NextAction handleRead(final FilterChainContext pFilterCtx) throws IOException {
		final String req = pFilterCtx.getMessage();
		final String resp = executeRequest(req);
		
		pFilterCtx.write(pFilterCtx.getAddress(), resp, null);
        return pFilterCtx.getStopAction();
	}
    
    /*--------------------------------------------------------------------------------------------*/
    public String executeRequest(final String pRequestJson) throws IOException {
		SessionContext sessCtx = new SessionContext();
		
		TcpResponse resp = RequestExecutor.getResponse(sessCtx, pRequestJson);
		String respJson = PrettyJson.getJson(resp, sessCtx.getConfigPrettyMode());
		
		vLog.info(
			"Resp "+resp.reqId+"  --  "+
			(resp.err == null ? "success" : "FAILURE")+
			",  in "+pRequestJson.length()+
			",  out "+respJson.length()+
			",  cmd "+resp.cmdList.size()+
			",  t "+resp.timer+"ms");
		
		if ( sessCtx.getConfigDebugMode() ) {
			vLog.debug("Response "+resp.reqId+" JSON:\n"+respJson);
		}
		
        return pRequestJson;
	}
    
}