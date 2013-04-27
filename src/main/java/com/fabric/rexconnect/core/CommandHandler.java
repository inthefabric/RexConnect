package com.fabric.rexconnect.core;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.UUID;

import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientHandler;

import com.fabric.rexconnect.core.commands.Command;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpRequest;
import com.fabric.rexconnect.core.io.TcpRequestCommand;
import com.fabric.rexconnect.core.io.TcpResponse;
import com.fabric.rexconnect.main.RexConnectServer;
import com.fasterxml.jackson.databind.ObjectMapper;

/*================================================================================================*/
public class CommandHandler implements ClientCommandHandler {
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void handleCommand(ClientHandler pHandler, String pRequest) 
														throws SocketTimeoutException, IOException {
		long t = System.currentTimeMillis();
		TcpResponse resp = new TcpResponse();
		SessionContext sessCtx = new SessionContext(RexConnectServer.RexConfig);
		
		try {
			TcpRequest req = new ObjectMapper().readValue(pRequest, TcpRequest.class);
			
			if ( req.sessId != null ) {
				sessCtx.openSession(UUID.fromString(req.sessId));
			}
			
			resp.reqId = req.reqId;
			
			for ( TcpRequestCommand reqCmd : req.cmdList ) {
				Command c = Command.build(sessCtx, reqCmd.cmd, reqCmd.args);
				c.execute();
				resp.cmdList.add(c.getResponse());
			}
		}
		catch ( Exception e ) {
			System.err.println("Exception "+resp.reqId+":\n"+
				" - Request: "+pRequest+"\n - Details: "+e);
			e.printStackTrace(System.err);
			
			String msg = e.getMessage();
			resp.err = (msg == null ? e.toString() : msg);
		}

		resp.sessId = (sessCtx.isSessionOpen() ? sessCtx.getSessionId().toString() : null);
		resp.timer = System.currentTimeMillis()-t;
		
		String json = PrettyJson.getJson(resp, sessCtx.getConfigPrettyMode());
		pHandler.sendClientMsg(json);
		
		System.out.println("Response "+resp.reqId+": "+
			(resp.err == null ? "success" : "failure")+", "+
			json.length()+" chars, "+resp.timer+"ms");
		
		if ( sessCtx.getConfigDebugMode() ) {
			System.out.println("Response "+resp.reqId+" JSON:\n"+json);
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void closingConnection(ClientHandler pHandler) throws IOException {}
	
	/*--------------------------------------------------------------------------------------------*/
	public void gotConnected(ClientHandler pHandler) throws SocketTimeoutException, IOException {}
	
	/*--------------------------------------------------------------------------------------------*/
	public void lostConnection(ClientHandler pHandler) throws IOException {}
    
}
