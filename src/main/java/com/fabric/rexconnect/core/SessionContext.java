package com.fabric.rexconnect.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.configuration.BaseConfiguration;

import com.tinkerpop.rexster.protocol.msg.RexProMessage;

/*================================================================================================*/
public class SessionContext {
	
	private UUID vSessId;
	private BaseConfiguration vRexsterClientConfig;
	private List<RexProMessage> vReqList;
	private List<RexProMessage> vRespList;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public SessionContext(BaseConfiguration pRexsterClientConfig) {
		vRexsterClientConfig = pRexsterClientConfig;
		vReqList = new ArrayList<RexProMessage>();
		vRespList = new ArrayList<RexProMessage>();
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
	
	/*--------------------------------------------------------------------------------------------*/
	public void addRequest(RexProMessage pMsg) {
		vReqList.add(pMsg);
	}

	/*--------------------------------------------------------------------------------------------*/
	public void addResponse(RexProMessage pMsg) {
		vRespList.add(pMsg);
	}
	
}