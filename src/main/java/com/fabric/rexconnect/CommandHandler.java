package com.fabric.rexconnect;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientHandler;

/*================================================================================================*/
public class CommandHandler implements ClientCommandHandler {

	private GremlinExecutor vGrem;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public CommandHandler() throws Exception {
		vGrem = new GremlinExecutor(RexConnectServer.RexConfig);
	}

	/*--------------------------------------------------------------------------------------------*/
	public void handleCommand(ClientHandler pHandler, String pCommand) 
														throws SocketTimeoutException, IOException {
		long t = System.currentTimeMillis();
		String id = "";
		String result = null;
		
		try {
			int i = (pCommand == null ? -1 : pCommand.indexOf('|'));
			
			if ( i == -1 || i >= pCommand.length()-1 ) {
				pHandler.sendClientMsg("Invalid request: "+pCommand);
				return;
			}
			
			id = pCommand.substring(0, i);
			
			result = vGrem.execute(pCommand.substring(i+1));
			t = System.currentTimeMillis()-t;
			
			String json = "{"+
				"request:'"+id+"',"+
				"success:true,"+
				"results:"+result+","+
				"queryTime:"+t+
			"}";
			
			pHandler.sendClientMsg(json);
		}
		catch ( Exception e ) {
			t = System.currentTimeMillis()-t;
			String msg = e.getMessage();
			
			String json = "{"+
				"request:'"+id+"',"+
				"success:false,"+
				(result == null ? "" : "results:"+result+",")+
				"queryTime:"+t+","+
				"exception:'"+(msg == null ? e.toString() : msg.replace('\'', '"'))+"'"+
			"}";
			
			pHandler.sendClientMsg(json);
			System.err.println("Command Exception: "+pCommand+" // "+e+"\n"+e.getStackTrace());
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
