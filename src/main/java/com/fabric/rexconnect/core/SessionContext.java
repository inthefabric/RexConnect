package com.fabric.rexconnect.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.tinkerpop.rexster.protocol.msg.ScriptRequestMessage;

/*================================================================================================*/
public class SessionContext {
	
	private UUID vSessId;
	private Boolean vUseSession;
	private List<ScriptRequestMessage> vReqList;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public SessionContext(Boolean pUseSession) {
		vSessId = UUID.randomUUID();
		vUseSession = pUseSession;
		vReqList = new ArrayList<ScriptRequestMessage>();
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public UUID getSessionId() {
		return vSessId;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public Boolean useSession() {
		return vUseSession;
	}
	
	/*--------------------------------------------------------------------------------------------*/
	public void addRequest(ScriptRequestMessage pReq) {
		vReqList.add(pReq);
	}
	
}