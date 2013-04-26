package com.fabric.rexconnect.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.GremlinExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.tinkerpop.rexster.client.RexProException;

/*================================================================================================*/
public class RexConnectConsole {
	
	private static GremlinExecutor vGremEx;
	private static String vPrevScript;
	private static String vPrevParams;
	private static boolean vPrettyPrint;
	
	
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /*--------------------------------------------------------------------------------------------*/
    public static void main(String args[]) throws Exception {
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
			System.err.println("RexConnectConsole Error:");
			System.err.println("");
			throw e;
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
    	//vPrettyPrint = true;
    	
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
			result = vGremEx.execute(null, pScript, paramMap);
	    	t = (System.currentTimeMillis()-t);
			
			if ( vPrettyPrint ) {
				ObjectMapper om = new ObjectMapper();
				Object resultObj = om.readValue(result, Object.class);
				ObjectWriter ow = om.writerWithDefaultPrettyPrinter();
				result = ow.writeValueAsString(resultObj);
				System.out.println(result+"\n");
			}
			else {
				System.out.println("... "+result);
			}
    	}
		catch ( RexProException rpe ) {
	    	t = (System.currentTimeMillis()-t);
	    	
			System.err.println("... ERROR: "+rpe);
			System.err.flush();
		}
    	
    	Thread.sleep(10); //helps to sync stderr output
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