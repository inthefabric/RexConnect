package com.fabric.rexconnect.core;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.commands.Command;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpRequestCommand;
import com.fabric.rexconnect.core.io.TcpResponseCommand;

public class CommandExecutor {

    private static final Logger vLog = Logger.getLogger(CommandExecutor.class);
    private static final Boolean vIsInit = Init();
    

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static Boolean Init() {
		vLog.setLevel(Level.DEBUG);
		return true;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public static TcpResponseCommand execute(SessionContext pSessCtx, TcpRequestCommand pReqCmd,
						int pIndex, Map<String,TcpResponseCommand> pCmdRespMap) throws IOException {
		Boolean debug = pSessCtx.getConfigDebugMode();
		
		if ( debug ) {
			logCommand(pReqCmd, pIndex);
		}
		
		if ( !allowCommandExecution(pSessCtx, pReqCmd, pCmdRespMap) ) {
			return getSkippedResponse(pReqCmd, pCmdRespMap, debug);
		}
		
		////
		
		Command c = Command.build(pSessCtx, pReqCmd.cmd, pReqCmd.args);
		c.execute();
		TcpResponseCommand respCmd = c.getResponse();
		respCmd.cmdId = pReqCmd.cmdId;
		
		transformWithOptions(pReqCmd, respCmd);
		
		if ( debug ) {
			vLog.debug("//  JSON: "+pIndex+" | "+PrettyJson.getJson(respCmd, false));
		}

		if ( respCmd.err != null ) {
			String errMsg = "Error for command '"+
				pReqCmd.cmd+"' at index "+pIndex+": "+respCmd.err;
			vLog.error(errMsg);
			throw new IOException(errMsg);
		}

		if ( pReqCmd.cmdId != null ) {
			pCmdRespMap.put(pReqCmd.cmdId, respCmd);
		}
		
		return respCmd;
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	private static void logCommand(TcpRequestCommand pReqCmd, int pIndex) {
		String cmdStr = pReqCmd.cmd;
		
		if ( pReqCmd.cmdId != null ) {
			cmdStr = pReqCmd.cmdId+" | "+cmdStr;
		}
		
		for ( String arg : pReqCmd.args ) {
			cmdStr += " | "+arg;
		}
		
		vLog.debug("//  CMD: "+pIndex+" | "+cmdStr);
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
	private static TcpResponseCommand getSkippedResponse(TcpRequestCommand pReqCmd, 
									Map<String,TcpResponseCommand> pCmdRespMap, Boolean pDebug) {
		if ( pDebug ) {
			vLog.debug("//  SKIP CMD: "+pReqCmd.cmdId);
		}
		
		TcpResponseCommand rc = new TcpResponseCommand();
		
		if ( pReqCmd.cmdId != null ) {
			rc.cmdId = pReqCmd.cmdId;
			pCmdRespMap.put(pReqCmd.cmdId, rc);
		}
		
		return rc;
	}

	/*--------------------------------------------------------------------------------------------*/
	private static void transformWithOptions(TcpRequestCommand pReq, TcpResponseCommand pResp) {
		if ( pReq.isOptionEnabled(TcpRequestCommand.Option.OMIT_TIMER) ) {
			pResp.timer = null;
		}
		
		if ( pReq.isOptionEnabled(TcpRequestCommand.Option.OMIT_RESULTS) ) {
			pResp.results = null;
		}
	}

}