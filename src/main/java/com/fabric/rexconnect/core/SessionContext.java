package com.fabric.rexconnect.core;

import java.util.UUID;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/*================================================================================================*/
public class SessionContext {

    private static final Logger vLog = Logger.getLogger(SessionContext.class);
	
	private UUID vSessId;
	private BaseConfiguration vRexsterClientConfig;
	private boolean vConsoleMode;
	private boolean vPrettyMode;
	private boolean vDebugMode;
	private RexConnectClient vPerRequestClient;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public SessionContext(BaseConfiguration pRexsterClientConfig) {
		vRexsterClientConfig = pRexsterClientConfig;
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void openSession(UUID pSessId) {
		vSessId = pSessId;
	}

	/*--------------------------------------------------------------------------------------------*/
	public void closeSession() {
		vSessId = null;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public UUID getSessionId() {
		return vSessId;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public Boolean isSessionOpen() {
		return (vSessId != null);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public RexConnectClient getOrOpenClient() throws Exception {
		if ( vPerRequestClient == null ) {
			vPerRequestClient = RexConnectClient.create(this, vRexsterClientConfig);
		}
		
		return vPerRequestClient;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void closeClientIfExists() {
		if ( vPerRequestClient == null ) {
			return;
		}
		
		try {
			vPerRequestClient.closeConnections();
		}
		catch ( Exception e ) {
			logAndPrintErr("SessContext.Close Exception: "+e, vLog, Level.ERROR, e);
		}
		
		vPerRequestClient = null;
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void setConsoleMode(boolean pConsole) {
		vConsoleMode = pConsole;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void setConfigPrettyMode(boolean pPretty) {
		vPrettyMode = pPretty;
	}

	/*--------------------------------------------------------------------------------------------*/
	public void setConfigDebugMode(boolean pDebug) {
		vDebugMode = pDebug;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public boolean getConsoleMode() {
		return vConsoleMode;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public boolean getConfigPrettyMode() {
		return vPrettyMode;
	}

	/*--------------------------------------------------------------------------------------------*/
	public boolean getConfigDebugMode() {
		return vDebugMode;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void logAndPrint(String pText, Logger pLog, Level pLevel) {
		pLog.log(pLevel, pText);
		
		if ( vConsoleMode ) {
			System.out.println(pText);
		}
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void logAndPrintErr(String pText, Logger pLog, Level pLevel, Throwable pThrow) {
		pLog.log(pLevel, pText, pThrow);

		if ( vConsoleMode ) {
			System.err.println(pText);
			pThrow.printStackTrace(System.err);
		}
	}
	
}