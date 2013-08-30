package com.fabric.rexconnect.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.commands.Command;
import com.fabric.rexconnect.core.commands.SessionCommand;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpRequest;
import com.fabric.rexconnect.core.io.TcpRequestCommand;
import com.fabric.rexconnect.core.io.TcpResponse;
import com.fabric.rexconnect.core.io.TcpResponseCommand;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestExecutor {

    private static final Logger vLog = Logger.getLogger(RequestExecutor.class);
    private static final ObjectMapper vMapper = new ObjectMapper();
    private static final JsonFactory vFactory = vMapper.getFactory();
    private static final Boolean vIsInit = Init();
    

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static Boolean Init() {
		vLog.setLevel(Level.DEBUG);
		return true;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
    public static TcpResponse getResponse(SessionContext pSessCtx, String pRequestJson)
    																			throws IOException {
    	long t = System.currentTimeMillis();
    	TcpRequest req = vMapper.readValue(pRequestJson, TcpRequest.class);
		return executeRequest(t, pSessCtx, req);
	}
    
    /*--------------------------------------------------------------------------------------------*/
    public static TcpResponse getResponse(long pStartTime, SessionContext pSessCtx,
    												InputStream pRequestStream) throws IOException {
		JsonParser jp = vFactory.createJsonParser(pRequestStream);
		TcpRequest req = vMapper.readValue(jp, TcpRequest.class);
		return executeRequest(pStartTime, pSessCtx, req);
	}
    
    /*--------------------------------------------------------------------------------------------*/
    private static TcpResponse executeRequest(long pStartTime, SessionContext pSessCtx,
    														TcpRequest pReq) throws IOException {
		TcpResponse resp = new TcpResponse();
		
		try {
			if ( pReq.sessId != null ) {
				pSessCtx.openSession(UUID.fromString(pReq.sessId));
			}
			
			resp.reqId = pReq.reqId;
			int n = pReq.cmdList.size();
			Map<String,TcpResponseCommand> cmdRespMap = new HashMap<String,TcpResponseCommand>(n);
			
			for ( int i = 0 ; i < n ; ++i ) {
				resp.cmdList.add(
					executeRequestCommand(pSessCtx, pReq.cmdList.get(i), i, cmdRespMap)
				);
			}
		}
		catch ( Exception e ) {
			vLog.error("Exception "+resp.reqId+": "+e.getMessage(), e);
			
			String msg = e.getMessage();
			resp.err = (msg == null ? e.toString() : msg);
		}
		
		pSessCtx.closeClientIfExists();
		
		resp.sessId = (pSessCtx.isSessionOpen() ? pSessCtx.getSessionId().toString() : null);
		resp.timer = System.currentTimeMillis()-pStartTime;
		return resp;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	private static TcpResponseCommand executeRequestCommand(SessionContext pSessCtx,
								TcpRequestCommand pReqCmd, int pIndex,
								Map<String,TcpResponseCommand> pCmdRespMap) throws IOException {
		Boolean debug = pSessCtx.getConfigDebugMode();
		
		if ( debug ) {
			String cmdStr = pReqCmd.cmd;
			
			if ( pReqCmd.cmdId != null ) {
				cmdStr = pReqCmd.cmdId+" | "+cmdStr;
			}
			
			for ( String arg : pReqCmd.args ) {
				cmdStr += " | "+arg;
			}
			
			vLog.debug("//  CMD: "+pIndex+" | "+cmdStr);
		}
		
		if ( !allowCommandExecution(pSessCtx, pReqCmd, pCmdRespMap) ) {
			if ( debug ) {
				vLog.debug("//  SKIP CMD: "+pReqCmd.cmdId);
			}
			
			TcpResponseCommand nonResp = new TcpResponseCommand();
			nonResp.timer = (long)-1;
			
			if ( pReqCmd.cmdId != null ) {
				nonResp.cmdId = pReqCmd.cmdId;
				pCmdRespMap.put(pReqCmd.cmdId, nonResp);
			}
			
			return nonResp;
		}
		
		////
		
		Command c = Command.build(pSessCtx, pReqCmd.cmd, pReqCmd.args);
		c.execute();
		TcpResponseCommand respCmd = c.getResponse();
		respCmd.cmdId = pReqCmd.cmdId;
		
		if ( debug ) {
			vLog.debug("//  JSON: "+pIndex+" | "+PrettyJson.getJson(respCmd, false));
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
	private static Boolean allowCommandExecution(SessionContext pSessCtx, TcpRequestCommand pReqCmd,
								Map<String,TcpResponseCommand> pCmdRespMap) throws IOException {
		if ( pReqCmd.cond == null ) {
			return true;
		}
		
		Boolean debug = pSessCtx.getConfigDebugMode();
		
		for ( String condCmdId : pReqCmd.cond ) {
			if ( !pCmdRespMap.containsKey(condCmdId) ) {
				throw new IOException("Unknown conditional command ID: "+condCmdId);
			}
			
			TcpResponseCommand r = pCmdRespMap.get(condCmdId);
			
			if ( r.err != null ) {
				if ( debug ) {
					vLog.debug("//  COND ERR: "+condCmdId+"=["+r.err+"]");
				}
				
				return false;
			}
			
			int s = (r.results == null ? 0 : r.results.size());
			
			if ( s == 1 ) {
				Object obj = r.results.get(0);
				String str = (obj == null ? null : obj.toString().toLowerCase());
				Boolean allow = (str != null && !str.isEmpty() && !str.equals("0") &&
					!str.equals("false"));
				
				if ( debug ) {
					vLog.debug("//  COND RESULT: "+condCmdId+"=["+str+"] ("+allow+")");
				}
				
				if ( !allow ) {
					return false;
				}
			}

			if ( debug ) {
				vLog.debug("//  COND COUNT: "+condCmdId+"=["+s+"]");
			}
			
			if ( s == 0 ) {
				return false;
			}
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