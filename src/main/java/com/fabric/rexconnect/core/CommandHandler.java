package com.fabric.rexconnect.core;

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

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void handleCommand(ClientHandler pHandler, String pCommand) 
														throws SocketTimeoutException, IOException {
		System.out.println("ECHO: "+pCommand);
		
		/*
		long t = System.currentTimeMillis();
		String id = "";
		String result = null;
		Boolean success = false;
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
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

			result = vGrem.execute(null, parts[1], paramMap);
			success = true;
		}
		catch ( Exception e ) {
			System.err.println("Exception "+id+":");
			System.err.println(" - Query: "+pCommand+"\n - Details: "+e);
			e.printStackTrace();
			
			String msg = e.getMessage();
			jsonMap.put("exception", (msg == null ? e.toString() : msg));
		}

		t = System.currentTimeMillis()-t;
		jsonMap.put("request", id);
		jsonMap.put("success", success);
		jsonMap.put("queryTime", t);
		
		if ( result != null ) {
			//use placeholder to directly insert result JSON
			jsonMap.put("results", ResultsValuePlaceholder);
		}
		
		String json = new ObjectMapper().writeValueAsString(jsonMap);
		
		if ( result != null ) {
			json = json.replace('"'+ResultsValuePlaceholder+'"', result);
		}
		
		pHandler.sendClientMsg(json);
		
		System.out.println("Response "+id+": "+(success ? "success" : "failure")+", "+
			json.length()+" chars, "+t+"ms");
		//System.out.println("\n"+json+"\n");
		*/
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void closingConnection(ClientHandler pHandler) throws IOException {}
	
	/*--------------------------------------------------------------------------------------------*/
	public void gotConnected(ClientHandler pHandler) throws SocketTimeoutException, IOException {}
	
	/*--------------------------------------------------------------------------------------------*/
	public void lostConnection(ClientHandler pHandler) throws IOException {}
    
}
