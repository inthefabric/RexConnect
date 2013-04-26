package com.fabric.rexconnect.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.tinkerpop.rexster.protocol.msg.RexProMessage;

/*================================================================================================*/
public class SessionContext {
	
	private UUID vSessId;
	private Boolean vUseSession;
	private List<RexProMessage> vReqList;
	private List<RexProMessage> vRespList;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public SessionContext(Boolean pUseSession) {
		vUseSession = pUseSession;
		vReqList = new ArrayList<RexProMessage>();
		vRespList = new ArrayList<RexProMessage>();
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public UUID getSessionId() {
		return vSessId;
	}

	/*--------------------------------------------------------------------------------------------*/
	public void openSession(UUID pSessId) {
		vSessId = pSessId;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public Boolean isSessionOpen() {
		return (vSessId != null);
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public Boolean useSession() {
		return vUseSession;
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