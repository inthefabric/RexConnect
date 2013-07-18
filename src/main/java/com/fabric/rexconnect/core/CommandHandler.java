package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
			Map<String,TcpResponseCommand> cmdRespMap = new HashMap<String,TcpResponseCommand>(n);
			
			for ( int i = 0 ; i < n ; ++i ) {
				resp.cmdList.add(
					executeRequestCommand(pSessCtx, req.cmdList.get(i), i, cmdRespMap)
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
								TcpRequestCommand pReqCmd, int pIndex,
								Map<String,TcpResponseCommand> pCmdRespMap) throws IOException {
		if ( pSessCtx.getConfigDebugMode() ) {
			String cmdStr = pReqCmd.cmd;
			
			if ( pReqCmd.cmdId != null ) {
				cmdStr = pReqCmd.cmdId+" | "+cmdStr;
			}
			
			for ( String arg : pReqCmd.args ) {
				cmdStr += " | "+arg;
			}
			
			vLog.debug("//  CMD: "+pIndex+" | "+cmdStr);
		}
		
		if ( !allowCommandExecution(pReqCmd, pCmdRespMap) ) {
			if ( pSessCtx.getConfigDebugMode() ) {
				vLog.debug("//  SKIP CMD: "+pReqCmd.cmdId);
			}
			
			TcpResponseCommand nonResp = new TcpResponseCommand();
			nonResp.cmdId = pReqCmd.cmdId;
			nonResp.timer = -1;
			return nonResp;
		}
		
		////
		
		Command c = Command.build(pSessCtx, pReqCmd.cmd, pReqCmd.args);
		c.execute();
		TcpResponseCommand respCmd = c.getResponse();
		respCmd.cmdId = pReqCmd.cmdId;
		
		if ( pSessCtx.getConfigDebugMode() ) {
			String json = PrettyJson.getJson(respCmd, false);
			vLog.debug("//  JSON: "+pIndex+" | "+json);
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

		if ( pReqCmd.cmdId != null ) {
			pCmdRespMap.put(pReqCmd.cmdId, respCmd);
		}
		
		return respCmd;
	}

	/*--------------------------------------------------------------------------------------------*/
	private static Boolean allowCommandExecution(TcpRequestCommand pReqCmd,
								Map<String,TcpResponseCommand> pCmdRespMap) throws IOException {
		if ( pReqCmd.cond == null ) {
			return true;
		}
		
		for ( String condCmdId : pReqCmd.cond ) {
			if ( !pCmdRespMap.containsKey(condCmdId) ) {
				throw new IOException("Unknown conditional command ID: "+condCmdId);
			}
			
			TcpResponseCommand r = pCmdRespMap.get(condCmdId);
			
			if ( r.err != null ) {
				return false;
			}
			
			int s = (r.results == null ? 0 : r.results.size());
			
			if ( s == 1 ) {
				Object obj = r.results.get(0);
				String str = (obj == null ? null : obj.toString().toLowerCase());
				return (str != null && str != "0" && str != "" && str != "false");
			}
			
			return (s > 1);
		}
		
		return true;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private static void cleanupFailedSession(SessionContext pSessCtx) {
		String sessId = pSessCtx.getSessionId().toString();
		
		List<String> args = new ArrayList<String>();
		args.add(SessionCommand.ROLLBACK);
		Command c = Command.build(pSessCtx, Command.SESSION, args);
		c.execute();
		
		TcpResponseCommand respCmd = c.getResponse();
		vLog.error("Session "+sessId+" failed: Rollback with results="+
			respCmd.results+", err="+respCmd.err);
		
		////
		
		args.remove(0);
		args.add(SessionCommand.CLOSE);
		c = Command.build(pSessCtx, Command.SESSION, args);
		c.execute();
		
		respCmd = c.getResponse();
		vLog.error("Session "+sessId+" failed: Close with results="+
			respCmd.results+", err="+respCmd.err);
	}
    
}