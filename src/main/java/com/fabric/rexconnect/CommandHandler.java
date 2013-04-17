package com.fabric.rexconnect;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientHandler;

/*================================================================================================*/
public class CommandHandler implements ClientCommandHandler {

	private GremlinExecutor vGrem;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public CommandHandler() throws Exception {
		vGrem = new GremlinExecutor();
	}

	/*--------------------------------------------------------------------------------------------*/
	public void handleCommand(ClientHandler pHandler, String pCommand) 
														throws SocketTimeoutException, IOException {
		long t = System.currentTimeMillis();
		String id = "";
		String result = null;
		
		try {
			String[] parts = pCommand.split("#");
			
			if ( parts.length < 2 || parts.length > 3 ) {
				pHandler.sendClientMsg(" - Invalid request: "+pCommand);
				return;
			}
			
			id = parts[0];
			
			Map<String,Object> paramMap = null;
			
			if ( parts.length == 3 ) {
				paramMap = new ObjectMapper().readValue(parts[2], HashMap.class);

				for ( String s : paramMap.keySet() ) {
					Object val = paramMap.get(s);
					
					if ( BigDecimal.class.isInstance(val) ) {
						paramMap.put(s, ((BigDecimal)val).longValueExact());
					}
				}
			}

			result = vGrem.execute(parts[1], paramMap);
			t = System.currentTimeMillis()-t;
			
			String json = "{"+
				"\"request\":\""+id+"\","+
				"\"success\":true,"+
				"\"results\":"+result+","+
				"\"queryTime\":"+t+
			"}";
			
			pHandler.sendClientMsg(json);
			System.out.println("Response "+id+": "+json.length()+" chars, "+t+"ms");
		}
		catch ( Exception e ) {
			System.err.println("Exception "+id+":");
			System.err.println(" - Query: "+pCommand+"\n - Details: "+e);
			e.printStackTrace();
			
			String msg = e.getMessage();
			t = System.currentTimeMillis()-t;
			
			String json = "{"+
				"\"request\":\""+id+"\","+
				"\"success\":false,"+
				(result == null ? "" : "\"results\":"+result+",")+
				"\"queryTime\":"+t+","+
				"\"exception\":'"+(msg == null ? e.toString() : msg.replace('\'', '"'))+"'"+
			"}";
			
			pHandler.sendClientMsg(json);
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
