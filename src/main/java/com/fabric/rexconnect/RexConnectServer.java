package com.fabric.rexconnect;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.QuickServer;

/*================================================================================================*/
public class RexConnectServer implements ClientCommandHandler {

    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public static void main(String args[]) {
		try {
			QuickServer qs = new QuickServer();
			qs.setClientCommandHandler(RexConnectServer.class.getName());
			qs.setPort(8185);
			qs.setName("RexConnectServer");
			qs.startServer();
			System.out.println("Server started.");
		}
		catch ( Exception e ) {
			System.err.println("RexConnectServer Exception: "+e);
			e.printStackTrace();
		}
    }
    

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void handleCommand(ClientHandler pHandler, String pCommand) 
														throws SocketTimeoutException, IOException {
		long t = System.currentTimeMillis();
		String id = "";
		GremlinQuery q = null;
		
		try {
			int i = (pCommand == null ? -1 : pCommand.indexOf('|'));
			
			if ( i == -1 || i >= pCommand.length()-1 ) {
				pHandler.sendClientMsg("Invalid request: "+pCommand);
				return;
			}
			
			id = pCommand.substring(0, i);
			
			q = new GremlinQuery();
			q.execute(pCommand.substring(i+1));

			t = System.currentTimeMillis()-t;
			
			String json = "{"+
				"Request:'"+id+"',"+
				"Success:true,"+
				"Results:"+q.getResultListJson()+","+
				"QueryTime:"+t+
			"}";
			
			pHandler.sendClientMsg(json);
			//System.out.println("Command Success: "+t);
		}
		catch ( Exception e ) {
			t = System.currentTimeMillis()-t;
			
			String json = "{"+
				"Request:'"+id+"',"+
				"Success:false,"+
				(q == null ? "" : "results:"+q.getResultListJson()+",")+
				"QueryTime:"+t+","+
				"Exception:'"+e.getMessage().replace('\'', '"')+"'"+
			"}";
			
			pHandler.sendClientMsg(json);
			System.err.println("Command Exception: "+pCommand+" // "+e);
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void closingConnection(ClientHandler pHandler) throws IOException {
		// TODO Auto-generated method stub
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void gotConnected(ClientHandler pHandler) throws SocketTimeoutException, IOException {
		// TODO Auto-generated method stub
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void lostConnection(ClientHandler pHandler) throws IOException {
		// TODO Auto-generated method stub
	}
    
}
