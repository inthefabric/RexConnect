package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.commands.Command;
import com.fabric.rexconnect.core.commands.SessionCommand;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpRequest;
import com.fabric.rexconnect.core.io.TcpRequestCommand;
import com.fabric.rexconnect.core.io.TcpResponse;
import com.fabric.rexconnect.core.io.TcpResponseCommand;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommandHandler {

    private static final Logger vLog = Logger.getLogger(CommandHandler.class);
    private static final ObjectMapper vMapper = new ObjectMapper();
    
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
    public static TcpResponse getResponse(SessionContext pSessCtx, String pRequestJson)
    																			throws IOException {
    	long t = System.currentTimeMillis();
		TcpResponse resp = new TcpResponse();
		
		try {
			TcpRequest req = vMapper.readValue(pRequestJson, TcpRequest.class);
			
			if ( req.sessId != null ) {
				pSessCtx.openSession(UUID.fromString(req.sessId));
			}
			
			resp.reqId = req.reqId;
			int n = req.cmdList.size();
			
			for ( int i = 0 ; i < n ; ++i ) {
				resp.cmdList.add(
					executeRequestCommand(pSessCtx, req.cmdList.get(i), i)
				);
			}
		}
		catch ( Exception e ) {
			vLog.error("Exception "+resp.reqId+":\n"+
				" - Request: "+pRequestJson+"\n - Details: "+e, e);
			
			String msg = e.getMessage();
			resp.err = (msg == null ? e.toString() : msg);
		}
		
		pSessCtx.closeClientIfExists();
		
		resp.sessId = (pSessCtx.isSessionOpen() ? pSessCtx.getSessionId().toString() : null);
		resp.timer = System.currentTimeMillis()-t;
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