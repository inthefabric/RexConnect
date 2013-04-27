package com.fabric.rexconnect.core;

import java.util.UUID;

import org.apache.commons.configuration.BaseConfiguration;

/*================================================================================================*/
public class SessionContext {
	
	private UUID vSessId;
	private BaseConfiguration vRexsterClientConfig;
	private boolean vPrettyMode;
	private boolean vDebugMode;
	
	
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
	public RexConnectClient createClient() throws Exception {
		return RexConnectClient.create(this, vRexsterClientConfig);
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public void setConfigPrettyMode(boolean pPretty) {
		vPrettyMode = pPretty;
	}

	/*--------------------------------------------------------------------------------------------*/
	public void setConfigDebugMode(boolean pDebug) {
		vDebugMode = pDebug;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public boolean getConfigPrettyMode() {
		return vPrettyMode;
	}

	/*--------------------------------------------------------------------------------------------*/
	public boolean getConfigDebugMode() {
		return vDebugMode;
	}
	
}