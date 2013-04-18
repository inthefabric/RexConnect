package com.fabric.rexconnect;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/*================================================================================================*/
public class RexConnectConsole {
	
	private static GremlinExecutor vGremEx;
	private static String vPrevScript;
	private static String vPrevParams;
	
	
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public static void main(String args[]) {
		try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.ERROR);
			
			vGremEx = new GremlinExecutor();
			vPrevScript = "";
			vPrevParams = "";
			
			Properties props = RexConnectServer.buildRexConfig();
			RexConnectServer.printHeader("Console", props);
			
			while ( true ) {
				String script = prompt();
				
				if ( !handleSpecialCommandAndPrint(script) ) {
					String params = paramPrompt();
					System.out.println("");
					executeAndPrint(script, params);
				}
				
		    	System.out.println("\n");
			}
		}
		catch ( Exception e ) {
			System.err.println("RexConnectConsole Exception: "+e);
			e.printStackTrace();
		}
    }

    /*--------------------------------------------------------------------------------------------*/
    private static Boolean handleSpecialCommandAndPrint(String pScript) throws Exception {
		if ( pScript.equals("-prev") ) {
			System.out.println("");
			executeAndPrint(vPrevScript, vPrevParams);
			return true;
		}
		
		return false;
    }
    
    /*--------------------------------------------------------------------------------------------*/
    private static String executeAndPrint(String pScript, String pParams) throws Exception {
    	vPrevScript = pScript;
    	vPrevParams = pParams;
    	
		Map<String,Object> paramMap = null;
		
    	try {
    		if ( pParams.length() > 0 ) {
    			paramMap = new ObjectMapper().readValue(pParams, HashMap.class);
    		}
    	}
		catch ( Exception e ) {
			System.err.println("... INPUT ERROR: "+e);
			System.err.flush();
		}
    	
    	////
    	
    	String result = null;
    	long t = System.currentTimeMillis();

    	try {
			result = vGremEx.execute(pScript, paramMap);
			System.out.println("... "+result);
    	}
		catch ( Exception e ) {
			System.err.println("... ERROR: "+e);
			System.err.flush();
		}
    	
    	t = (System.currentTimeMillis()-t);
    	Thread.sleep(10); //helps sync stderr output
		System.out.println("... "+t+"ms");
    	return result;
    }
    

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    private static String prompt() throws IOException {
    	System.out.print("RexConn script> ");
    	return readLine();
    }
    
    /*--------------------------------------------------------------------------------------------*/
    private static String paramPrompt() throws IOException {
    	System.out.print("        params> ");
    	return readLine();
    }

    /*--------------------------------------------------------------------------------------------*/
    private static String readLine() throws IOException {
		return new BufferedReader(new InputStreamReader(System.in)).readLine();
    }
    
}