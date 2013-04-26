package com.fabric.rexconnect.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jline.console.ConsoleReader;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.GremlinExecutor;
import com.fabric.rexconnect.core.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.tinkerpop.rexster.client.RexProException;

/*================================================================================================*/
public class RexConnectConsole {
	
	private static GremlinExecutor vGremEx;
	private static SessionContext vCurrSessCtx;
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
			vCurrSessCtx = new SessionContext(false);
			vPrevScript = "";
			vPrevParams = "";
			
			Properties props = RexConnectServer.buildRexConfig();
			RexConnectServer.printHeader("Console", props);
			
			while ( true ) {
				String script = prompt();
				
				if ( !handleSpecialCommandAndPrint(script) ) {
					String params = paramPrompt();
					System.out.println();
					executeAndPrint(script, params);
				}
				
		    	System.out.println();
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
			executeAndPrint(vPrevScript, vPrevParams);
			return true;
		}
		
		if ( pScript.equals("-pretty") ) {
			vPrettyPrint = true;
			return true;
		}
		
		if ( pScript.equals("-ugly") ) {
			vPrettyPrint = false;
			return true;
		}

		if ( pScript.equals("-session") ) {
			vCurrSessCtx = new SessionContext(true);
			return true;
		}

		if ( pScript.equals("-commit") ) {
			vGremEx.commit(vCurrSessCtx);
			vCurrSessCtx = new SessionContext(false);
			return true;
		}

		if ( pScript.equals("-rollback") ) {
			vGremEx.rollback(vCurrSessCtx);
			vCurrSessCtx = new SessionContext(false);
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
			result = vGremEx.execute(vCurrSessCtx, pScript, paramMap);
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
    	return new ConsoleReader().readLine();
    }
    
}