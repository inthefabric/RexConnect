package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import com.fabric.rexconnect.core.commands.Command;
import com.fabric.rexconnect.core.commands.SessionCommand;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpRequest;
import com.fabric.rexconnect.core.io.TcpRequestCommand;
import com.fabric.rexconnect.core.io.TcpResponse;
import com.fabric.rexconnect.core.io.TcpResponseCommand;
import com.fabric.rexconnect.main.RexConnectServer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommandHandler extends BaseFilter {

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
    public NextAction handleRead(final FilterChainContext pFilterCtx) throws IOException {
		long t = System.currentTimeMillis();
		final String request = pFilterCtx.getMessage();
		
		TcpResponse resp = new TcpResponse();
		SessionContext sessCtx = new SessionContext(RexConnectServer.RexConfig);
		int cmdCount = -1;
		
		try {
			TcpRequest req = new ObjectMapper().readValue(request, TcpRequest.class);
			cmdCount = req.cmdList.size();
			
			if ( req.sessId != null ) {
				sessCtx.openSession(UUID.fromString(req.sessId));
			}
			
			resp.reqId = req.reqId;
			int n = req.cmdList.size();
			
			for ( int i = 0 ; i < n ; ++i ) {
				resp.cmdList.add(
					executeRequestCommand(sessCtx, req.cmdList.get(i), i)
				);
			}
		}
		catch ( Exception e ) {
			System.err.println("Exception "+resp.reqId+":\n"+
				" - Request: "+request+"\n - Details: "+e);
			e.printStackTrace(System.err);
			
			String msg = e.getMessage();
			resp.err = (msg == null ? e.toString() : msg);
		}
		
		sessCtx.closeClientIfExists();
		
		resp.sessId = (sessCtx.isSessionOpen() ? sessCtx.getSessionId().toString() : null);
		resp.timer = System.currentTimeMillis()-t;
		
		String json = PrettyJson.getJson(resp, sessCtx.getConfigPrettyMode());
		pFilterCtx.write(pFilterCtx.getAddress(), json, null);
		
		System.out.println(
			"Resp "+resp.reqId+"  --  "+
			(resp.err == null ? "success" : "FAILURE")+
			",  in "+request.length()+
			",  out "+json.length()+
			",  cmd "+cmdCount+
			",  t "+resp.timer+"ms");
		
		if ( sessCtx.getConfigDebugMode() ) {
			System.out.println("Response "+resp.reqId+" JSON:\n"+json);
		}
		
        return pFilterCtx.getStopAction();
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private TcpResponseCommand executeRequestCommand(SessionContext pSessCtx,
										TcpRequestCommand pReqCmd, int pIndex) throws IOException {
		if ( pSessCtx.getConfigDebugMode() ) {
			String cmdStr = pReqCmd.cmd;
			
			for ( String arg : pReqCmd.args ) {
				cmdStr += " | "+arg;
			}
			
			System.out.println("//  CMD: "+cmdStr);
		}
		
		Command c = Command.build(pSessCtx, pReqCmd.cmd, pReqCmd.args);
		c.execute();
		TcpResponseCommand respCmd = c.getResponse();
		
		if ( pSessCtx.getConfigDebugMode() ) {
			String json = PrettyJson.getJson(respCmd, false);
			System.out.println("//  JSON: "+json);
		}

		if ( respCmd.err != null ) {
			String errMsg = "Error for command '"+
				pReqCmd.cmd+"' at index "+pIndex+": "+respCmd.err;
			System.err.println(errMsg);
			
			if ( pSessCtx.isSessionOpen() ) {
				cleanupFailedSession(pSessCtx);
			}
			
			throw new IOException(errMsg);
		}
		
		return respCmd;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private void cleanupFailedSession(SessionContext pSessCtx) {
		String sessId = pSessCtx.getSessionId().toString();
		
		List<String> args = new ArrayList<String>();
		args.add(SessionCommand.ROLLBACK);
		Command c = Command.build(pSessCtx, Command.SESSION, args);
		c.execute();
		
		TcpResponseCommand respCmd = c.getResponse();
		System.err.println("Session "+sessId+": Rollback with results="+
			respCmd.results+", err="+respCmd.err);
		
		////
		
		args.remove(0);
		args.add(SessionCommand.CLOSE);
		c = Command.build(pSessCtx, Command.SESSION, args);
		c.execute();
		
		respCmd = c.getResponse();
		System.err.println("Session "+sessId+": Close with results="+
			respCmd.results+", err="+respCmd.err);
	}
    
}