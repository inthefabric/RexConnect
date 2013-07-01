package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.filterchain.BaseFilter;

import com.fabric.rexconnect.core.commands.Command;
import com.fabric.rexconnect.core.commands.SessionCommand;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpRequest;
import com.fabric.rexconnect.core.io.TcpRequestCommand;
import com.fabric.rexconnect.core.io.TcpResponse;
import com.fabric.rexconnect.core.io.TcpResponseCommand;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommandHandler extends BaseFilter {

    private static final Logger vLog = Logger.getLogger(CommandHandler.class);
    
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
    public static TcpResponse getResponse(SessionContext pSessCtx, String pRequestJson)
    																			throws IOException {
    	long t = System.currentTimeMillis();
    	long t2 = 0, t2_5 = 0, t3 = 0, t4 = 0, t5 = 0, t6 = 0, t7 = 0;
		TcpResponse resp = new TcpResponse();
		
		try {
			ObjectMapper om = new ObjectMapper();
	    	t2 = System.currentTimeMillis();
			TcpRequest req = om.readValue(pRequestJson, TcpRequest.class);
			t2_5 = System.currentTimeMillis();
			
			if ( req.sessId != null ) {
				pSessCtx.openSession(UUID.fromString(req.sessId));
			}
			
			resp.reqId = req.reqId;
			int n = req.cmdList.size();
			t3 = System.currentTimeMillis();
			
			for ( int i = 0 ; i < n ; ++i ) {
				resp.cmdList.add(
					executeRequestCommand(pSessCtx, req.cmdList.get(i), i)
				);
			}
			
			t4 = System.currentTimeMillis();
		}
		catch ( Exception e ) {
			vLog.error("Exception "+resp.reqId+":\n"+
				" - Request: "+pRequestJson+"\n - Details: "+e, e);
			
			String msg = e.getMessage();
			resp.err = (msg == null ? e.toString() : msg);
		}
		
		t5 = System.currentTimeMillis();
		pSessCtx.closeClientIfExists();
		t6 = System.currentTimeMillis();
		
		resp.sessId = (pSessCtx.isSessionOpen() ? pSessCtx.getSessionId().toString() : null);
		t7 = System.currentTimeMillis();
		resp.timer = System.currentTimeMillis()-t;
		vLog.warn("Timer 2:"+(t2-t)+" | 2.5:"+(t2_5-t)+" | 3:"+(t3-t)+" | 4:"+(t4-t)+
				" | 5:"+(t5-t)+" | 6:"+(t6-t)+" | 7:"+(t7-t));
		return resp;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private static TcpResponseCommand executeRequestCommand(SessionContext pSessCtx,
										TcpRequestCommand pReqCmd, int pIndex) throws IOException {
		if ( pSessCtx.getConfigDebugMode() ) {
			String cmdStr = pReqCmd.cmd;
			
			for ( String arg : pReqCmd.args ) {
				cmdStr += " | "+arg;
			}
			
			vLog.debug("//  CMD: "+cmdStr);
		}
		
		Command c = Command.build(pSessCtx, pReqCmd.cmd, pReqCmd.args);
		c.execute();
		TcpResponseCommand respCmd = c.getResponse();
		
		if ( pSessCtx.getConfigDebugMode() ) {
			String json = PrettyJson.getJson(respCmd, false);
			vLog.debug("//  JSON: "+json);
		}

		if ( respCmd.err != null ) {
			String errMsg = "Error for command '"+
				pReqCmd.cmd+"' at index "+pIndex+": "+respCmd.err;
			vLog.error(errMsg);
			
			if ( pSessCtx.isSessionOpen() ) {
				cleanupFailedSession(pSessCtx);
			}
			
			throw new IOException(errMsg);
		}
		
		return respCmd;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private static void cleanupFailedSession(SessionContext pSessCtx) {
		String sessId = pSessCtx.getSessionId().toString();
		
		List<String> args = new ArrayList<String>();
		args.add(SessionCommand.ROLLBACK);
		Command c = Command.build(pSessCtx, Command.SESSION, args);
		c.execute();
		
		TcpResponseCommand respCmd = c.getResponse();
		vLog.error("Session "+sessId+": Rollback with results="+
			respCmd.results+", err="+respCmd.err);
		
		////
		
		args.remove(0);
		args.add(SessionCommand.CLOSE);
		c = Command.build(pSessCtx, Command.SESSION, args);
		c.execute();
		
		respCmd = c.getResponse();
		vLog.error("Session "+sessId+": Close with results="+
			respCmd.results+", err="+respCmd.err);
	}
    
}