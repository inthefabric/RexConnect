package com.fabric.rexconnect;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.commons.configuration.BaseConfiguration;
import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.QuickServer;

import com.tinkerpop.rexster.client.RexsterClientTokens;

/*================================================================================================*/
public class RexConnectServer implements ClientCommandHandler {

	private static BaseConfiguration RexConfig;
	
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public static void main(String args[]) {
		try {
			RexConfig = new BaseConfiguration() {{
			    addProperty(RexsterClientTokens.CONFIG_PORT, 8184);
			    addProperty(RexsterClientTokens.CONFIG_HOSTNAME, "ENTER_IP_ADDRESSES");
			    addProperty(RexsterClientTokens.CONFIG_MESSAGE_RETRY_WAIT_MS, 0);
			}};
			
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
		String result = null;
		
		try {
			int i = (pCommand == null ? -1 : pCommand.indexOf('|'));
			
			if ( i == -1 || i >= pCommand.length()-1 ) {
				pHandler.sendClientMsg("Invalid request: "+pCommand);
				return;
			}
			
			id = pCommand.substring(0, i);
			
			GremlinQuery2 q = new GremlinQuery2(RexConfig);
			result = q.execute(pCommand.substring(i+1));

			t = System.currentTimeMillis()-t;
			
			String json = "{"+
				"Request:'"+id+"',"+
				"Success:true,"+
				"Results:"+result+","+
				"QueryTime:"+t+
			"}";
			
			pHandler.sendClientMsg(json);
		}
		catch ( Exception e ) {
			t = System.currentTimeMillis()-t;
			String msg = e.getMessage();
			
			String json = "{"+
				"Request:'"+id+"',"+
				"Success:false,"+
				(result == null ? "" : "results:"+result+",")+
				"QueryTime:"+t+","+
				"Exception:'"+(msg == null ? e.toString() : msg.replace('\'', '"'))+"'"+
			"}";
			
			pHandler.sendClientMsg(json);
			System.err.println("Command Exception: "+pCommand+" // "+e+"\n"+e.getStackTrace());
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
